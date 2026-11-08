package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@Service
public class AlocacaoBolsaService {

    private final BolsaRepository bolsaRepository;
    private final SolicitacaoSangueRepository solicitacaoSangueRepository;

    /**
     * Margem de segurança: a bolsa só entra na seleção se ainda faltarem pelo menos
     * esses dias para vencer, para não vencer durante o transporte.
     */
    private final int validadeMinimaDias;

    public AlocacaoBolsaService(BolsaRepository bolsaRepository,
                                SolicitacaoSangueRepository solicitacaoSangueRepository,
                                @Value("${rotavital.alocacao.validade-minima-dias:1}") int validadeMinimaDias) {
        this.bolsaRepository = bolsaRepository;
        this.solicitacaoSangueRepository = solicitacaoSangueRepository;
        this.validadeMinimaDias = validadeMinimaDias;
    }

    /**
     * Aloca a bolsa compatível que vence primeiro (FEFO) e grava o vínculo com a
     * solicitação. A primeira bolsa leva o pedido para EM_ATENDIMENTO e a que completa
     * a quantidade o leva para ATENDIDA. Solicitação e candidatas ficam travadas até o
     * fim da transação, então pedidos simultâneos não pegam a mesma bolsa nem passam
     * da quantidade.
     */
    @Transactional
    public RespostaAlocacaoBolsa alocarBolsaCompativel(Long solicitacaoId) {
        SolicitacaoSangue solicitacao = solicitacaoSangueRepository.travarPorId(solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada"));

        if (solicitacao.getStatus() == StatusSolicitacao.CANCELADA) {
            throw new RegraDeNegocioException("Solicitação cancelada não recebe bolsas");
        }

        long jaAlocadas = bolsaRepository.countBySolicitacaoId(solicitacaoId);
        if (solicitacao.getStatus() == StatusSolicitacao.ATENDIDA || jaAlocadas >= solicitacao.getQuantidade()) {
            throw new RegraDeNegocioException("Solicitação já recebeu a quantidade de bolsas pedida");
        }

        LocalDate validadeMinima = LocalDate.now().plusDays(validadeMinimaDias);
        List<Bolsa> bolsasCandidatas = bolsaRepository
                .travarPorStatusEComponente(StatusBolsa.DISPONIVEL, solicitacao.getTipoComponente()).stream()
                .filter(bolsa -> !bolsa.getDataValidade().isBefore(validadeMinima))
                .filter(bolsa -> isCompativel(solicitacao, bolsa))
                .sorted(Comparator.comparing(Bolsa::getDataValidade))
                .toList();

        if (bolsasCandidatas.isEmpty()) {
            return new RespostaAlocacaoBolsa(
                    null,
                    false,
                    "Nenhuma bolsa compatível disponível no momento.",
                    jaAlocadas,
                    solicitacao.getStatus()
            );
        }

        Bolsa bolsaSelecionada = bolsasCandidatas.get(0);
        bolsaSelecionada.setStatus(StatusBolsa.ALOCADA);
        bolsaSelecionada.setSolicitacao(solicitacao);
        bolsaRepository.save(bolsaSelecionada);

        long alocadas = jaAlocadas + 1;
        if (alocadas >= solicitacao.getQuantidade()) {
            solicitacao.setStatus(StatusSolicitacao.ATENDIDA);
            solicitacao.setAtendidaEm(LocalDateTime.now());
        } else {
            solicitacao.setStatus(StatusSolicitacao.EM_ATENDIMENTO);
        }
        solicitacaoSangueRepository.save(solicitacao);

        return new RespostaAlocacaoBolsa(
                bolsaSelecionada.getId(),
                true,
                "Bolsa alocada com sucesso conforme compatibilidade ABO/Rh e regra FEFO.",
                alocadas,
                solicitacao.getStatus()
        );
    }

    /**
     * Desfaz a alocação: a bolsa volta para DISPONIVEL e perde o vínculo. Se o pedido
     * estava ATENDIDA, ele volta para EM_ATENDIMENTO, porque ficou faltando uma bolsa.
     */
    @Transactional
    public BolsaResponse cancelarAlocacao(Long bolsaId) {
        Bolsa bolsa = bolsaRepository.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bolsa não encontrada"));
        if (bolsa.getStatus() != StatusBolsa.ALOCADA) {
            throw new RegraDeNegocioException("Bolsa não está alocada");
        }

        SolicitacaoSangue solicitacao = bolsa.getSolicitacao();
        if (solicitacao != null) {
            solicitacao = solicitacaoSangueRepository.travarPorId(solicitacao.getId()).orElseThrow();
            if (solicitacao.getStatus() == StatusSolicitacao.ATENDIDA) {
                solicitacao.setStatus(StatusSolicitacao.EM_ATENDIMENTO);
                solicitacao.setAtendidaEm(null);
                solicitacaoSangueRepository.save(solicitacao);
            }
        }

        bolsa.setStatus(StatusBolsa.DISPONIVEL);
        bolsa.setSolicitacao(null);
        return paraResponse(bolsaRepository.save(bolsa));
    }

    @Transactional(readOnly = true)
    public List<BolsaResponse> listarBolsasAlocadas(Long solicitacaoId) {
        if (!solicitacaoSangueRepository.existsById(solicitacaoId)) {
            throw new RecursoNaoEncontradoException("Solicitação não encontrada");
        }
        return bolsaRepository.findBySolicitacaoIdOrderByIdAsc(solicitacaoId).stream()
                .map(this::paraResponse)
                .toList();
    }

    public boolean isCompativel(SolicitacaoSangue solicitacao, Bolsa bolsa) {
        return isCompativelPorTipoSanguineo(
                solicitacao.getTipoSanguineo(),
                bolsa.getTipoSanguineo()
        );
    }

    public boolean isCompativelPorTipoSanguineo(TipoSanguineo tipoReceptor, TipoSanguineo tipoDoador) {
        return switch (tipoReceptor) {
            case O_NEGATIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO;
            case O_POSITIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.O_POSITIVO;
            case A_NEGATIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.A_NEGATIVO;
            case A_POSITIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.O_POSITIVO
                    || tipoDoador == TipoSanguineo.A_NEGATIVO
                    || tipoDoador == TipoSanguineo.A_POSITIVO;
            case B_NEGATIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.B_NEGATIVO;
            case B_POSITIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.O_POSITIVO
                    || tipoDoador == TipoSanguineo.B_NEGATIVO
                    || tipoDoador == TipoSanguineo.B_POSITIVO;
            case AB_NEGATIVO -> tipoDoador == TipoSanguineo.O_NEGATIVO
                    || tipoDoador == TipoSanguineo.A_NEGATIVO
                    || tipoDoador == TipoSanguineo.B_NEGATIVO
                    || tipoDoador == TipoSanguineo.AB_NEGATIVO;
            case AB_POSITIVO -> true;
        };
    }

    private BolsaResponse paraResponse(Bolsa bolsa) {
        return new BolsaResponse(bolsa.getId(), bolsa.getIdentificador(), bolsa.getTipoSanguineo(),
                bolsa.getTipoComponente(), bolsa.getDataColeta(), bolsa.getDataValidade(),
                bolsa.getVolume(), bolsa.getStatus(),
                bolsa.getSolicitacao() == null ? null : bolsa.getSolicitacao().getId());
    }
}
