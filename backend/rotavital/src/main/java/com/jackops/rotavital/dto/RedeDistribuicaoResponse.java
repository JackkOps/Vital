package com.jackops.rotavital.dto;

import java.util.List;

public record RedeDistribuicaoResponse(List<LocalRedeResponse> locais, List<TrechoResponse> trechos) {
}
