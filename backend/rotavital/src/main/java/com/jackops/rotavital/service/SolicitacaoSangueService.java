package com.jackops.rotavital.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.AtualizacaoSolicitacaoRequest;
import com.jackops.rotavital.dto.CadastroSolicitacaoRequest;
import com.jackops.rotavital.dto.SolicitacaoResponse;
import com.jackops.rotavital.estrutura.FilaRequisicoes;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

import jakarta.annotation.PostConstruct;

@Service
public class SolicitacaoSangueService {
    private final SolicitacaoSangueRepository solicitacaoRepository;
    private final FilaRequisicoes filaRequisicoes;
    private final PilhaHistorico pilhaHistorico;

    public SolicitacaoSangueService(SolicitacaoSangueRepository solicitacaoRepository,
            FilaRequisicoes filaRequisicoes, PilhaHistorico pilhaHistorico) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.filaRequisicoes = filaRequisicoes;
        this.pilhaHistorico = pilhaHistorico;
    }

    /**
     * Recarrega as requisições pendentes para a fila FIFO no início da aplicação.
     */
    @PostConstruct
    public void iniciarFila() {
        recarregarFila();
    }

    @Transactional
    public SolicitacaoResponse cadastrar(CadastroSolicitacaoRequest request) {
        SolicitacaoSangue solicitacao = SolicitacaoSangue.builder()
                .nomeHospital(request.nomeHospital())
                .tipoSanguineo(request.tipoSanguineo())
                .tipoComponente(request.tipoComponente())
                .quantidade(request.quantidade())
                .build();

        solicitacao.setNivelUrgencia(request.nivelUrgencia());

        SolicitacaoSangue salva = solicitacaoRepository.save(solicitacao);
        filaRequisicoes.enfileirar(salva);
        pilhaHistorico.empilhar("Cadastro de solicitação " + salva.getId());

        return paraResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listar() {
        return solicitacaoRepository.findAllByOrderByIdAsc().stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarPorId(Long id) {
        recarregarFila();
        SolicitacaoSangue solicitacaoNaFila = buscarNaFila(id);
        if (solicitacaoNaFila != null) {
            return paraResponse(solicitacaoNaFila);
        }

        SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        return paraResponse(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse atualizar(Long id, AtualizacaoSolicitacaoRequest request) {
        SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        solicitacao.setNomeHospital(request.nomeHospital());
        solicitacao.setTipoSanguineo(request.tipoSanguineo());
        solicitacao.setTipoComponente(request.tipoComponente());
        solicitacao.setQuantidade(request.quantidade());

        SolicitacaoSangue atualizada = solicitacaoRepository.save(solicitacao);
        recarregarFila();
        pilhaHistorico.empilhar("Atualização de solicitação " + atualizada.getId());

        return paraResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        SolicitacaoSangue solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitação não encontrada"));

        if (solicitacao.getStatus() == StatusSolicitacao.PENDENTE) {
            removerDaFila(id);
        }
        solicitacaoRepository.delete(solicitacao);
        pilhaHistorico.empilhar("Exclusão de solicitação " + solicitacao.getId());
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarFila() {
        recarregarFila();
        return Arrays.stream(filaRequisicoes.listar())
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public SolicitacaoResponse chamarProximaDaFila() {
        recarregarFila();
        SolicitacaoSangue solicitacao = filaRequisicoes.desenfileirar();
        if (solicitacao == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fila de solicitações vazia");
        }

        solicitacao.setStatus(StatusSolicitacao.EM_ATENDIMENTO);
        SolicitacaoSangue salva = solicitacaoRepository.save(solicitacao);
        pilhaHistorico.empilhar("Chamada de solicitação " + salva.getId());
        return paraResponse(salva);
    }

    private SolicitacaoSangue buscarNaFila(Long id) {
        return Arrays.stream(filaRequisicoes.listar())
                .filter(solicitacao -> solicitacao.getId() != null && solicitacao.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void removerDaFila(Long id) {
        FilaRequisicoes auxiliar = new FilaRequisicoes();
        SolicitacaoSangue solicitacao = filaRequisicoes.desenfileirar();
        while (solicitacao != null) {
            if (solicitacao.getId() == null || !solicitacao.getId().equals(id)) {
                auxiliar.enfileirar(solicitacao);
            }
            solicitacao = filaRequisicoes.desenfileirar();
        }

        solicitacao = auxiliar.desenfileirar();
        while (solicitacao != null) {
            filaRequisicoes.enfileirar(solicitacao);
            solicitacao = auxiliar.desenfileirar();
        }
    }

    private void recarregarFila() {
        filaRequisicoes.limpar();
        solicitacaoRepository.findByStatusOrderByIdAsc(StatusSolicitacao.PENDENTE)
                .forEach(filaRequisicoes::enfileirar);
    }

    private SolicitacaoResponse paraResponse(SolicitacaoSangue solicitacao) {
        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getNomeHospital(),
                solicitacao.getTipoSanguineo(),
                solicitacao.getTipoComponente(),
                solicitacao.getQuantidade(),
                solicitacao.getNivelUrgencia(),
                solicitacao.getStatus());
    }
}
