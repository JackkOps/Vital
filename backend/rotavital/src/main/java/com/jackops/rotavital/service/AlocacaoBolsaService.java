package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@Service
public class AlocacaoBolsaService {

    private final BolsaRepository bolsaRepository;
    private final SolicitacaoSangueRepository solicitacaoSangueRepository;

    public AlocacaoBolsaService(BolsaRepository bolsaRepository,
                                SolicitacaoSangueRepository solicitacaoSangueRepository) {
        this.bolsaRepository = bolsaRepository;
        this.solicitacaoSangueRepository = solicitacaoSangueRepository;
    }

    @Transactional
    public RespostaAlocacaoBolsa alocarBolsaCompativel(Long solicitacaoId) {
        SolicitacaoSangue solicitacao = solicitacaoSangueRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        List<Bolsa> bolsasCandidatas = bolsaRepository.findAll().stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DISPONIVEL)
                .filter(bolsa -> !bolsa.getDataValidade().isBefore(LocalDate.now()))
                .filter(bolsa -> isCompativel(solicitacao, bolsa))
                .filter(bolsa -> bolsa.getTipoComponente() == solicitacao.getTipoComponente())
                .sorted(Comparator.comparing(Bolsa::getDataValidade))
                .toList();

        if (bolsasCandidatas.isEmpty()) {
            return new RespostaAlocacaoBolsa(
                    null,
                    false,
                    "Nenhuma bolsa compatível disponível no momento."
            );
        }

        Bolsa bolsaSelecionada = bolsasCandidatas.getFirst();
        bolsaSelecionada.setStatus(StatusBolsa.ALOCADA);
        bolsaRepository.save(bolsaSelecionada);

        return new RespostaAlocacaoBolsa(
                bolsaSelecionada.getId(),
                true,
                "Bolsa alocada com sucesso conforme compatibilidade ABO/Rh e regra FEFO."
        );
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
}
