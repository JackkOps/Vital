package com.jackops.rotavital.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.CadastroSolicitacaoRequest;
import com.jackops.rotavital.dto.SolicitacaoResponse;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@Service
public class SolicitacaoSangueService {
    private final SolicitacaoSangueRepository solicitacaoRepository;

    public SolicitacaoSangueService(SolicitacaoSangueRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public SolicitacaoResponse cadastrar(CadastroSolicitacaoRequest request) {
        SolicitacaoSangue solicitacao = solicitacaoRepository.save(SolicitacaoSangue.builder()
                .nomeHospital(request.nomeHospital())
                .tipoSanguineo(request.tipoSanguineo())
                .tipoComponente(request.tipoComponente())
                .quantidade(request.quantidade())
                .build());
        return new SolicitacaoResponse(solicitacao.getId(), solicitacao.getNomeHospital(),
                solicitacao.getTipoSanguineo(), solicitacao.getTipoComponente(), solicitacao.getQuantidade());
    }
}
