package com.jackops.rotavital.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.AtualizacaoBolsaRequest;
import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.CadastroBolsaRequest;
import com.jackops.rotavital.estrutura.ListaEstoque;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.repository.BolsaRepository;

import jakarta.annotation.PostConstruct;

@Service
public class BolsaService {
    private final BolsaRepository bolsaRepository;
    private final ListaEstoque listaEstoque;
    private final PilhaHistorico pilhaHistorico;

    public BolsaService(BolsaRepository bolsaRepository, ListaEstoque listaEstoque,
            PilhaHistorico pilhaHistorico) {
        this.bolsaRepository = bolsaRepository;
        this.listaEstoque = listaEstoque;
        this.pilhaHistorico = pilhaHistorico;
    }

    /**
     * Recarrega o estoque persistido para a lista encadeada no inicio da aplicacao.
     */
    @PostConstruct
    public void iniciarLista() {
        recarregarLista();
    }

    @Transactional
    public BolsaResponse cadastrar(CadastroBolsaRequest request) {
        recarregarLista();
        if (listaEstoque.buscar(request.identificador()) != null) {
            throw new RegraDeNegocioException("Bolsa ja existe");
        }
        if (request.dataValidade().isBefore(request.dataColeta())) {
            throw new RegraDeNegocioException("Data de validade nao pode ser anterior a data de coleta");
        }
        Bolsa bolsa = Bolsa.builder()
                .identificador(request.identificador())
                .tipoSanguineo(request.tipoSanguineo())
                .tipoComponente(request.tipoComponente())
                .dataColeta(request.dataColeta())
                .dataValidade(request.dataValidade())
                .volume(request.volume())
                .status(StatusBolsa.DISPONIVEL)
                .build();

        Bolsa salva = bolsaRepository.save(bolsa);
        listaEstoque.inserir(salva);
        pilhaHistorico.empilhar("Cadastro de bolsa " + salva.getIdentificador());
        return paraResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<BolsaResponse> listarEstoque() {
        recarregarLista();
        return Arrays.stream(listaEstoque.listar()).map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public BolsaResponse buscarPorId(Long id) {
        recarregarLista();
        Bolsa bolsa = listaEstoque.buscarPorId(id);
        if (bolsa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bolsa nao encontrada");
        }

        return paraResponse(bolsa);
    }

    @Transactional
    public BolsaResponse atualizar(Long id, AtualizacaoBolsaRequest request) {
        recarregarLista();
        Bolsa bolsa = listaEstoque.buscarPorId(id);
        if (bolsa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bolsa nao encontrada");
        }

        if (request.dataValidade().isBefore(request.dataColeta())) {
            throw new RegraDeNegocioException("Data de validade nao pode ser anterior a data de coleta");
        }

        bolsa.setTipoSanguineo(request.tipoSanguineo());
        bolsa.setTipoComponente(request.tipoComponente());
        bolsa.setDataColeta(request.dataColeta());
        bolsa.setDataValidade(request.dataValidade());
        bolsa.setVolume(request.volume());

        Bolsa atualizada = bolsaRepository.save(bolsa);
        recarregarLista();
        pilhaHistorico.empilhar("Atualizacao de bolsa " + atualizada.getIdentificador());
        return paraResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        recarregarLista();
        Bolsa bolsa = listaEstoque.buscarPorId(id);
        if (bolsa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bolsa nao encontrada");
        }

        if (bolsa.getStatus() != StatusBolsa.DISPONIVEL) {
            throw new RegraDeNegocioException("Somente bolsas disponiveis podem ser excluidas");
        }

        bolsaRepository.delete(bolsa);
        listaEstoque.remover(bolsa.getIdentificador());
        pilhaHistorico.empilhar("Exclusao de bolsa " + bolsa.getIdentificador());
    }

    private void recarregarLista() {
        listaEstoque.limpar();
        bolsaRepository.findAllByOrderByIdAsc().forEach(listaEstoque::inserir);
    }

    private BolsaResponse paraResponse(Bolsa bolsa) {
        return new BolsaResponse(bolsa.getId(), bolsa.getIdentificador(), bolsa.getTipoSanguineo(),
                bolsa.getTipoComponente(), bolsa.getDataColeta(), bolsa.getDataValidade(),
                bolsa.getVolume(), bolsa.getStatus());
    }
}
