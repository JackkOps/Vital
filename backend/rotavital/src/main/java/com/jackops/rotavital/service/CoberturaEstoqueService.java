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
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoSanguineo;

/**
 * Relatorio de cobertura nacional: cruza cada solicitacao com todo o estoque (O(S x B)) e conta
 * quantas bolsas disponiveis, validas, do mesmo componente e compativeis (ABO/Rh) poderiam atende-la.
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

    private static final TipoSanguineo[] TIPOS = TipoSanguineo.values();

    private final AlocacaoBolsaService alocacaoBolsaService;
    private final GeradorDadosService geradorDadosService;

    public CoberturaEstoqueService(AlocacaoBolsaService alocacaoBolsaService,
                                   GeradorDadosService geradorDadosService) {
        this.alocacaoBolsaService = alocacaoBolsaService;
        this.geradorDadosService = geradorDadosService;
    }

    public RelatorioCoberturaResponse gerarRelatorio(int quantidadeBolsas, int quantidadeSolicitacoes,
                                                     int threads, boolean virtual) {
        List<Bolsa> bolsas = geradorDadosService.bolsas(quantidadeBolsas);
        List<SolicitacaoSangue> solicitacoes = geradorDadosService.solicitacoes(quantidadeSolicitacoes);
        Modo modo = virtual ? Modo.VIRTUAL : threads == 1 ? Modo.SEQUENCIAL : Modo.PLATAFORMA;

        long inicio = System.nanoTime();
        List<CoberturaPorTipo> porTipo = calcular(bolsas, solicitacoes, threads, modo);
        double tempoMs = (System.nanoTime() - inicio) / 1_000_000.0;

        long totalAtendiveis = porTipo.stream().mapToLong(CoberturaPorTipo::solicitacoesAtendiveis).sum();
        return new RelatorioCoberturaResponse(quantidadeBolsas, quantidadeSolicitacoes, threads, modo.name(),
                tempoMs, totalAtendiveis, porTipo);
    }

    public List<CoberturaPorTipo> calcular(List<Bolsa> bolsas, List<SolicitacaoSangue> solicitacoes,
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

    private Parcial processarFatia(List<Bolsa> bolsas, List<SolicitacaoSangue> solicitacoes, int inicio, int fim) {
        LocalDate hoje = GeradorDadosService.DATA_REFERENCIA;
        LocalDate limiteVencimento = hoje.plusDays(7);
        Parcial parcial = new Parcial();

        for (int i = inicio; i < fim; i++) {
            SolicitacaoSangue solicitacao = solicitacoes.get(i);
            long compativeis = 0;
            long vencendo = 0;

            for (Bolsa bolsa : bolsas) {
                if (bolsa.getStatus() == StatusBolsa.DISPONIVEL
                        && bolsa.getTipoComponente() == solicitacao.getTipoComponente()
                        && !bolsa.getDataValidade().isBefore(hoje)
                        && alocacaoBolsaService.isCompativelPorTipoSanguineo(
                                solicitacao.getTipoSanguineo(), bolsa.getTipoSanguineo())) {
                    compativeis++;
                    if (!bolsa.getDataValidade().isAfter(limiteVencimento)) {
                        vencendo++;
                    }
                }
            }

            int tipo = solicitacao.getTipoSanguineo().ordinal();
            parcial.solicitacoes[tipo]++;
            parcial.bolsasCompativeis[tipo] += compativeis;
            parcial.bolsasVencendo[tipo] += vencendo;
            if (compativeis >= solicitacao.getQuantidade()) {
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
