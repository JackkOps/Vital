package com.jackops.rotavital.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.OperacaoHistoricoResponse;
import com.jackops.rotavital.estrutura.PilhaHistorico;

@Service
public class HistoricoService {
    private final PilhaHistorico pilhaHistorico;

    public HistoricoService(PilhaHistorico pilhaHistorico) {
        this.pilhaHistorico = pilhaHistorico;
    }

    public List<OperacaoHistoricoResponse> listar() {
        List<OperacaoHistoricoResponse> operacoes = new ArrayList<>();
        PilhaHistorico auxiliar = new PilhaHistorico();

        String operacao = pilhaHistorico.desempilhar();
        while (operacao != null) {
            operacoes.add(new OperacaoHistoricoResponse(operacao));
            auxiliar.empilhar(operacao);
            operacao = pilhaHistorico.desempilhar();
        }

        operacao = auxiliar.desempilhar();
        while (operacao != null) {
            pilhaHistorico.empilhar(operacao);
            operacao = auxiliar.desempilhar();
        }

        return operacoes;
    }

    public OperacaoHistoricoResponse removerUltima() {
        String operacao = pilhaHistorico.desempilhar();
        if (operacao == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Histórico vazio");
        }
        return new OperacaoHistoricoResponse(operacao);
    }
}
