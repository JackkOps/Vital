/*
 * Interface web do Rota Vital.
 * Consome a API REST servida pela própria aplicação Spring Boot (mesma origem).
 */
(function () {
    "use strict";

    // ---------- Rótulos dos enums da API ----------

    const TIPOS_SANGUINEOS = {
        A_POSITIVO: "A+", A_NEGATIVO: "A−",
        B_POSITIVO: "B+", B_NEGATIVO: "B−",
        AB_POSITIVO: "AB+", AB_NEGATIVO: "AB−",
        O_POSITIVO: "O+", O_NEGATIVO: "O−"
    };

    const COMPONENTES = {
        HEMACIAS: "Hemácias",
        PLASMA: "Plasma",
        PLAQUETAS: "Plaquetas"
    };

    const URGENCIAS = {
        ELETIVA: "Eletiva",
        URGENTE: "Urgente",
        EMERGENCIA: "Emergência"
    };

    const STATUS = {
        DISPONIVEL: "Disponível",
        ALOCADA: "Alocada",
        INDISPONIVEL: "Indisponível",
        DESCARTADA: "Descartada",
        PENDENTE: "Pendente",
        EM_ATENDIMENTO: "Em atendimento",
        ATENDIDA: "Atendida",
        CANCELADA: "Cancelada"
    };

    const MOTIVOS_BAIXA = {
        VENCIMENTO: "Vencimento"
    };

    const OPCOES = {
        tipoSanguineo: TIPOS_SANGUINEOS,
        tipoComponente: COMPONENTES,
        nivelUrgencia: URGENCIAS
    };

    // ---------- Utilidades ----------

    function el(tag, atributos, ...filhos) {
        const elemento = document.createElement(tag);
        Object.entries(atributos || {}).forEach(([nome, valor]) => {
            if (valor === null || valor === undefined || valor === false) {
                return;
            }
            if (nome === "class") {
                elemento.className = valor;
            } else if (nome.startsWith("on") && typeof valor === "function") {
                elemento.addEventListener(nome.slice(2), valor);
            } else {
                elemento.setAttribute(nome, valor === true ? "" : valor);
            }
        });
        filhos.flat().forEach((filho) => {
            if (filho === null || filho === undefined || filho === false) {
                return;
            }
            elemento.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
        });
        return elemento;
    }

    function hojeIso() {
        const agora = new Date();
        const mes = String(agora.getMonth() + 1).padStart(2, "0");
        const dia = String(agora.getDate()).padStart(2, "0");
        return `${agora.getFullYear()}-${mes}-${dia}`;
    }

    function formatarData(iso) {
        if (!iso) {
            return "—";
        }
        const [ano, mes, dia] = iso.split("-");
        return `${dia}/${mes}/${ano}`;
    }

    function tipo(valor) {
        return el("span", { class: "tipo" }, TIPOS_SANGUINEOS[valor] || valor);
    }

    function componente(valor) {
        return el("span", { class: `componente ${valor}` }, COMPONENTES[valor] || valor);
    }

    function selo(valor, rotulos) {
        return el("span", { class: `selo ${valor}` }, (rotulos || STATUS)[valor] || valor);
    }

    function linhaVazia(tbody, colunas, mensagem) {
        tbody.replaceChildren(el("tr", {}, el("td", { class: "vazio", colspan: colunas }, mensagem)));
    }

    function avisar(mensagem, erro) {
        const caixa = document.getElementById("avisos");
        const aviso = el("div", { class: erro ? "aviso erro" : "aviso" }, mensagem);
        caixa.append(aviso);
        while (caixa.children.length > 3) {
            caixa.firstElementChild.remove();
        }
        setTimeout(() => aviso.remove(), erro ? 7000 : 4500);
    }

    async function api(metodo, caminho, corpo) {
        const opcoes = { method: metodo, headers: { Accept: "application/json" } };
        if (corpo !== undefined) {
            opcoes.headers["Content-Type"] = "application/json";
            opcoes.body = JSON.stringify(corpo);
        }

        let resposta;
        try {
            resposta = await fetch(caminho, opcoes);
        } catch (falha) {
            throw new Error("Não foi possível falar com o servidor. Verifique se a aplicação está rodando.");
        }

        const texto = await resposta.text();
        let dados = null;
        if (texto) {
            try {
                dados = JSON.parse(texto);
            } catch (falha) {
                dados = null;
            }
        }

        if (!resposta.ok) {
            const mensagem = dados && (dados.mensagem || dados.message);
            throw new Error(mensagem || `A operação falhou (erro ${resposta.status}).`);
        }
        return { status: resposta.status, dados };
    }

    async function executar(acao) {
        try {
            await acao();
        } catch (erro) {
            avisar(erro.message, true);
        }
    }

    function preencherOpcoes() {
        document.querySelectorAll("select[data-opcoes]").forEach((select) => {
            const rotulos = OPCOES[select.dataset.opcoes];
            select.replaceChildren(
                el("option", { value: "" }, "Selecione"),
                ...Object.entries(rotulos).map(([valor, rotulo]) => el("option", { value: valor }, rotulo))
            );
        });
    }

    function dadosDoFormulario(form, campos) {
        const dados = {};
        campos.forEach((campo) => {
            const valor = form.elements[campo].value.trim();
            dados[campo] = form.elements[campo].type === "number" ? Number(valor) : valor;
        });
        return dados;
    }

    // ---------- Navegação ----------

    const carregadores = {};

    function mostrarArea(nome) {
        const areas = document.querySelectorAll("main .area");
        const existe = Array.from(areas).some((area) => area.dataset.area === nome);
        const alvo = existe ? nome : "estoque";

        areas.forEach((area) => {
            area.hidden = area.dataset.area !== alvo;
        });
        document.querySelectorAll(".navegacao a").forEach((link) => {
            if (link.dataset.area === alvo) {
                link.setAttribute("aria-current", "page");
            } else {
                link.removeAttribute("aria-current");
            }
        });
        executar(carregadores[alvo]);
    }

    // ---------- Estoque (bolsas) ----------

    const formBolsa = document.getElementById("form-bolsa");

    function limparFormBolsa() {
        formBolsa.reset();
        formBolsa.elements.id.value = "";
        formBolsa.elements.identificador.disabled = false;
        formBolsa.classList.remove("editando");
        document.getElementById("form-bolsa-titulo").textContent = "Cadastrar bolsa";
        document.getElementById("form-bolsa-enviar").textContent = "Cadastrar bolsa";
        document.getElementById("form-bolsa-cancelar").hidden = true;
    }

    function editarBolsa(bolsa) {
        formBolsa.elements.id.value = bolsa.id;
        formBolsa.elements.identificador.value = bolsa.identificador;
        formBolsa.elements.identificador.disabled = true;
        formBolsa.elements.tipoSanguineo.value = bolsa.tipoSanguineo;
        formBolsa.elements.tipoComponente.value = bolsa.tipoComponente;
        formBolsa.elements.dataColeta.value = bolsa.dataColeta;
        formBolsa.elements.dataValidade.value = bolsa.dataValidade;
        formBolsa.elements.volume.value = bolsa.volume;
        formBolsa.classList.add("editando");
        document.getElementById("form-bolsa-titulo").textContent = `Editar bolsa ${bolsa.identificador}`;
        document.getElementById("form-bolsa-enviar").textContent = "Salvar alterações";
        document.getElementById("form-bolsa-cancelar").hidden = false;
        formBolsa.elements.tipoSanguineo.focus();
    }

    async function excluirBolsa(bolsa) {
        if (!confirm(`Excluir a bolsa ${bolsa.identificador}?`)) {
            return;
        }
        await api("DELETE", `/api/bolsas/${bolsa.id}`);
        avisar(`Bolsa ${bolsa.identificador} excluída.`);
        await carregadores.estoque();
    }

    async function darBaixa(bolsa) {
        if (!confirm(`Dar baixa por vencimento na bolsa ${bolsa.identificador}?`)) {
            return;
        }
        await api("POST", `/api/validade/bolsas/${bolsa.id}/baixa`);
        avisar(`Baixa registrada para a bolsa ${bolsa.identificador}.`);
        await carregadores.estoque();
    }

    carregadores.estoque = async function () {
        const tbody = document.getElementById("tabela-bolsas");
        const { dados: bolsas } = await api("GET", "/api/bolsas");
        if (!bolsas.length) {
            linhaVazia(tbody, 8, "Nenhuma bolsa cadastrada. Use o formulário acima para registrar a primeira.");
            return;
        }

        const hoje = hojeIso();
        tbody.replaceChildren(...bolsas.map((bolsa) => {
            const disponivel = bolsa.status === "DISPONIVEL";
            const vencida = bolsa.dataValidade < hoje;
            return el("tr", {},
                el("td", {}, bolsa.identificador),
                el("td", {}, tipo(bolsa.tipoSanguineo)),
                el("td", {}, componente(bolsa.tipoComponente)),
                el("td", {}, formatarData(bolsa.dataColeta)),
                el("td", { class: vencida && disponivel ? "dias-criticos" : null }, formatarData(bolsa.dataValidade)),
                el("td", { class: "num" }, `${bolsa.volume} mL`),
                el("td", {}, selo(bolsa.status)),
                el("td", { class: "acoes" },
                    disponivel && vencida
                        ? el("button", { type: "button", class: "botao pequeno perigo",
                            onclick: () => executar(() => darBaixa(bolsa)) }, "Dar baixa")
                        : null,
                    el("button", { type: "button", class: "botao pequeno",
                        onclick: () => editarBolsa(bolsa) }, "Editar"),
                    el("button", { type: "button", class: "botao pequeno", disabled: !disponivel,
                        title: disponivel ? null : "Somente bolsas disponíveis podem ser excluídas",
                        onclick: () => executar(() => excluirBolsa(bolsa)) }, "Excluir")
                )
            );
        }));
    };

    formBolsa.addEventListener("submit", (evento) => {
        evento.preventDefault();
        executar(async () => {
            const id = formBolsa.elements.id.value;
            const campos = ["tipoSanguineo", "tipoComponente", "dataColeta", "dataValidade", "volume"];
            if (id) {
                const { dados } = await api("PUT", `/api/bolsas/${id}`, dadosDoFormulario(formBolsa, campos));
                avisar(`Bolsa ${dados.identificador} atualizada.`);
            } else {
                const { dados } = await api("POST", "/api/bolsas",
                    dadosDoFormulario(formBolsa, ["identificador", ...campos]));
                avisar(`Bolsa ${dados.identificador} cadastrada como disponível.`);
            }
            limparFormBolsa();
            await carregadores.estoque();
        });
    });

    document.getElementById("form-bolsa-cancelar").addEventListener("click", limparFormBolsa);

    // ---------- Solicitações, fila e alocação ----------

    const formSolicitacao = document.getElementById("form-solicitacao");

    function limparFormSolicitacao() {
        formSolicitacao.reset();
        formSolicitacao.elements.id.value = "";
        formSolicitacao.elements.nivelUrgencia.disabled = false;
        document.getElementById("campo-urgencia").hidden = false;
        formSolicitacao.classList.remove("editando");
        document.getElementById("form-solicitacao-titulo").textContent = "Registrar solicitação";
        document.getElementById("form-solicitacao-enviar").textContent = "Registrar solicitação";
        document.getElementById("form-solicitacao-cancelar").hidden = true;
    }

    function editarSolicitacao(solicitacao) {
        formSolicitacao.elements.id.value = solicitacao.id;
        formSolicitacao.elements.nomeHospital.value = solicitacao.nomeHospital;
        formSolicitacao.elements.tipoSanguineo.value = solicitacao.tipoSanguineo;
        formSolicitacao.elements.tipoComponente.value = solicitacao.tipoComponente;
        formSolicitacao.elements.quantidade.value = solicitacao.quantidade;
        // A API não altera a urgência na atualização; o campo fica oculto durante a edição.
        formSolicitacao.elements.nivelUrgencia.disabled = true;
        document.getElementById("campo-urgencia").hidden = true;
        formSolicitacao.classList.add("editando");
        document.getElementById("form-solicitacao-titulo").textContent = `Editar solicitação nº ${solicitacao.id}`;
        document.getElementById("form-solicitacao-enviar").textContent = "Salvar alterações";
        document.getElementById("form-solicitacao-cancelar").hidden = false;
        formSolicitacao.elements.nomeHospital.focus();
    }

    async function excluirSolicitacao(solicitacao) {
        if (!confirm(`Excluir a solicitação nº ${solicitacao.id} de ${solicitacao.nomeHospital}?`)) {
            return;
        }
        await api("DELETE", `/api/solicitacoes/${solicitacao.id}`);
        avisar(`Solicitação nº ${solicitacao.id} excluída.`);
        await carregadores.solicitacoes();
    }

    async function alocarBolsa(solicitacao) {
        const { status, dados } = await api("POST", `/api/alocacoes/solicitacoes/${solicitacao.id}/alocar`);
        if (status === 200 && dados.alocada) {
            let descricao = `nº ${dados.bolsaId}`;
            try {
                const { dados: bolsa } = await api("GET", `/api/bolsas/${dados.bolsaId}`);
                descricao = bolsa.identificador;
            } catch (erro) {
                // Mantém a descrição pelo número quando a consulta da bolsa falhar.
            }
            avisar(`Bolsa ${descricao} alocada para a solicitação nº ${solicitacao.id}.`);
        } else {
            avisar(dados && dados.mensagem ? dados.mensagem : "Nenhuma bolsa compatível disponível no momento.", true);
        }
        await carregadores.solicitacoes();
    }

    function renderizarFila(fila) {
        const lista = document.getElementById("lista-fila");
        if (!fila.length) {
            lista.replaceChildren(el("li", { class: "fila-vazia" }, "Nenhuma solicitação pendente."));
            return;
        }
        lista.replaceChildren(...fila.map((solicitacao, indice) =>
            el("li", {},
                el("p", { class: "posicao" }, indice === 0 ? "Próxima a ser atendida" : `${indice + 1}ª na fila`),
                el("p", { class: "hospital" }, solicitacao.nomeHospital),
                el("p", { class: "detalhes" },
                    tipo(solicitacao.tipoSanguineo),
                    componente(solicitacao.tipoComponente),
                    `× ${solicitacao.quantidade}`),
                el("p", { class: "detalhes" }, selo(solicitacao.nivelUrgencia, URGENCIAS))
            )
        ));
    }

    function renderizarSolicitacoes(solicitacoes) {
        const tbody = document.getElementById("tabela-solicitacoes");
        if (!solicitacoes.length) {
            linhaVazia(tbody, 8, "Nenhuma solicitação registrada. Use o formulário acima para registrar a primeira.");
            return;
        }
        tbody.replaceChildren(...solicitacoes.map((solicitacao) => {
            const aberta = solicitacao.status === "PENDENTE" || solicitacao.status === "EM_ATENDIMENTO";
            return el("tr", {},
                el("td", { class: "num" }, solicitacao.id),
                el("td", {}, solicitacao.nomeHospital),
                el("td", {}, tipo(solicitacao.tipoSanguineo)),
                el("td", {}, componente(solicitacao.tipoComponente)),
                el("td", { class: "num" }, solicitacao.quantidade),
                el("td", {}, selo(solicitacao.nivelUrgencia, URGENCIAS)),
                el("td", {}, selo(solicitacao.status)),
                el("td", { class: "acoes" },
                    aberta
                        ? el("button", { type: "button", class: "botao pequeno principal",
                            onclick: () => executar(() => alocarBolsa(solicitacao)) }, "Alocar bolsa")
                        : null,
                    el("button", { type: "button", class: "botao pequeno",
                        onclick: () => editarSolicitacao(solicitacao) }, "Editar"),
                    el("button", { type: "button", class: "botao pequeno",
                        onclick: () => executar(() => excluirSolicitacao(solicitacao)) }, "Excluir")
                )
            );
        }));
    }

    carregadores.solicitacoes = async function () {
        const [fila, todas] = await Promise.all([
            api("GET", "/api/solicitacoes/fila"),
            api("GET", "/api/solicitacoes")
        ]);
        renderizarFila(fila.dados);
        renderizarSolicitacoes(todas.dados);
    };

    formSolicitacao.addEventListener("submit", (evento) => {
        evento.preventDefault();
        executar(async () => {
            const id = formSolicitacao.elements.id.value;
            const campos = ["nomeHospital", "tipoSanguineo", "tipoComponente", "quantidade"];
            if (id) {
                const { dados } = await api("PUT", `/api/solicitacoes/${id}`,
                    dadosDoFormulario(formSolicitacao, campos));
                avisar(`Solicitação nº ${dados.id} atualizada.`);
            } else {
                const { dados } = await api("POST", "/api/solicitacoes",
                    dadosDoFormulario(formSolicitacao, [...campos, "nivelUrgencia"]));
                avisar(`Solicitação nº ${dados.id} registrada e colocada na fila.`);
            }
            limparFormSolicitacao();
            await carregadores.solicitacoes();
        });
    });

    document.getElementById("form-solicitacao-cancelar").addEventListener("click", limparFormSolicitacao);

    document.getElementById("chamar-proxima").addEventListener("click", () => executar(async () => {
        const { dados } = await api("POST", "/api/solicitacoes/fila/proxima");
        avisar(`Solicitação nº ${dados.id} de ${dados.nomeHospital} em atendimento.`);
        await carregadores.solicitacoes();
    }));

    // ---------- Validade ----------

    function renderizarBaixas(baixas) {
        const tbody = document.getElementById("tabela-baixas");
        if (!baixas.length) {
            linhaVazia(tbody, 5, "Nenhuma baixa registrada.");
            return;
        }
        tbody.replaceChildren(...baixas.map((baixa) =>
            el("tr", {},
                el("td", { class: "num" }, baixa.id),
                el("td", {}, baixa.identificadorBolsa),
                el("td", {}, MOTIVOS_BAIXA[baixa.motivo] || baixa.motivo),
                el("td", {}, formatarData(baixa.dataBaixa)),
                el("td", {}, baixa.automatica ? "Automática" : "Manual")
            )
        ));
    }

    function renderizarAlertas(alertas) {
        const tbody = document.getElementById("tabela-alertas");
        if (!alertas.length) {
            linhaVazia(tbody, 5, "Nenhuma bolsa disponível está próxima do vencimento.");
            return;
        }
        tbody.replaceChildren(...alertas.map((alerta) =>
            el("tr", {},
                el("td", {}, alerta.identificador),
                el("td", {}, tipo(alerta.tipoSanguineo)),
                el("td", {}, componente(alerta.tipoComponente)),
                el("td", {}, formatarData(alerta.dataValidade)),
                el("td", { class: alerta.diasParaVencer <= 1 ? "num dias-criticos" : "num" },
                    alerta.diasParaVencer === 0 ? "Vence hoje" : alerta.diasParaVencer)
            )
        ));
    }

    carregadores.validade = async function () {
        const [alertas, baixas] = await Promise.all([
            api("GET", "/api/validade/alertas"),
            api("GET", "/api/validade/baixas")
        ]);
        renderizarAlertas(alertas.dados);
        renderizarBaixas(baixas.dados);
    };

    document.getElementById("verificar-validade").addEventListener("click", () => executar(async () => {
        const { dados } = await api("POST", "/api/validade/verificar");
        const resultado = document.getElementById("resultado-verificacao");
        const quantidade = dados.baixasRealizadas.length;
        const resumo = quantidade === 0
            ? "Nenhuma bolsa vencida encontrada."
            : `${quantidade} ${quantidade === 1 ? "bolsa recebeu" : "bolsas receberam"} baixa por vencimento:`;
        resultado.replaceChildren(el("div", { class: "resultado" },
            el("p", {}, `Verificação de ${formatarData(dados.dataReferencia)}. ${resumo}`),
            quantidade
                ? el("ul", {}, dados.baixasRealizadas.map((baixa) => el("li", {}, baixa.identificadorBolsa)))
                : null
        ));
        renderizarAlertas(dados.alertas);
        const { dados: baixas } = await api("GET", "/api/validade/baixas");
        renderizarBaixas(baixas);
    }));

    // ---------- Indicadores ----------

    function renderizarMatriz(estoque) {
        const tabela = document.getElementById("matriz-estoque");
        const tipos = Object.keys(TIPOS_SANGUINEOS);
        const contagem = {};
        estoque.forEach((item) => {
            contagem[`${item.tipoComponente}|${item.tipoSanguineo}`] = item.quantidadeBolsasDisponiveis;
        });

        const cabecalho = el("thead", {}, el("tr", {},
            el("th", { scope: "col" }, el("span", { class: "visualmente-oculto" }, "Componente")),
            tipos.map((t) => el("th", { scope: "col" }, TIPOS_SANGUINEOS[t])),
            el("th", { scope: "col", class: "total" }, "Total")
        ));

        const corpo = el("tbody", {}, Object.keys(COMPONENTES).map((comp) => {
            let total = 0;
            const celulas = tipos.map((t) => {
                const valor = contagem[`${comp}|${t}`] || 0;
                total += valor;
                return el("td", { class: valor ? null : "zero",
                    "aria-label": `${COMPONENTES[comp]} ${TIPOS_SANGUINEOS[t]}: ${valor}` }, valor);
            });
            return el("tr", { class: comp },
                el("th", { scope: "row" }, componente(comp)),
                celulas,
                el("td", { class: total ? "total" : "total zero" }, total));
        }));

        tabela.replaceChildren(cabecalho, corpo);
    }

    function renderizarDemanda(demanda) {
        const tbody = document.getElementById("tabela-demanda");
        if (!demanda.length) {
            linhaVazia(tbody, 4, "Nenhuma solicitação pendente ou em atendimento.");
            return;
        }
        tbody.replaceChildren(...demanda.map((item) =>
            el("tr", {},
                el("td", {}, item.nomeHospital),
                el("td", {}, componente(item.tipoComponente)),
                el("td", {}, tipo(item.tipoSanguineo)),
                el("td", { class: "num" }, item.quantidadeBolsasSolicitadasEmAberto)
            )
        ));
    }

    carregadores.indicadores = async function () {
        const { dados } = await api("GET", "/api/indicadores");
        document.getElementById("indicadores-referencia").textContent =
            `Calculados a partir dos dados operacionais em ${formatarData(dados.dataReferencia)}.`;
        document.getElementById("descricao-estoque").textContent = dados.descricaoEstoque;
        document.getElementById("descricao-demanda").textContent = dados.descricaoDemanda;
        renderizarMatriz(dados.estoqueDisponivel);
        renderizarDemanda(dados.demandaEmAberto);
    };

    // ---------- Histórico ----------

    carregadores.historico = async function () {
        const lista = document.getElementById("lista-historico");
        const { dados: operacoes } = await api("GET", "/api/historico");
        if (!operacoes.length) {
            lista.replaceChildren(el("li", {}, "Nenhuma operação registrada desde que a aplicação iniciou."));
            return;
        }
        lista.replaceChildren(...operacoes.map((item) => el("li", {}, item.operacao)));
    };

    document.getElementById("remover-ultima").addEventListener("click", () => executar(async () => {
        if (!confirm("Remover a última operação do histórico?")) {
            return;
        }
        const { dados } = await api("DELETE", "/api/historico/ultima");
        avisar(`Operação removida: ${dados.operacao}`);
        await carregadores.historico();
    }));

    // ---------- Inicialização ----------

    document.querySelectorAll("[data-recarregar]").forEach((botao) => {
        botao.addEventListener("click", () => executar(carregadores[botao.dataset.recarregar]));
    });

    document.getElementById("data-hoje").textContent = new Date().toLocaleDateString("pt-BR", {
        weekday: "long", day: "numeric", month: "long", year: "numeric"
    });

    preencherOpcoes();
    window.addEventListener("hashchange", () => mostrarArea(location.hash.slice(1)));
    mostrarArea(location.hash.slice(1));
})();
