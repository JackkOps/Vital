package com.jackops.rotavital.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints de diagnóstico da observabilidade — NÃO fazem parte do domínio.
 *
 * <p>Existem para que o painel de taxa de erro 5xx tenha dados. Sem uma falha de
 * servidor real, a consulta
 * {@code rate(http_server_requests_seconds_count{status=~"5.."}[5m])} fica
 * permanentemente em zero, e um painel sem dados não demonstra nada.
 *
 * <p>Erro de cliente (4xx) não serve para isso: 404 significa "você pediu algo que
 * não existe", enquanto 5xx significa "eu falhei". São painéis que respondem a
 * perguntas diferentes, e o de 5xx é o que indica problema da aplicação.
 *
 * <p>Fica desligado por padrão e só é registrado quando
 * {@code rotavital.diagnostico.habilitado=true}. Em produção real, basta não
 * definir a propriedade e a classe nem entra no contexto do Spring.
 */
@RestController
@RequestMapping("/api/diagnostico")
@ConditionalOnProperty(name = "rotavital.diagnostico.habilitado", havingValue = "true")
public class DiagnosticoController {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoController.class);

    /**
     * Sempre devolve HTTP 500, alimentando o painel de erros de servidor.
     *
     * <p>O {@code log.error} antes de lançar é deliberado: o Spring trata
     * {@link ResponseStatusException} como resposta de erro esperada e NÃO emite log
     * de nível ERROR por conta própria. Sem esta linha, a consulta
     * {@code {service="api", level="ERROR"}} no Loki não retorna nada e a navegação
     * log -> trace (R6.3) fica sem ponto de partida.
     *
     * <p>A linha de log carrega o {@code traceId} automaticamente, por causa do
     * padrão de correlação configurado em {@code application.properties}.
     */
    @GetMapping("/erro")
    public void erro() {
        log.error("Falha simulada no endpoint de diagnostico: demonstracao de erro 5xx "
                + "correlacionado com trace");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "erro simulado para demonstracao do painel de taxa de erro 5xx");
    }

    /** Devolve 503, simulando dependência indisponível. */
    @GetMapping("/indisponivel")
    public void indisponivel() {
        log.warn("Dependencia externa reportada como indisponivel (simulado)");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "dependencia indisponivel (simulado)");
    }

    /** Confirma que os endpoints de diagnóstico estão ativos. */
    @GetMapping
    public Map<String, Object> informacao() {
        return Map.of(
                "finalidade", "gerar falhas controladas para validar paineis de erro",
                "endpoints", Map.of(
                        "GET /api/diagnostico/erro", "HTTP 500",
                        "GET /api/diagnostico/indisponivel", "HTTP 503"),
                "desativar", "remova rotavital.diagnostico.habilitado do application.properties");
    }
}
