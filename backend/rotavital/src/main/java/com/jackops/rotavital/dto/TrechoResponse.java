package com.jackops.rotavital.dto;

public record TrechoResponse(Long id, Long origemId, Long destinoId, Integer tempoMinutos, Double distanciaKm) {
}
