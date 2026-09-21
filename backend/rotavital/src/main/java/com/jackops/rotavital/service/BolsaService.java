package com.jackops.rotavital.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private BolsaResponse paraResponse(Bolsa bolsa) {
        return new BolsaResponse(bolsa.getId(), bolsa.getIdentificador(), bolsa.getTipoSanguineo(),
                bolsa.getTipoComponente(), bolsa.getDataColeta(), bolsa.getDataValidade(),
                bolsa.getVolume(), bolsa.getStatus());
    }
}
