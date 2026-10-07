package com.jackops.rotavital.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.AtualizacaoBolsaRequest;
import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.CadastroBolsaRequest;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.repository.BolsaRepository;

@Service
public class BolsaService {
    private final BolsaRepository bolsaRepository;

    public BolsaService(BolsaRepository bolsaRepository) {
        this.bolsaRepository = bolsaRepository;
    }

    @Transactional
    public BolsaResponse cadastrar(CadastroBolsaRequest request) {
        if (bolsaRepository.existsByIdentificador(request.identificador())) {
            throw new RegraDeNegocioException("Bolsa já existe");
        }
        if (request.dataValidade().isBefore(request.dataColeta())) {
            throw new RegraDeNegocioException("Data de validade não pode ser anterior à data de coleta");
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
        return paraResponse(bolsaRepository.save(bolsa));
    }

    @Transactional(readOnly = true)
    public List<BolsaResponse> listarEstoque() {
        return bolsaRepository.findAll().stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public BolsaResponse buscarPorId(Long id){
        Bolsa bolsa = bolsaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Bolsa não encontrada"));

        return paraResponse(bolsa);
    }

    @Transactional
    public BolsaResponse atualizar(Long id, AtualizacaoBolsaRequest request){
        Bolsa bolsa = bolsaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Bolsa não encontrada"));

        if (request.dataValidade().isBefore(request.dataColeta())){
            throw new RegraDeNegocioException(
                "Data de validade não pode ser anterior à data de coleta");
        }

        bolsa.setTipoSanguineo(request.tipoSanguineo());
        bolsa.setTipoComponente(request.tipoComponente());
        bolsa.setDataColeta(request.dataColeta());
        bolsa.setDataValidade(request.dataValidade());
        bolsa.setVolume(request.volume());

        return paraResponse(bolsaRepository.save(bolsa));
        
    }

    @Transactional
    public void excluir(Long id){
        Bolsa bolsa = bolsaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Bolsa não encontrada"));
        
        if (bolsa.getStatus() != StatusBolsa.DISPONIVEL){
            throw new RegraDeNegocioException(
                "Somente bolsas disponíveis podem ser excluidas");
        }

        bolsaRepository.delete(bolsa);
    }

    private BolsaResponse paraResponse(Bolsa bolsa) {
        return new BolsaResponse(bolsa.getId(), bolsa.getIdentificador(), bolsa.getTipoSanguineo(),
                bolsa.getTipoComponente(), bolsa.getDataColeta(), bolsa.getDataValidade(),
                bolsa.getVolume(), bolsa.getStatus());
    }
}
