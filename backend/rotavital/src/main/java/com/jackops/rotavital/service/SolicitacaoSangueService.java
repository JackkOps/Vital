package com.jackops.rotavital.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.CadastroSolicitacaoRequest;
import com.jackops.rotavital.dto.SolicitacaoResponse;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;
import com.jackops.rotavital.dto.AtualizacaoSolicitacaoRequest;
import com.jackops.rotavital.model.enums.NivelUrgencia;
import com.jackops.rotavital.model.enums.StatusSolicitacao;

import java.util.List;
import java.util.Comparator;

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

        solicitacao.setNivelUrgencia(request.nivelUrgencia());

        SolicitacaoSangue salva = solicitacaoRepository.save(solicitacao);

        return new SolicitacaoResponse(
            solicitacao.getId(),
            solicitacao.getNomeHospital(),
            solicitacao.getTipoSanguineo(),
            solicitacao.getTipoComponente(),
            solicitacao.getQuantidade(),
            solicitacao.getNivelUrgencia(),
            solicitacao.getStatus());
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listar(){
        return solicitacaoRepository.findAll().stream()
            .map(solicitacao -> new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getNomeHospital(),
                solicitacao.getTipoSanguineo(),
                solicitacao.getTipoComponente(),
                solicitacao.getQuantidade(),
                solicitacao.getNivelUrgencia(),
                solicitacao.getStatus()))
            .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarPorId(Long id){
        SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        return new SolicitacaoResponse(
            solicitacao.getId(),
            solicitacao.getNomeHospital(),
            solicitacao.getTipoSanguineo(),
            solicitacao.getTipoComponente(),
            solicitacao.getQuantidade(),
            solicitacao.getNivelUrgencia(),
            solicitacao.getStatus());
    }  

    @Transactional
public SolicitacaoResponse atualizar(
        Long id, AtualizacaoSolicitacaoRequest request) {

    SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

    solicitacao.setNomeHospital(request.nomeHospital());
    solicitacao.setTipoSanguineo(request.tipoSanguineo());
    solicitacao.setTipoComponente(request.tipoComponente());
    solicitacao.setQuantidade(request.quantidade());

    SolicitacaoSangue atualizada = solicitacaoRepository.save(solicitacao);

    return new SolicitacaoResponse(
            atualizada.getId(),
            atualizada.getNomeHospital(),
            atualizada.getTipoSanguineo(),
            atualizada.getTipoComponente(),
            atualizada.getQuantidade(),
            solicitacao.getNivelUrgencia(),
            solicitacao.getStatus());
    }

    @Transactional
    public void excluir(Long id){
        SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Solicitacao não encontrada"));

        solicitacaoRepository.delete(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarFila(){
        return solicitacaoRepository.findAll().stream()
            .filter(solicitacao ->
                solicitacao.getStatus() == StatusSolicitacao.PENDENTE)
            .sorted(Comparator
                .comparingInt((SolicitacaoSangue solicitacao) ->
                    prioridade(solicitacao.getNivelUrgencia()))
                .thenComparing(SolicitacaoSangue::getId))
            .map(solicitacao -> new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getNomeHospital(),
                solicitacao.getTipoSanguineo(),
                solicitacao.getTipoComponente(),
                solicitacao.getQuantidade(),
                solicitacao.getNivelUrgencia(),
                solicitacao.getStatus()))
            .toList();
    }

    private int prioridade(NivelUrgencia nivelUrgencia){
        return switch(nivelUrgencia){
            case EMERGENCIA -> 0;
            case URGENTE -> 1;
            case ELETIVA -> 2;
        };
    }
}
