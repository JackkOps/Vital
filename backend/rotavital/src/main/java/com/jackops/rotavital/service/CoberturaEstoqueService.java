package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import com.jackops.rotavital.dto.CoberturaPorTipo;
import com.jackops.rotavital.dto.RelatorioCoberturaResponse;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.model.relatorio.BolsaEmEstoque;
import com.jackops.rotavital.model.relatorio.DemandaHospitalar;

/**
 * Relatorio de cobertura nacional: cruza cada solicitacao com todo o estoque (O(S x B)) e conta
 * quantas bolsas disponiveis, validas, do mesmo componente, compativeis (ABO/Rh) e armazenadas a ate
 * {@value #RAIO_ATENDIMENTO_KM} km do hospital poderiam atende-la.
 *
 * A versao paralela divide as solicitacoes em fatias independentes. Cada thread apenas le o estoque
 * e acumula em uma parcial propria; as parciais sao somadas no final. Nao ha estado compartilhado
 * mutavel, portanto nao ha race condition.
 */
@Service
public class CoberturaEstoqueService {

    public enum Modo {
        SEQUENCIAL, PLATAFORMA, VIRTUAL
    }

    public static final double RAIO_ATENDIMENTO_KM = 150.0;

    private static final TipoSanguineo[] TIPOS = TipoSanguineo.values();
    private static final double KM_POR_GRAU = 111.2;

    private final AlocacaoBolsaService alocacaoBolsaService;
    private final GeradorDadosService geradorDadosService;

    public CoberturaEstoqueService(AlocacaoBolsaService alocacaoBolsaService,
                                   GeradorDadosService geradorDadosService) {
        this.alocacaoBolsaService = alocacaoBolsaService;
        this.geradorDadosService = geradorDadosService;
    }

    public RelatorioCoberturaResponse gerarRelatorio(int quantidadeBolsas, int quantidadeSolicitacoes,
                                                     int threads, boolean virtual) {
        List<BolsaEmEstoque> bolsas = geradorDadosService.bolsas(quantidadeBolsas);
        List<DemandaHospitalar> solicitacoes = geradorDadosService.solicitacoes(quantidadeSolicitacoes);
        Modo modo = virtual ? Modo.VIRTUAL : threads == 1 ? Modo.SEQUENCIAL : Modo.PLATAFORMA;

        long inicio = System.nanoTime();
        List<CoberturaPorTipo> porTipo = calcular(bolsas, solicitacoes, threads, modo);
        double tempoMs = (System.nanoTime() - inicio) / 1_000_000.0;

        long totalAtendiveis = porTipo.stream().mapToLong(CoberturaPorTipo::solicitacoesAtendiveis).sum();
        return new RelatorioCoberturaResponse(quantidadeBolsas, quantidadeSolicitacoes, threads, modo.name(),
                tempoMs, totalAtendiveis, porTipo);
    }

    public List<CoberturaPorTipo> calcular(List<BolsaEmEstoque> bolsas, List<DemandaHospitalar> solicitacoes,
                                           int threads, Modo modo) {
        if (modo == Modo.SEQUENCIAL) {
            return processarFatia(bolsas, solicitacoes, 0, solicitacoes.size()).paraLista();
        }

        try (ExecutorService executor = modo == Modo.VIRTUAL
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threads)) {
            List<Future<Parcial>> futuros = new ArrayList<>(threads);
            int total = solicitacoes.size();
            for (int t = 0; t < threads; t++) {
                int inicio = (int) ((long) total * t / threads);
                int fim = (int) ((long) total * (t + 1) / threads);
                futuros.add(executor.submit(() -> processarFatia(bolsas, solicitacoes, inicio, fim)));
            }

            Parcial resultado = new Parcial();
            for (Future<Parcial> futuro : futuros) {
                resultado.somar(futuro.get());
            }
            return resultado.paraLista();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Processamento interrompido.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Falha no processamento paralelo.", e.getCause());
        }
    }

    private Parcial processarFatia(List<BolsaEmEstoque> bolsas, List<DemandaHospitalar> solicitacoes,
                                   int inicio, int fim) {
        LocalDate hoje = GeradorDadosService.DATA_REFERENCIA;
        LocalDate limiteVencimento = hoje.plusDays(7);
        Parcial parcial = new Parcial();

        for (int i = inicio; i < fim; i++) {
            DemandaHospitalar solicitacao = solicitacoes.get(i);
            // Distancia equiretangular: precisa o suficiente para raios de algumas centenas de km.
            double kmPorGrauLongitude = KM_POR_GRAU * Math.cos(Math.toRadians(solicitacao.latitude()));
            double raioAoQuadrado = RAIO_ATENDIMENTO_KM * RAIO_ATENDIMENTO_KM;
            long compativeis = 0;
            long vencendo = 0;

            for (BolsaEmEstoque bolsa : bolsas) {
                if (bolsa.status() == StatusBolsa.DISPONIVEL
                        && bolsa.tipoComponente() == solicitacao.tipoComponente()
                        && !bolsa.dataValidade().isBefore(hoje)
                        && alocacaoBolsaService.isCompativelPorTipoSanguineo(
                                solicitacao.tipoSanguineo(), bolsa.tipoSanguineo())) {
                    double dy = (bolsa.latitude() - solicitacao.latitude()) * KM_POR_GRAU;
                    double dx = (bolsa.longitude() - solicitacao.longitude()) * kmPorGrauLongitude;
                    if (dx * dx + dy * dy > raioAoQuadrado) {
                        continue;
                    }
                    compativeis++;
                    if (!bolsa.dataValidade().isAfter(limiteVencimento)) {
                        vencendo++;
                    }
                }
            }

            int tipo = solicitacao.tipoSanguineo().ordinal();
            parcial.solicitacoes[tipo]++;
            parcial.bolsasCompativeis[tipo] += compativeis;
            parcial.bolsasVencendo[tipo] += vencendo;
            if (compativeis >= solicitacao.quantidade()) {
                parcial.atendiveis[tipo]++;
            }
        }
        return parcial;
    }

    /** Acumulador local de cada thread, indexado pelo ordinal do tipo sanguineo. */
    private static final class Parcial {
        private final long[] solicitacoes = new long[TIPOS.length];
        private final long[] atendiveis = new long[TIPOS.length];
        private final long[] bolsasCompativeis = new long[TIPOS.length];
        private final long[] bolsasVencendo = new long[TIPOS.length];

        private void somar(Parcial outra) {
            for (int i = 0; i < TIPOS.length; i++) {
                solicitacoes[i] += outra.solicitacoes[i];
                atendiveis[i] += outra.atendiveis[i];
                bolsasCompativeis[i] += outra.bolsasCompativeis[i];
                bolsasVencendo[i] += outra.bolsasVencendo[i];
            }
        }

        private List<CoberturaPorTipo> paraLista() {
            List<CoberturaPorTipo> lista = new ArrayList<>(TIPOS.length);
            for (int i = 0; i < TIPOS.length; i++) {
                lista.add(new CoberturaPorTipo(TIPOS[i].getDescricao(), solicitacoes[i], atendiveis[i],
                        bolsasCompativeis[i], bolsasVencendo[i]));
            }
            return lista;
        }
    }
}
