package com.jackops.rotavital.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.CalculoRotaRequest;
import com.jackops.rotavital.dto.EntregaRotaResponse;
import com.jackops.rotavital.dto.LocalRedeResponse;
import com.jackops.rotavital.dto.RedeDistribuicaoResponse;
import com.jackops.rotavital.dto.RotaResponse;
import com.jackops.rotavital.dto.TrechoResponse;
import com.jackops.rotavital.estrutura.Grafo;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.LocalRede;
import com.jackops.rotavital.model.Rota;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.Trecho;
import com.jackops.rotavital.model.enums.StatusRota;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.repository.LocalRedeRepository;
import com.jackops.rotavital.repository.RotaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;
import com.jackops.rotavital.repository.TrechoRepository;

/**
 * História 3 - Cálculo de rota de distribuição. Monta o grafo da rede, usa o
 * Dijkstra para o menor tempo entre cada par de locais e escolhe a ordem de
 * visita aos hospitais de menor tempo total, checando a janela da cadeia fria.
 */
@Service
public class RotaService {

    static final HttpStatusCode SEM_ROTA = HttpStatusCode.valueOf(422);

    private final LocalRedeRepository localRepository;
    private final TrechoRepository trechoRepository;
    private final RotaRepository rotaRepository;
    private final SolicitacaoSangueRepository solicitacaoRepository;
    private final PilhaHistorico pilhaHistorico;
    private final int janelaPadraoMinutos;
    private final int maximoDestinos;

    public RotaService(LocalRedeRepository localRepository, TrechoRepository trechoRepository,
            RotaRepository rotaRepository, SolicitacaoSangueRepository solicitacaoRepository,
            PilhaHistorico pilhaHistorico,
            @Value("${rotavital.rotas.janela-padrao-minutos:120}") int janelaPadraoMinutos,
            @Value("${rotavital.rotas.maximo-destinos:8}") int maximoDestinos) {
        this.localRepository = localRepository;
        this.trechoRepository = trechoRepository;
        this.rotaRepository = rotaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.pilhaHistorico = pilhaHistorico;
        this.janelaPadraoMinutos = janelaPadraoMinutos;
        this.maximoDestinos = maximoDestinos;
    }

    @Transactional(readOnly = true)
    public RedeDistribuicaoResponse rede() {
        return new RedeDistribuicaoResponse(
                localRepository.findAllByOrderByIdAsc().stream().map(this::paraResponse).toList(),
                trechoRepository.findAllByOrderByIdAsc().stream()
                        .map(t -> new TrechoResponse(t.getId(), t.getOrigem().getId(), t.getDestino().getId(),
                                t.getTempoMinutos(), t.getDistanciaKm()))
                        .toList());
    }

    @Transactional
    public RotaResponse calcular(CalculoRotaRequest request) {
        LocalRede origem = localRepository.findById(request.origemId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local de origem não encontrado"));
        int janela = request.janelaMinutos() != null ? request.janelaMinutos() : janelaPadraoMinutos;

        List<LocalRede> destinos = resolverDestinos(request, origem);
        if (destinos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um hospital de destino");
        }
        if (destinos.size() > maximoDestinos) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Uma rota atende no máximo " + maximoDestinos + " hospitais");
        }

        RedeEmGrafo rede = montarGrafo();
        int verticeOrigem = rede.indice(origem.getId());
        int[] verticesDestino = destinos.stream().mapToInt(local -> rede.indice(local.getId())).toArray();

        // Dijkstra a partir da origem e de cada destino: menor tempo entre todos os pares que importam.
        Grafo.CaminhosMinimos daOrigem = rede.grafo().menoresCaminhos(verticeOrigem);
        for (LocalRede destino : destinos) {
            if (!daOrigem.alcancavel(rede.indice(destino.getId()))) {
                throw new ResponseStatusException(SEM_ROTA,
                        "Não há rota disponível de " + origem.getNome() + " até " + destino.getNome());
            }
        }
        Grafo.CaminhosMinimos[] dosDestinos = new Grafo.CaminhosMinimos[verticesDestino.length];
        for (int i = 0; i < verticesDestino.length; i++) {
            dosDestinos[i] = rede.grafo().menoresCaminhos(verticesDestino[i]);
        }

        OrdemDeVisita escolhida = escolherOrdem(daOrigem, dosDestinos, verticesDestino, janela);

        List<Integer> caminho = new ArrayList<>();
        List<String> entregas = new ArrayList<>();
        Grafo.CaminhosMinimos trechoAtual = daOrigem;
        for (int i = 0; i < escolhida.ordem().length; i++) {
            int indiceDestino = escolhida.ordem()[i];
            int[] segmento = trechoAtual.caminhoAte(verticesDestino[indiceDestino]);
            for (int passo = caminho.isEmpty() ? 0 : 1; passo < segmento.length; passo++) {
                caminho.add(segmento[passo]);
            }
            entregas.add(destinos.get(indiceDestino).getId() + "@" + escolhida.chegadas()[i]);
            trechoAtual = dosDestinos[indiceDestino];
        }

        StatusRota status = escolhida.cumpreJanela() ? StatusRota.CONFIRMADA : StatusRota.AGUARDANDO_CONFIRMACAO;
        Rota rota = rotaRepository.save(new Rota(
                origem,
                caminho.stream().map(v -> String.valueOf(rede.ids()[v])).collect(Collectors.joining(",")),
                String.join(",", entregas),
                (int) escolhida.tempoTotal(),
                arredondar(escolhida.distanciaTotal()),
                janela,
                status));
        pilhaHistorico.empilhar("Cálculo de rota " + rota.getId());
        return paraResponse(rota);
    }

    @Transactional
    public RotaResponse confirmar(Long id) {
        Rota rota = rotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rota não encontrada"));
        if (rota.getStatus() == StatusRota.CONFIRMADA) {
            throw new RegraDeNegocioException("Rota já está confirmada");
        }
        rota.setStatus(StatusRota.CONFIRMADA);
        pilhaHistorico.empilhar("Confirmação manual da rota " + rota.getId());
        return paraResponse(rotaRepository.save(rota));
    }

    @Transactional(readOnly = true)
    public List<RotaResponse> listar() {
        return rotaRepository.findAllByOrderByIdDesc().stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public RotaResponse buscar(Long id) {
        return rotaRepository.findById(id).map(this::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rota não encontrada"));
    }

    /**
     * Junta os destinos informados diretamente e os hospitais das solicitações,
     * sem repetir e sem incluir a própria origem.
     */
    private List<LocalRede> resolverDestinos(CalculoRotaRequest request, LocalRede origem) {
        Set<Long> ids = new LinkedHashSet<>();
        List<LocalRede> destinos = new ArrayList<>();

        if (request.destinoIds() != null) {
            for (Long id : request.destinoIds()) {
                LocalRede local = localRepository.findById(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Local de destino não encontrado: " + id));
                if (!local.getId().equals(origem.getId()) && ids.add(local.getId())) {
                    destinos.add(local);
                }
            }
        }
        if (request.solicitacaoIds() != null) {
            for (Long id : request.solicitacaoIds()) {
                SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada: " + id));
                if (solicitacao.getStatus() == StatusSolicitacao.CANCELADA) {
                    throw new RegraDeNegocioException("Solicitação " + id + " está cancelada");
                }
                LocalRede local = localRepository.findByNomeIgnoreCase(solicitacao.getNomeHospital().trim())
                        .orElseThrow(() -> new RegraDeNegocioException(
                                solicitacao.getNomeHospital() + " não está cadastrado na rede de distribuição"));
                if (!local.getId().equals(origem.getId()) && ids.add(local.getId())) {
                    destinos.add(local);
                }
            }
        }
        return destinos;
    }

    private RedeEmGrafo montarGrafo() {
        List<LocalRede> locais = localRepository.findAllByOrderByIdAsc();
        long[] ids = new long[locais.size()];
        Map<Long, Integer> indices = new HashMap<>();
        for (int i = 0; i < locais.size(); i++) {
            ids[i] = locais.get(i).getId();
            indices.put(locais.get(i).getId(), i);
        }
        Grafo grafo = new Grafo(locais.size());
        for (Trecho trecho : trechoRepository.findAll()) {
            grafo.adicionarArestaDupla(indices.get(trecho.getOrigem().getId()), indices.get(trecho.getDestino().getId()),
                    trecho.getTempoMinutos(), trecho.getDistanciaKm());
        }
        return new RedeEmGrafo(grafo, ids, indices);
    }

    /**
     * Testa todas as ordens de visita (permutações) e fica com a de menor tempo
     * total; empate de tempo, menor distância. Como a janela é a mesma para todas
     * as entregas e a última chegada é o tempo total, essa ordem cumpre a janela
     * sempre que alguma outra cumprir.
     */
    static OrdemDeVisita escolherOrdem(Grafo.CaminhosMinimos daOrigem, Grafo.CaminhosMinimos[] dosDestinos,
            int[] verticesDestino, int janela) {
        OrdemDeVisita[] melhor = new OrdemDeVisita[1];
        int[] ordem = new int[verticesDestino.length];
        boolean[] usado = new boolean[verticesDestino.length];
        permutar(0, ordem, usado, daOrigem, dosDestinos, verticesDestino, janela, melhor);
        return melhor[0];
    }

    private static void permutar(int posicao, int[] ordem, boolean[] usado, Grafo.CaminhosMinimos daOrigem,
            Grafo.CaminhosMinimos[] dosDestinos, int[] verticesDestino, int janela, OrdemDeVisita[] melhor) {
        if (posicao == ordem.length) {
            OrdemDeVisita candidata = avaliar(ordem.clone(), daOrigem, dosDestinos, verticesDestino, janela);
            if (candidata.melhorQue(melhor[0])) {
                melhor[0] = candidata;
            }
            return;
        }
        for (int i = 0; i < ordem.length; i++) {
            if (!usado[i]) {
                usado[i] = true;
                ordem[posicao] = i;
                permutar(posicao + 1, ordem, usado, daOrigem, dosDestinos, verticesDestino, janela, melhor);
                usado[i] = false;
            }
        }
    }

    private static OrdemDeVisita avaliar(int[] ordem, Grafo.CaminhosMinimos daOrigem,
            Grafo.CaminhosMinimos[] dosDestinos, int[] verticesDestino, int janela) {
        long tempo = 0;
        double distancia = 0;
        long[] chegadas = new long[ordem.length];
        boolean cumpre = true;
        Grafo.CaminhosMinimos atual = daOrigem;
        for (int i = 0; i < ordem.length; i++) {
            int vertice = verticesDestino[ordem[i]];
            tempo += atual.tempos()[vertice];
            distancia += atual.distancias()[vertice];
            chegadas[i] = tempo;
            cumpre &= tempo <= janela;
            atual = dosDestinos[ordem[i]];
        }
        return new OrdemDeVisita(ordem, chegadas, tempo, distancia, cumpre);
    }

    private RotaResponse paraResponse(Rota rota) {
        Map<Long, LocalRede> locais = localRepository.findAll().stream()
                .collect(Collectors.toMap(LocalRede::getId, local -> local));

        List<LocalRedeResponse> caminho = Arrays.stream(rota.getCaminho().split(","))
                .map(id -> paraResponse(locais.get(Long.valueOf(id))))
                .toList();
        List<EntregaRotaResponse> entregas = Arrays.stream(rota.getEntregas().split(","))
                .map(item -> {
                    String[] partes = item.split("@");
                    LocalRede local = locais.get(Long.valueOf(partes[0]));
                    int chegada = Integer.parseInt(partes[1]);
                    return new EntregaRotaResponse(local.getId(), local.getNome(), chegada,
                            chegada <= rota.getJanelaMinutos());
                })
                .toList();
        boolean cumpre = entregas.stream().allMatch(EntregaRotaResponse::dentroDaJanela);

        return new RotaResponse(rota.getId(), paraResponse(rota.getOrigem()), caminho, entregas,
                rota.getTempoTotalMinutos(), rota.getDistanciaTotalKm(), rota.getJanelaMinutos(), cumpre,
                rota.getStatus(), alerta(rota, cumpre, entregas), rota.getCriadaEm());
    }

    private String alerta(Rota rota, boolean cumpre, List<EntregaRotaResponse> entregas) {
        if (cumpre) {
            return null;
        }
        String atrasadas = entregas.stream().filter(e -> !e.dentroDaJanela())
                .map(e -> e.nome() + " (" + e.chegadaMinutos() + " min)")
                .collect(Collectors.joining(", "));
        if (rota.getStatus() == StatusRota.CONFIRMADA) {
            return "Rota confirmada manualmente com risco de atraso: " + atrasadas
                    + " passa(m) da janela de " + rota.getJanelaMinutos() + " min.";
        }
        return "Risco de atraso: " + atrasadas + " passa(m) da janela de " + rota.getJanelaMinutos()
                + " min da cadeia fria. A rota não foi confirmada automaticamente.";
    }

    private LocalRedeResponse paraResponse(LocalRede local) {
        return new LocalRedeResponse(local.getId(), local.getNome(), local.getTipo());
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    private record RedeEmGrafo(Grafo grafo, long[] ids, Map<Long, Integer> indices) {
        int indice(Long id) {
            return indices.get(id);
        }
    }

    record OrdemDeVisita(int[] ordem, long[] chegadas, long tempoTotal, double distanciaTotal, boolean cumpreJanela) {
        boolean melhorQue(OrdemDeVisita outra) {
            return outra == null || tempoTotal < outra.tempoTotal()
                    || (tempoTotal == outra.tempoTotal() && distanciaTotal < outra.distanciaTotal());
        }
    }
}
