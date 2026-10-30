/* Rota Vital — interface web
 * Consome a API REST do backend e desenha as cinco áreas do protótipo
 * (Estoque, Solicitações, Validade, Indicadores e Histórico).
 */
(function () {
    'use strict';

    const API = {
        bolsas: '/api/bolsas',
        solicitacoes: '/api/solicitacoes',
        fila: '/api/solicitacoes/fila',
        chamarProxima: '/api/solicitacoes/fila/proxima',
        alertas: '/api/validade/alertas',
        verificarValidade: '/api/validade/verificar',
        baixas: '/api/validade/baixas',
        indicadores: '/api/indicadores',
        historico: '/api/historico',
        removerUltimaOperacao: '/api/historico/ultima',
        alocar: (solicitacaoId) => `/api/alocacoes/solicitacoes/${solicitacaoId}/alocar`,
        baixaManual: (bolsaId) => `/api/validade/bolsas/${bolsaId}/baixa`
    };

    /* ---------- Domínio ---------- */

    const TIPOS_SANGUINEOS = {
        A_POSITIVO: { abo: 'A', rh: '+' },
        A_NEGATIVO: { abo: 'A', rh: '-' },
        B_POSITIVO: { abo: 'B', rh: '+' },
        B_NEGATIVO: { abo: 'B', rh: '-' },
        AB_POSITIVO: { abo: 'AB', rh: '+' },
        AB_NEGATIVO: { abo: 'AB', rh: '-' },
        O_POSITIVO: { abo: 'O', rh: '+' },
        O_NEGATIVO: { abo: 'O', rh: '-' }
    };
    Object.values(TIPOS_SANGUINEOS).forEach((tipo) => {
        tipo.rotulo = tipo.abo + (tipo.rh === '+' ? '+' : '−');
        tipo.extenso = `${tipo.abo} ${tipo.rh === '+' ? 'positivo' : 'negativo'}`;
    });

    // Janelas de alerta iguais às de TipoComponente.diasAlertaVencimento no backend.
    const COMPONENTES = {
        HEMACIAS: { rotulo: 'Hemácias', minusculo: 'hemácias', classe: 'hem', janela: 7 },
        PLASMA: { rotulo: 'Plasma', minusculo: 'plasma', classe: 'pla', janela: 30 },
        PLAQUETAS: { rotulo: 'Plaquetas', minusculo: 'plaquetas', classe: 'plq', janela: 2 },
        SANGUE_TOTAL: { rotulo: 'Sangue total', minusculo: 'sangue total', classe: 'st', janela: 7 }
    };

    const STATUS_BOLSA = {
        DISPONIVEL: { rotulo: 'Disponível', tom: 'ok', icone: 'check' },
        ALOCADA: { rotulo: 'Alocada', tom: 'in', icone: 'link' },
        INDISPONIVEL: { rotulo: 'Indisponível', tom: 'ne', icone: 'proibido' },
        DESCARTADA: { rotulo: 'Descartada', tom: 'pe', icone: 'x' }
    };

    const STATUS_SOLICITACAO = {
        PENDENTE: { rotulo: 'Pendente', tom: 'at', icone: 'relogio' },
        EM_ATENDIMENTO: { rotulo: 'Em atendimento', tom: 'in', icone: 'play' },
        ATENDIDA: { rotulo: 'Atendida', tom: 'ok', icone: 'check' },
        CANCELADA: { rotulo: 'Cancelada', tom: 'ne', icone: 'x' }
    };

    const URGENCIAS = {
        ELETIVA: { rotulo: 'Eletiva', curto: 'Eletiva', tom: 'ne', icone: 'calendario' },
        URGENTE: { rotulo: 'Urgente', curto: 'Urgente', tom: 'at', icone: 'relogio' },
        EMERGENCIA: { rotulo: 'Emergência', curto: 'Emerg.', tom: 'pe', icone: 'alerta' }
    };

    // Mesma tabela de AlocacaoBolsaService.isCompativelPorTipoSanguineo: receptor -> doadores aceitos.
    const DOADORES_COMPATIVEIS = {
        O_NEGATIVO: ['O_NEGATIVO'],
        O_POSITIVO: ['O_NEGATIVO', 'O_POSITIVO'],
        A_NEGATIVO: ['O_NEGATIVO', 'A_NEGATIVO'],
        A_POSITIVO: ['O_NEGATIVO', 'O_POSITIVO', 'A_NEGATIVO', 'A_POSITIVO'],
        B_NEGATIVO: ['O_NEGATIVO', 'B_NEGATIVO'],
        B_POSITIVO: ['O_NEGATIVO', 'O_POSITIVO', 'B_NEGATIVO', 'B_POSITIVO'],
        AB_NEGATIVO: ['O_NEGATIVO', 'A_NEGATIVO', 'B_NEGATIVO', 'AB_NEGATIVO'],
        AB_POSITIVO: Object.keys(TIPOS_SANGUINEOS)
    };

    const PAGINAS = ['estoque', 'solicitacoes', 'validade', 'indicadores', 'historico'];

    /* ---------- Ícones (traço 24x24) ---------- */

    const ICONES = {
        gota: '<path d="M12 2.5s-6.5 7.2-6.5 12a6.5 6.5 0 0 0 13 0c0-4.8-6.5-12-6.5-12z"/>',
        estoque: '<path d="M21 8 12 3 3 8l9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/>',
        prancheta: '<rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>',
        calendario: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
        grafico: '<path d="M3 3v18h18"/><path d="M8 17v-5M13 17V8M18 17v-9"/>',
        historico: '<path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5"/><path d="M12 7v5l3 2"/>',
        check: '<path d="M20 6 9 17l-5-5"/>',
        x: '<path d="M18 6 6 18M6 6l12 12"/>',
        link: '<path d="M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7l-1.7 1.7"/><path d="M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7l1.7-1.7"/>',
        proibido: '<circle cx="12" cy="12" r="9"/><path d="m5.7 5.7 12.6 12.6"/>',
        alerta: '<path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/>',
        erro: '<circle cx="12" cy="12" r="9"/><path d="M12 8v4M12 16h.01"/>',
        relogio: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
        play: '<path d="M7 5v14l11-7z" fill="currentColor"/>',
        lapis: '<path d="M17 3a2.8 2.8 0 0 1 4 4L7.5 20.5 2 22l1.5-5.5z"/>',
        lixeira: '<path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6"/>',
        mais: '<path d="M12 5v14M5 12h14"/>',
        seta: '<path d="M5 12h14M13 6l6 6-6 6"/>',
        seta_baixo: '<path d="M12 5v14M6 13l6 6 6-6"/>',
        recarregar: '<path d="M21 12a9 9 0 1 1-3-6.7L21 8"/><path d="M21 3v5h-5"/>',
        info: '<circle cx="12" cy="12" r="9"/><path d="M12 16v-4M12 8h.01"/>',
        pessoa: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
        engrenagem: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>'
    };

    function icone(nome) {
        return `<svg class="icone" viewBox="0 0 24 24" aria-hidden="true" focusable="false">${ICONES[nome] || ''}</svg>`;
    }

    /* ---------- Utilitários ---------- */

    function esc(valor) {
        return String(valor ?? '').replace(/[&<>"']/g, (c) => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[c]);
    }

    function plural(n, singular, pluralTexto) {
        return `${n} ${n === 1 ? singular : pluralTexto}`;
    }

    function hoje() {
        const data = new Date();
        data.setHours(0, 0, 0, 0);
        return data;
    }

    function hojeIso() {
        const d = hoje();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    function lerData(iso) {
        const [ano, mes, dia] = iso.split('-').map(Number);
        return new Date(ano, mes - 1, dia);
    }

    function formatarData(iso) {
        if (!iso) {
            return '—';
        }
        const [ano, mes, dia] = iso.split('-');
        return `${dia}/${mes}/${ano}`;
    }

    function diasAte(iso) {
        return Math.round((lerData(iso) - hoje()) / 86400000);
    }

    function hora(data) {
        return data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }

    const DIAS_SEMANA = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];
    const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
    const MESES_EXTENSO = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho',
        'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];

    function codigoSolicitacao(id) {
        return `SOL-${String(id).padStart(4, '0')}`;
    }

    function tipoHtml(valor, tamanho = 'md') {
        const tipo = TIPOS_SANGUINEOS[valor];
        if (!tipo) {
            return '—';
        }
        return `<span class="tipo tipo-${tamanho}" role="img" aria-label="Tipo ${tipo.extenso}">`
            + `<span aria-hidden="true">${tipo.rotulo}</span>`
            + `<span class="rh" aria-hidden="true">${tipo.rh === '+' ? 'pos' : 'neg'}</span></span>`;
    }

    function etiquetaComponente(valor, complemento = '') {
        const componente = COMPONENTES[valor];
        if (!componente) {
            return '';
        }
        return `<span class="etiqueta comp-${componente.classe}">${esc(componente.rotulo)}${complemento}</span>`;
    }

    function selo(definicao) {
        if (!definicao) {
            return '';
        }
        return `<span class="selo tom-${definicao.tom}">${icone(definicao.icone)}${esc(definicao.rotulo)}</span>`;
    }

    /** Situação de validade de uma bolsa em texto, para não depender só da cor. */
    function situacaoValidade(bolsa) {
        const dias = diasAte(bolsa.dataValidade);
        const janela = COMPONENTES[bolsa.tipoComponente]?.janela ?? 7;
        if (dias < 0) {
            return { texto: `Vencida há ${plural(-dias, 'dia', 'dias')}`, tom: 'pe' };
        }
        if (dias === 0) {
            return { texto: 'Vence hoje', tom: 'pe' };
        }
        if (dias <= janela) {
            return { texto: `Vence em ${plural(dias, 'dia', 'dias')}`, tom: dias <= 2 ? 'pe' : 'at' };
        }
        return null;
    }

    function avisoValidadeHtml(bolsa) {
        const situacao = situacaoValidade(bolsa);
        if (!situacao) {
            return '';
        }
        return `<span class="aviso-validade ${situacao.tom}">${icone('alerta')}${esc(situacao.texto)}</span>`;
    }

    /* ---------- Acesso à API ---------- */

    async function requisitar(url, { metodo = 'GET', corpo } = {}) {
        let resposta;
        try {
            resposta = await fetch(url, {
                method: metodo,
                headers: corpo === undefined
                    ? { Accept: 'application/json' }
                    : { Accept: 'application/json', 'Content-Type': 'application/json' },
                body: corpo === undefined ? undefined : JSON.stringify(corpo)
            });
        } catch (falha) {
            const erro = new Error('O servidor não respondeu.');
            erro.status = 0;
            throw erro;
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
            const erro = new Error(dados?.mensagem || `Erro ${resposta.status} ao falar com o servidor.`);
            erro.status = resposta.status;
            throw erro;
        }
        return { status: resposta.status, dados };
    }

    /* ---------- Estado ---------- */

    const FONTES = {
        bolsas: API.bolsas,
        solicitacoes: API.solicitacoes,
        fila: API.fila,
        alertas: API.alertas,
        baixas: API.baixas,
        indicadores: API.indicadores,
        historico: API.historico
    };

    const estado = {
        pagina: 'estoque',
        dados: {},
        erros: {},
        carregando: new Set(),
        atualizadoEm: null,
        filtros: { tipo: '', componente: '', status: '' },
        edicaoBolsaId: null,
        sugestaoIdentificador: null,
        atendimentoSelecionado: null,
        resultadoAlocacao: null
    };

    function carregandoPrimeiraVez(chave) {
        return estado.carregando.has(chave) && !(chave in estado.dados);
    }

    async function carregar(chaves = Object.keys(FONTES)) {
        chaves.forEach((chave) => estado.carregando.add(chave));
        renderizar();
        await Promise.all(chaves.map(async (chave) => {
            try {
                estado.dados[chave] = (await requisitar(FONTES[chave])).dados;
                delete estado.erros[chave];
            } catch (erro) {
                estado.erros[chave] = { mensagem: erro.message, status: erro.status, quando: new Date() };
            } finally {
                estado.carregando.delete(chave);
            }
        }));
        estado.atualizadoEm = new Date();
        renderizar();
    }

    const bolsas = () => estado.dados.bolsas || [];
    const solicitacoes = () => estado.dados.solicitacoes || [];
    const bolsaPorId = (id) => bolsas().find((bolsa) => bolsa.id === id);
    const solicitacaoPorId = (id) => solicitacoes().find((solicitacao) => solicitacao.id === id);

    /** Bolsas que a API registrou como alocadas para a solicitação. */
    function bolsasAlocadasPara(solicitacaoId) {
        return bolsas().filter((bolsa) => bolsa.solicitacaoId === solicitacaoId);
    }

    function candidatasPara(solicitacao) {
        const aceitos = DOADORES_COMPATIVEIS[solicitacao.tipoSanguineo] || [];
        return bolsas().filter((bolsa) => bolsa.status === 'DISPONIVEL'
            && bolsa.tipoComponente === solicitacao.tipoComponente
            && aceitos.includes(bolsa.tipoSanguineo)
            && diasAte(bolsa.dataValidade) >= 0);
    }

    function vencidasAguardandoBaixa() {
        return bolsas()
            .filter((bolsa) => (bolsa.status === 'DISPONIVEL' || bolsa.status === 'ALOCADA')
                && diasAte(bolsa.dataValidade) < 0)
            .sort((a, b) => a.dataValidade.localeCompare(b.dataValidade));
    }

    /* ---------- Estados de lista (carregando, vazio, erro) ---------- */

    function esqueleto(linhas = 3) {
        const linha = '<div><span style="width:22%"></span><span style="width:10%"></span>'
            + '<span style="width:16%"></span><span style="width:30%"></span></div>';
        return `<div class="esqueleto" aria-busy="true"><span class="so-leitor">Carregando…</span>${linha.repeat(linhas)}</div>`;
    }

    function estadoVazio(nomeIcone, titulo, texto, extraClasse = '') {
        return `<div class="estado-lista ${extraClasse}"><span class="estado-icone">${icone(nomeIcone)}</span>`
            + `<strong>${esc(titulo)}</strong><p>${esc(texto)}</p></div>`;
    }

    function estadoErro(titulo, erro) {
        const quando = erro.quando ? `${formatarData(hojeIso())} ${hora(erro.quando)}` : '';
        const codigo = erro.status ? `Código: ${erro.status} · ${quando}` : quando;
        return `<div class="estado-lista erro" role="alert"><span class="estado-icone">${icone('alerta')}</span>`
            + `<strong>${esc(titulo)}</strong>`
            + `<p>${esc(erro.mensagem)} Os dados exibidos podem estar desatualizados; não aloque bolsas até recarregar.</p>`
            + `<button type="button" class="botao secundario" data-acao="recarregar">${icone('recarregar')}Tentar novamente</button>`
            + `<p class="codigo">${esc(codigo)}</p></div>`;
    }

    /** Devolve o HTML do estado especial da fonte (carregando/erro) ou null se há dados. */
    function estadoDaFonte(chave, tituloErro, linhas) {
        if (chave in estado.dados) {
            return null;
        }
        return estado.erros[chave] ? estadoErro(tituloErro, estado.erros[chave]) : esqueleto(linhas);
    }

    /* ---------- Toasts e confirmação ---------- */

    function avisar(tipo, mensagem) {
        const toast = document.createElement('div');
        toast.className = `toast ${tipo}`;
        toast.setAttribute('role', tipo === 'erro' ? 'alert' : 'status');
        toast.innerHTML = `<span class="toast-icone">${icone(tipo === 'erro' ? 'x' : 'check')}</span>`
            + `<div class="toast-corpo"><strong>${tipo === 'erro' ? 'Erro' : 'Sucesso'}</strong><p>${esc(mensagem)}</p></div>`
            + `<button type="button" class="botao-icone" aria-label="Fechar aviso">${icone('x')}</button>`;
        const fechar = () => toast.remove();
        toast.querySelector('button').addEventListener('click', fechar);
        document.getElementById('toasts').append(toast);
        setTimeout(fechar, tipo === 'erro' ? 9000 : 5000);
    }

    function confirmar({ titulo, texto, extra = '', rotuloConfirmar }) {
        const modal = document.getElementById('modal');
        document.getElementById('modal-titulo').textContent = titulo;
        document.getElementById('modal-texto').textContent = texto;
        document.getElementById('modal-extra').innerHTML = extra;
        document.getElementById('modal-confirmar').textContent = rotuloConfirmar;
        return new Promise((resolver) => {
            modal.addEventListener('close', () => resolver(modal.returnValue === 'confirmar'), { once: true });
            modal.returnValue = '';
            modal.showModal();
            modal.querySelector('button[value="cancelar"]').focus();
        });
    }

    /* ---------- Formulários: opções, erros ---------- */

    function preencherOpcoes() {
        const grupos = {
            tipoSanguineo: Object.entries(TIPOS_SANGUINEOS).map(([valor, tipo]) => [valor, tipo.rotulo, `Tipo ${tipo.extenso}`]),
            tipoComponente: Object.entries(COMPONENTES).map(([valor, c]) => [valor, c.rotulo, c.rotulo, c.classe]),
            nivelUrgencia: Object.entries(URGENCIAS).map(([valor, u]) => [valor, u.rotulo, u.rotulo])
        };
        document.querySelectorAll('[data-opcoes]').forEach((container) => {
            const nome = container.dataset.opcoes;
            container.innerHTML = grupos[nome].map(([valor, rotulo, rotuloLeitor, classe]) =>
                `<label class="opcao${classe ? ` comp-${classe}` : ''}"><input type="radio" name="${nome}" value="${valor}" aria-label="${esc(rotuloLeitor)}">`
                + `<span aria-hidden="true">${esc(rotulo)}</span></label>`).join('');
        });

        const listas = {
            tipoSanguineo: Object.entries(TIPOS_SANGUINEOS).map(([valor, tipo]) => [valor, tipo.rotulo]),
            tipoComponente: Object.entries(COMPONENTES).map(([valor, c]) => [valor, c.rotulo]),
            statusBolsa: Object.entries(STATUS_BOLSA).map(([valor, s]) => [valor, s.rotulo])
        };
        document.querySelectorAll('[data-opcoes-select]').forEach((select) => {
            const vazio = select.dataset.vazio || 'Todos';
            select.innerHTML = `<option value="">${esc(vazio)}</option>`
                + listas[select.dataset.opcoesSelect].map(([valor, rotulo]) =>
                    `<option value="${valor}">${esc(rotulo)}</option>`).join('');
        });
    }

    function limparErros(form) {
        form.querySelectorAll('.campo.com-erro').forEach((campo) => campo.classList.remove('com-erro'));
        form.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
        const resumo = form.querySelector('[data-resumo]');
        resumo.hidden = true;
        resumo.innerHTML = '';
    }

    function mostrarErros(form, erros) {
        limparErros(form);
        const nomes = Object.keys(erros);
        if (nomes.length === 0) {
            return false;
        }
        nomes.forEach((nome) => {
            const campo = form.querySelector(`[data-campo="${nome}"]`);
            if (!campo) {
                return;
            }
            campo.classList.add('com-erro');
            campo.querySelector('.mensagem-erro').innerHTML = `${icone('erro')}<span>${esc(erros[nome])}</span>`;
            campo.querySelectorAll('input, select').forEach((el) => el.setAttribute('aria-invalid', 'true'));
        });
        if (nomes.length > 1) {
            const resumo = form.querySelector('[data-resumo]');
            resumo.innerHTML = `${icone('erro')}${nomes.length} campos precisam de correção.`;
            resumo.hidden = false;
        }
        const primeiro = form.querySelector('.campo.com-erro input:not([type=radio]), .campo.com-erro select, .campo.com-erro input[type=radio]');
        primeiro?.focus();
        return true;
    }

    function limparErroDoCampo(evento) {
        const campo = evento.target.closest('.campo');
        if (campo?.classList.contains('com-erro')) {
            campo.classList.remove('com-erro');
            campo.querySelectorAll('[aria-invalid]').forEach((el) => el.removeAttribute('aria-invalid'));
        }
    }

    /* ---------- Estoque ---------- */

    const formBolsa = document.getElementById('form-bolsa');

    function proximoIdentificador() {
        const ano = new Date().getFullYear();
        const prefixo = `BOLSA-${ano}-`;
        const maior = bolsas()
            .filter((bolsa) => bolsa.identificador.startsWith(prefixo))
            .map((bolsa) => Number(bolsa.identificador.slice(prefixo.length)))
            .filter(Number.isFinite)
            .reduce((a, b) => Math.max(a, b), 0);
        return prefixo + String(maior + 1).padStart(4, '0');
    }

    function lerFormBolsa() {
        const campos = formBolsa.elements;
        return {
            identificador: campos.identificador.value.trim(),
            tipoSanguineo: campos.tipoSanguineo.value,
            tipoComponente: campos.tipoComponente.value,
            dataColeta: campos.dataColeta.value,
            dataValidade: campos.dataValidade.value,
            volume: campos.volume.value === '' ? null : Number(campos.volume.value)
        };
    }

    function validarBolsa(dados, editando) {
        const erros = {};
        if (!editando) {
            if (!dados.identificador) {
                erros.identificador = 'Informe o identificador da bolsa.';
            } else if (bolsas().some((bolsa) => bolsa.identificador === dados.identificador)) {
                erros.identificador = 'Bolsa já existe.';
            }
        }
        if (!dados.tipoSanguineo) {
            erros.tipoSanguineo = 'Selecione o tipo sanguíneo.';
        }
        if (!dados.tipoComponente) {
            erros.tipoComponente = 'Selecione o componente.';
        }
        if (!dados.dataColeta) {
            erros.dataColeta = 'Informe a data de coleta.';
        }
        if (!dados.dataValidade) {
            erros.dataValidade = 'Informe a data de validade.';
        } else if (dados.dataColeta && dados.dataValidade < dados.dataColeta) {
            erros.dataValidade = 'Data de validade não pode ser anterior à data de coleta.';
        }
        if (dados.volume === null) {
            erros.volume = 'Informe o volume.';
        } else if (!(dados.volume > 0) || !Number.isInteger(dados.volume)) {
            erros.volume = 'O volume precisa ser um número inteiro positivo.';
        }
        return erros;
    }

    function entrarEdicaoBolsa(bolsa) {
        estado.edicaoBolsaId = bolsa.id;
        limparErros(formBolsa);
        const campos = formBolsa.elements;
        campos.id.value = bolsa.id;
        campos.identificador.value = bolsa.identificador;
        campos.identificador.readOnly = true;
        formBolsa.querySelector(`input[name=tipoSanguineo][value="${bolsa.tipoSanguineo}"]`).checked = true;
        formBolsa.querySelector(`input[name=tipoComponente][value="${bolsa.tipoComponente}"]`).checked = true;
        campos.dataColeta.value = bolsa.dataColeta;
        campos.dataValidade.value = bolsa.dataValidade;
        campos.volume.value = bolsa.volume;
        document.getElementById('form-bolsa-titulo').textContent = `Editar bolsa ${bolsa.identificador}`;
        document.getElementById('form-bolsa-modo').hidden = false;
        document.getElementById('form-bolsa-cancelar').hidden = false;
        document.getElementById('form-bolsa-enviar').innerHTML = 'Salvar alterações';
        formBolsa.scrollIntoView({ behavior: 'smooth', block: 'start' });
        formBolsa.querySelector('input[name=tipoSanguineo]:checked').focus({ preventScroll: true });
        renderizarEstoque();
    }

    function sairEdicaoBolsa() {
        estado.edicaoBolsaId = null;
        formBolsa.reset();
        limparErros(formBolsa);
        formBolsa.elements.identificador.readOnly = false;
        formBolsa.elements.identificador.value = '';
        document.getElementById('form-bolsa-titulo').textContent = 'Cadastrar bolsa';
        document.getElementById('form-bolsa-modo').hidden = true;
        document.getElementById('form-bolsa-cancelar').hidden = true;
        document.getElementById('form-bolsa-enviar').innerHTML = `${icone('mais')}Cadastrar bolsa`;
        renderizarEstoque();
    }

    async function enviarBolsa(evento) {
        evento.preventDefault();
        const editando = estado.edicaoBolsaId !== null;
        const dados = lerFormBolsa();
        if (mostrarErros(formBolsa, validarBolsa(dados, editando))) {
            avisar('erro', 'Não foi possível salvar. Corrija os campos destacados.');
            return;
        }
        const botao = document.getElementById('form-bolsa-enviar');
        botao.disabled = true;
        try {
            if (editando) {
                const { identificador, ...corpo } = dados;
                await requisitar(`${API.bolsas}/${estado.edicaoBolsaId}`, { metodo: 'PUT', corpo });
                avisar('sucesso', `Bolsa ${identificador} atualizada.`);
            } else {
                await requisitar(API.bolsas, { metodo: 'POST', corpo: dados });
                avisar('sucesso', `Bolsa ${dados.identificador} cadastrada no estoque.`);
            }
            await carregar();
            sairEdicaoBolsa();
        } catch (erro) {
            const porCampo = {
                'Bolsa já existe': 'identificador',
                'Data de validade não pode ser anterior à data de coleta': 'dataValidade'
            };
            const campo = porCampo[erro.message];
            if (campo) {
                mostrarErros(formBolsa, { [campo]: `${erro.message}.` });
            }
            avisar('erro', erro.message);
        } finally {
            botao.disabled = false;
        }
    }

    async function excluirBolsa(bolsa) {
        const confirmado = await confirmar({
            titulo: `Excluir a bolsa ${bolsa.identificador}?`,
            texto: 'A bolsa sai do estoque e o registro não pode ser recuperado.',
            rotuloConfirmar: 'Excluir bolsa'
        });
        if (!confirmado) {
            return;
        }
        try {
            await requisitar(`${API.bolsas}/${bolsa.id}`, { metodo: 'DELETE' });
            if (estado.edicaoBolsaId === bolsa.id) {
                sairEdicaoBolsa();
            }
            avisar('sucesso', `Bolsa ${bolsa.identificador} excluída do estoque.`);
            await carregar();
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    function bolsasFiltradas() {
        const { tipo, componente, status } = estado.filtros;
        return bolsas()
            .filter((bolsa) => (!tipo || bolsa.tipoSanguineo === tipo)
                && (!componente || bolsa.tipoComponente === componente)
                && (!status || bolsa.status === status))
            .sort((a, b) => b.id - a.id);
    }

    function acoesBolsaHtml(bolsa) {
        const podeExcluir = bolsa.status === 'DISPONIVEL';
        return `<button type="button" class="botao-icone" data-acao="editar-bolsa" data-id="${bolsa.id}" aria-label="Editar ${esc(bolsa.identificador)}">${icone('lapis')}</button>`
            + `<button type="button" class="botao-icone perigo" data-acao="excluir-bolsa" data-id="${bolsa.id}"`
            + (podeExcluir
                ? ` aria-label="Excluir ${esc(bolsa.identificador)}"`
                : ` disabled aria-label="Excluir ${esc(bolsa.identificador)} (só bolsas disponíveis podem ser excluídas)" title="Só bolsas disponíveis podem ser excluídas"`)
            + `>${icone('lixeira')}</button>`;
    }

    function renderizarEstoque() {
        const alvo = document.getElementById('lista-bolsas');
        const contagem = document.getElementById('contagem-bolsas');
        const especial = estadoDaFonte('bolsas', 'Não foi possível carregar as bolsas', 4);
        if (especial) {
            alvo.innerHTML = especial;
            contagem.innerHTML = !estado.erros.bolsas
                ? '<span class="carregando-texto"><span class="giro"></span>Carregando bolsas…</span>' : '';
            return;
        }

        // Sugere o próximo identificador enquanto o usuário não digitar outro.
        const campoIdentificador = formBolsa.elements.identificador;
        if (estado.edicaoBolsaId === null
                && (!campoIdentificador.value || campoIdentificador.value === estado.sugestaoIdentificador)) {
            estado.sugestaoIdentificador = proximoIdentificador();
            campoIdentificador.value = estado.sugestaoIdentificador;
        }

        const lista = bolsasFiltradas();
        const filtrando = Object.values(estado.filtros).some(Boolean);
        contagem.textContent = filtrando
            ? `${lista.length} de ${plural(bolsas().length, 'bolsa', 'bolsas')}`
            : plural(bolsas().length, 'bolsa', 'bolsas');

        if (bolsas().length === 0) {
            alvo.innerHTML = estadoVazio('estoque', 'Nenhuma bolsa cadastrada',
                'Use o formulário “Cadastrar bolsa” para registrar a primeira bolsa.');
            return;
        }
        if (lista.length === 0) {
            alvo.innerHTML = estadoVazio('estoque', 'Nenhuma bolsa encontrada com esses filtros',
                'Ajuste os filtros de tipo, componente ou status para ver outras bolsas.');
            return;
        }

        const linhas = lista.map((bolsa) => {
            const classes = [bolsa.id === estado.edicaoBolsaId ? 'em-edicao' : '',
                bolsa.status === 'DESCARTADA' ? 'apagada' : ''].join(' ');
            return `<tr class="${classes}">
                <td class="mono">${esc(bolsa.identificador)}</td>
                <td>${tipoHtml(bolsa.tipoSanguineo)}</td>
                <td>${etiquetaComponente(bolsa.tipoComponente)}</td>
                <td class="num">${formatarData(bolsa.dataColeta)}</td>
                <td class="num"><div class="celula-validade">${formatarData(bolsa.dataValidade)}${avisoValidadeHtml(bolsa)}</div></td>
                <td class="num direita">${bolsa.volume} mL</td>
                <td>${selo(STATUS_BOLSA[bolsa.status])}</td>
                <td class="acoes">${acoesBolsaHtml(bolsa)}</td>
            </tr>`;
        }).join('');

        const cartoes = lista.map((bolsa) => `<li class="${bolsa.id === estado.edicaoBolsaId ? 'em-edicao' : ''}">
                <span class="bloco-tipo">${tipoHtml(bolsa.tipoSanguineo, 'sm')}</span>
                <div class="corpo">
                    <div class="linha"><span class="mono">${esc(bolsa.identificador)}</span>${etiquetaComponente(bolsa.tipoComponente)}</div>
                    <p class="detalhe num">Coleta ${formatarData(bolsa.dataColeta)} · Validade <strong>${formatarData(bolsa.dataValidade)}</strong> · ${bolsa.volume} mL</p>
                    <div class="linha">${selo(STATUS_BOLSA[bolsa.status])}${avisoValidadeHtml(bolsa)}</div>
                </div>
                <div>${acoesBolsaHtml(bolsa)}</div>
            </li>`).join('');

        alvo.innerHTML = `<div class="tabela-rolagem so-desktop"><table class="tabela">
                <thead><tr>
                    <th scope="col">Identificador</th><th scope="col">Tipo</th><th scope="col">Componente</th>
                    <th scope="col">Coleta</th><th scope="col">Validade</th><th scope="col" class="direita">Volume</th>
                    <th scope="col">Status</th><th scope="col" class="direita">Ações</th>
                </tr></thead>
                <tbody>${linhas}</tbody>
            </table></div>
            <ul class="lista-movel">${cartoes}</ul>`;
    }

    /* ---------- Solicitações ---------- */

    const formSolicitacao = document.getElementById('form-solicitacao');

    function lerFormSolicitacao() {
        const campos = formSolicitacao.elements;
        return {
            nomeHospital: campos.nomeHospital.value.trim(),
            tipoSanguineo: campos.tipoSanguineo.value,
            tipoComponente: campos.tipoComponente.value,
            quantidade: campos.quantidade.value === '' ? null : Number(campos.quantidade.value),
            nivelUrgencia: campos.nivelUrgencia.value
        };
    }

    function validarSolicitacao(dados) {
        const erros = {};
        if (!dados.nomeHospital) {
            erros.nomeHospital = 'Informe o hospital.';
        }
        if (!dados.tipoSanguineo) {
            erros.tipoSanguineo = 'Selecione o tipo sanguíneo do paciente.';
        }
        if (!dados.tipoComponente) {
            erros.tipoComponente = 'Selecione o componente.';
        }
        if (dados.quantidade === null) {
            erros.quantidade = 'Informe a quantidade de bolsas.';
        } else if (!(dados.quantidade > 0) || !Number.isInteger(dados.quantidade)) {
            erros.quantidade = 'A quantidade precisa ser um número inteiro positivo.';
        }
        if (!dados.nivelUrgencia) {
            erros.nivelUrgencia = 'Selecione a urgência.';
        }
        return erros;
    }

    async function enviarSolicitacao(evento) {
        evento.preventDefault();
        const dados = lerFormSolicitacao();
        if (mostrarErros(formSolicitacao, validarSolicitacao(dados))) {
            avisar('erro', 'Não foi possível registrar. Corrija os campos destacados.');
            return;
        }
        const botao = formSolicitacao.querySelector('button[type=submit]');
        botao.disabled = true;
        try {
            const { dados: criada } = await requisitar(API.solicitacoes, { metodo: 'POST', corpo: dados });
            formSolicitacao.reset();
            limparErros(formSolicitacao);
            await carregar();
            const posicao = (estado.dados.fila || []).findIndex((item) => item.id === criada.id) + 1;
            avisar('sucesso', `${codigoSolicitacao(criada.id)} registrada`
                + (posicao > 0 ? ` · ${posicao}º da fila.` : '.'));
        } catch (erro) {
            avisar('erro', erro.message);
        } finally {
            botao.disabled = false;
        }
    }

    function emAtendimento() {
        return solicitacoes().filter((s) => s.status === 'EM_ATENDIMENTO').sort((a, b) => b.id - a.id);
    }

    function solicitacaoAtual() {
        const lista = emAtendimento();
        return lista.find((s) => s.id === estado.atendimentoSelecionado) || lista[0] || null;
    }

    async function chamarProxima() {
        try {
            const { dados: chamada } = await requisitar(API.chamarProxima, { metodo: 'POST' });
            estado.atendimentoSelecionado = chamada.id;
            estado.resultadoAlocacao = null;
            avisar('sucesso', `${codigoSolicitacao(chamada.id)} chamada da fila · ${chamada.nomeHospital}.`);
            await carregar();
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    async function alocarBolsa(solicitacao) {
        const candidatas = candidatasPara(solicitacao).length;
        try {
            const { status, dados } = await requisitar(API.alocar(solicitacao.id), { metodo: 'POST' });
            if (status === 200 && dados.alocada) {
                estado.resultadoAlocacao = { solicitacaoId: solicitacao.id, alocada: true, bolsaId: dados.bolsaId, candidatas };
                await carregar();
                const bolsa = bolsaPorId(dados.bolsaId);
                const concluida = dados.statusSolicitacao === 'ATENDIDA' ? ' Solicitação atendida.' : '';
                avisar('sucesso', `${bolsa ? bolsa.identificador : 'Bolsa'} alocada para ${codigoSolicitacao(solicitacao.id)} `
                    + `(${dados.bolsasAlocadas} de ${solicitacao.quantidade}).${concluida}`);
            } else {
                estado.resultadoAlocacao = { solicitacaoId: solicitacao.id, alocada: false };
                avisar('erro', `Sem estoque compatível para ${codigoSolicitacao(solicitacao.id)}.`);
                renderizar();
            }
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    async function excluirSolicitacao(solicitacao) {
        const confirmado = await confirmar({
            titulo: `Excluir ${codigoSolicitacao(solicitacao.id)}?`,
            texto: `O pedido de ${solicitacao.nomeHospital} sai da lista de solicitações. Bolsas já alocadas a ele continuam com status ALOCADA.`,
            rotuloConfirmar: 'Excluir solicitação'
        });
        if (!confirmado) {
            return;
        }
        try {
            await requisitar(`${API.solicitacoes}/${solicitacao.id}`, { metodo: 'DELETE' });
            if (estado.resultadoAlocacao?.solicitacaoId === solicitacao.id) {
                estado.resultadoAlocacao = null;
            }
            avisar('sucesso', `${codigoSolicitacao(solicitacao.id)} excluída.`);
            await carregar();
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    function resumoPedido(s) {
        return `${COMPONENTES[s.tipoComponente]?.rotulo} · ${plural(s.quantidade, 'bolsa', 'bolsas')}`;
    }

    function seloUrgencia(valor, curto = false) {
        const urgencia = URGENCIAS[valor];
        return urgencia ? selo({ ...urgencia, rotulo: curto ? urgencia.curto : urgencia.rotulo }) : '';
    }

    function renderizarFila() {
        const alvo = document.getElementById('fila-atendimento');
        const contagem = document.getElementById('contagem-fila');
        const especial = estadoDaFonte('fila', 'Não foi possível carregar a fila', 3);
        if (especial) {
            alvo.innerHTML = especial;
            contagem.textContent = '';
            return;
        }
        const fila = estado.dados.fila || [];
        contagem.textContent = `${fila.length} na fila`;

        const atual = solicitacaoAtual();
        const dica = atual
            ? `<p class="dica-fila">${codigoSolicitacao(atual.id)} ainda está em atendimento.</p>`
            : '';
        const botao = `<button type="button" class="botao secundario largo" data-acao="chamar-proxima"${fila.length ? '' : ' disabled'}>${icone('seta')}Chamar próxima</button>`;

        if (fila.length === 0) {
            alvo.innerHTML = `${estadoVazio('prancheta', 'Fila vazia',
                'Nenhuma solicitação aguardando atendimento. Novos pedidos entram aqui por ordem de chegada.', 'tracejado')}
                <div style="margin-top:12px">${botao}</div>`;
            return;
        }

        const [primeira, ...demais] = fila;
        alvo.innerHTML = `<ol class="fila">
            <li class="primeira">
                <div class="primeira-cab"><span>1º da fila · próxima</span><span class="mono">${codigoSolicitacao(primeira.id)}</span></div>
                <div class="primeira-corpo">
                    ${tipoHtml(primeira.tipoSanguineo, 'xl')}
                    <div><strong>${esc(primeira.nomeHospital)}</strong>${etiquetaComponente(primeira.tipoComponente)}
                        <span class="legenda"> ${plural(primeira.quantidade, 'bolsa', 'bolsas')}</span></div>
                </div>
                <div class="primeira-rodape"><span></span>${seloUrgencia(primeira.nivelUrgencia)}</div>
            </li>
            ${demais.map((s, indice) => `<li>
                <span class="posicao">${indice + 2}º</span>
                <span class="tipo-coluna">${tipoHtml(s.tipoSanguineo, 'sm')}</span>
                <div class="corpo"><strong>${esc(s.nomeHospital)}</strong><span>${esc(resumoPedido(s))}</span></div>
                ${seloUrgencia(s.nivelUrgencia, true)}
            </li>`).join('')}
        </ol>${botao}${dica}`;
    }

    function renderizarAtendimento() {
        const alvo = document.getElementById('em-atendimento');
        const especial = estadoDaFonte('solicitacoes', 'Não foi possível carregar o atendimento', 2);
        if (especial) {
            alvo.innerHTML = especial;
            return;
        }
        const atual = solicitacaoAtual();
        if (!atual) {
            alvo.innerHTML = estadoVazio('play', 'Nenhuma solicitação em atendimento',
                'Use “Chamar próxima” para tirar o primeiro pedido da fila e começar a alocar bolsas.', 'tracejado');
            return;
        }

        const alocadas = bolsasAlocadasPara(atual.id);
        const completa = alocadas.length >= atual.quantidade;
        const percentual = Math.min(100, Math.round((alocadas.length / atual.quantidade) * 100));
        const resultado = estado.resultadoAlocacao?.solicitacaoId === atual.id ? estado.resultadoAlocacao : null;
        const tipo = TIPOS_SANGUINEOS[atual.tipoSanguineo];
        const componente = COMPONENTES[atual.tipoComponente];

        let resultadoHtml = '';
        if (resultado?.alocada) {
            const bolsa = bolsaPorId(resultado.bolsaId);
            const dias = bolsa ? diasAte(bolsa.dataValidade) : null;
            resultadoHtml = `<div class="resultado ok" role="status">
                <p class="resultado-titulo">${icone('check')}Bolsa alocada</p>
                ${bolsa ? `<div class="cartao-bolsa">${tipoHtml(bolsa.tipoSanguineo, 'md')}<div class="corpo">
                    <span class="mono">${esc(bolsa.identificador)}</span>${etiquetaComponente(bolsa.tipoComponente)}
                    <span class="legenda num">vence ${formatarData(bolsa.dataValidade)} · ${dias === 0 ? 'hoje' : `em ${plural(dias, 'dia', 'dias')}`}</span>
                </div></div>` : ''}
                <p class="regra">Regra: compatibilidade de ${esc(componente.minusculo)} (ABO/Rh) para receptor ${tipo.rotulo}.
                    Escolhida por vencer primeiro entre ${plural(resultado.candidatas, 'bolsa compatível', 'bolsas compatíveis')}.</p>
            </div>`;
        } else if (resultado && !resultado.alocada) {
            resultadoHtml = `<div class="resultado perigo" role="alert">
                <p class="resultado-titulo">${icone('alerta')}Sem estoque compatível</p>
                <p>Nenhuma bolsa DISPONÍVEL de ${esc(componente.rotulo)} dentro da validade é compatível com o tipo ${tipo.rotulo}.</p>
                <p class="legenda" style="margin-top:6px">A solicitação continua em atendimento. Acione a captação ou exclua o pedido.</p>
            </div>`;
        }

        const outros = emAtendimento().filter((s) => s.id !== atual.id);
        alvo.innerHTML = `<div class="atendimento-cartao">
                ${tipoHtml(atual.tipoSanguineo, 'xl')}
                <div class="corpo">
                    <div>${selo(STATUS_SOLICITACAO.EM_ATENDIMENTO)}</div>
                    <span class="mono secundario">${codigoSolicitacao(atual.id)}</span>
                    <strong>${esc(atual.nomeHospital)}</strong>
                    <div class="linha">${etiquetaComponente(atual.tipoComponente)} <span class="legenda">${esc(URGENCIAS[atual.nivelUrgencia]?.rotulo || '')}</span></div>
                </div>
            </div>
            <div class="progresso-rotulo"><span>Bolsas alocadas</span><span class="num">${alocadas.length} de ${atual.quantidade}</span></div>
            <div class="progresso" role="progressbar" aria-label="Bolsas alocadas" aria-valuemin="0" aria-valuemax="${atual.quantidade}" aria-valuenow="${alocadas.length}"><span style="width:${percentual}%"></span></div>
            <div class="atendimento-acoes">
                <button type="button" class="botao principal largo" data-acao="alocar" data-id="${atual.id}"${completa ? ' disabled' : ''}>${icone('link')}${completa ? 'Quantidade solicitada atingida' : 'Alocar bolsa compatível'}</button>
                <div class="linha-botoes"><button type="button" class="botao secundario" data-acao="excluir-solicitacao" data-id="${atual.id}">Excluir solicitação</button></div>
                ${resultadoHtml}
            </div>
            ${outros.length ? `<div class="outros-atendimentos"><span class="secundario">Também em atendimento:</span><ul>
                ${outros.map((s) => `<li><button type="button" data-acao="selecionar-atendimento" data-id="${s.id}">${codigoSolicitacao(s.id)}</button></li>`).join('')}
            </ul></div>` : ''}`;
    }

    function renderizarSolicitacoes() {
        renderizarFila();
        renderizarAtendimento();

        const hospitais = [...new Set(solicitacoes().map((s) => s.nomeHospital))].sort((a, b) => a.localeCompare(b, 'pt-BR'));
        document.getElementById('hospitais').innerHTML = hospitais.map((h) => `<option value="${esc(h)}"></option>`).join('');

        const alvo = document.getElementById('lista-solicitacoes');
        const contagem = document.getElementById('contagem-solicitacoes');
        const especial = estadoDaFonte('solicitacoes', 'Não foi possível carregar as solicitações', 4);
        if (especial) {
            alvo.innerHTML = especial;
            contagem.textContent = '';
            return;
        }
        const lista = [...solicitacoes()].sort((a, b) => b.id - a.id);
        const abertas = lista.filter((s) => s.status === 'PENDENTE' || s.status === 'EM_ATENDIMENTO').length;
        contagem.textContent = `${plural(lista.length, 'pedido', 'pedidos')} · ${abertas} em aberto`;
        if (lista.length === 0) {
            alvo.innerHTML = estadoVazio('prancheta', 'Nenhuma solicitação registrada',
                'Use o formulário “Registrar solicitação” para colocar o primeiro pedido na fila.');
            return;
        }

        const bolsasTexto = (s) => {
            const alocadas = bolsasAlocadasPara(s.id).length;
            return alocadas ? `${alocadas} de ${s.quantidade}` : String(s.quantidade);
        };

        alvo.innerHTML = `<div class="tabela-rolagem so-desktop"><table class="tabela">
                <thead><tr>
                    <th scope="col">Nº</th><th scope="col">Tipo</th><th scope="col">Hospital</th><th scope="col">Componente</th>
                    <th scope="col" class="direita">Bolsas</th><th scope="col">Urgência</th><th scope="col">Status</th>
                </tr></thead>
                <tbody>${lista.map((s) => `<tr>
                    <td class="mono">${codigoSolicitacao(s.id)}</td>
                    <td>${tipoHtml(s.tipoSanguineo)}</td>
                    <td>${esc(s.nomeHospital)}</td>
                    <td>${etiquetaComponente(s.tipoComponente)}</td>
                    <td class="num direita">${bolsasTexto(s)}</td>
                    <td>${seloUrgencia(s.nivelUrgencia)}</td>
                    <td>${selo(STATUS_SOLICITACAO[s.status])}</td>
                </tr>`).join('')}</tbody>
            </table></div>
            <ul class="lista-movel">${lista.map((s) => `<li>
                <span class="bloco-tipo">${tipoHtml(s.tipoSanguineo, 'sm')}</span>
                <div class="corpo">
                    <div class="linha"><strong>${esc(s.nomeHospital)}</strong>${etiquetaComponente(s.tipoComponente)}</div>
                    <p class="detalhe num"><span class="mono">${codigoSolicitacao(s.id)}</span> · ${bolsasTexto(s)} ${s.quantidade === 1 ? 'bolsa' : 'bolsas'} · ${esc(URGENCIAS[s.nivelUrgencia]?.rotulo || '')}</p>
                </div>
                ${selo(STATUS_SOLICITACAO[s.status])}
            </li>`).join('')}</ul>`;
    }

    /* ---------- Validade ---------- */

    function nivelDia(dias) {
        if (dias <= 0) {
            return 'nivel-hoje';
        }
        if (dias <= 2) {
            return 'nivel-critico';
        }
        return dias <= 7 ? 'nivel-atencao' : 'nivel-neutro';
    }

    async function darBaixaManual(bolsa) {
        try {
            await requisitar(API.baixaManual(bolsa.id), { metodo: 'POST' });
            avisar('sucesso', `Baixa por vencimento registrada para ${bolsa.identificador}.`);
            await carregar();
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    async function verificarValidade() {
        const vencidas = vencidasAguardandoBaixa();
        const disponiveis = vencidas.filter((b) => b.status === 'DISPONIVEL');
        const alocadas = vencidas.filter((b) => b.status === 'ALOCADA');
        if (disponiveis.length > 0) {
            const linhas = disponiveis.map((b) => `<tr><td>${tipoHtml(b.tipoSanguineo, 'sm')}</td>
                <td class="mono">${esc(b.identificador)}</td><td>${etiquetaComponente(b.tipoComponente)}</td>
                <td class="num"><span class="aviso-validade pe">venceu ${formatarData(b.dataValidade)}</span></td></tr>`).join('');
            const avisoAlocadas = alocadas.length
                ? `<div class="aviso atencao">${icone('alerta')}<p><strong>${alocadas.map((b) => esc(b.identificador)).join(', ')}</strong>
                    ${alocadas.length === 1 ? 'está vencida, mas ALOCADA, e não será baixada' : 'estão vencidas, mas ALOCADAS, e não serão baixadas'}.
                    Cancele a alocação antes de dar baixa.</p></div>`
                : '';
            const n = disponiveis.length;
            const confirmado = await confirmar({
                titulo: n === 1 ? 'Dar baixa em 1 bolsa vencida?' : `Dar baixa em ${n} bolsas vencidas?`,
                texto: 'O status passa para DESCARTADA e a baixa é registrada como automática. Não é possível desfazer.',
                extra: `<table class="tabela"><tbody>${linhas}</tbody></table>${avisoAlocadas}`,
                rotuloConfirmar: n === 1 ? 'Dar baixa em 1 bolsa' : `Dar baixa em ${n} bolsas`
            });
            if (!confirmado) {
                return;
            }
        }
        try {
            const { dados } = await requisitar(API.verificarValidade, { metodo: 'POST' });
            const total = dados.baixasRealizadas.length;
            avisar('sucesso', total === 0
                ? 'Verificação concluída. Nenhuma bolsa disponível estava vencida.'
                : `${plural(total, 'bolsa vencida recebeu', 'bolsas vencidas receberam')} baixa.`);
            await carregar();
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    function renderizarValidade() {
        document.getElementById('janelas-alerta').innerHTML = '<span class="legenda">Janela de alerta:</span>'
            + Object.values(COMPONENTES).map((c) => `<span class="etiqueta comp-${c.classe}">${esc(c.rotulo)} · ${plural(c.janela, 'dia', 'dias')}</span>`).join('');

        // Vencidas aguardando baixa
        const alvoVencidas = document.getElementById('vencidas');
        const especialBolsas = estadoDaFonte('bolsas', 'Não foi possível carregar as bolsas', 2);
        if (especialBolsas) {
            alvoVencidas.innerHTML = especialBolsas;
        } else {
            const vencidas = vencidasAguardandoBaixa();
            alvoVencidas.innerHTML = `<p class="titulo-lista">Vencidas aguardando baixa · ${vencidas.length}</p>`
                + (vencidas.length === 0
                    ? `<div class="aviso info" style="margin-bottom:16px">${icone('check')}<p>Nenhuma bolsa vencida no estoque.</p></div>`
                    : `<ul class="vencidas">${vencidas.map((b) => {
                        const alocada = b.status === 'ALOCADA';
                        return `<li class="${alocada ? 'bloqueada' : ''}">
                            <div class="vencida-linha">
                                ${tipoHtml(b.tipoSanguineo, 'sm')}
                                <div class="corpo">
                                    <span class="mono">${esc(b.identificador)}</span>
                                    <div class="linha">${etiquetaComponente(b.tipoComponente)}<span class="venceu num">venceu ${formatarData(b.dataValidade)}</span></div>
                                </div>
                                ${alocada ? selo(STATUS_BOLSA.ALOCADA) : ''}
                                <button type="button" class="botao perigo-contorno" data-acao="baixa-manual" data-id="${b.id}"${alocada ? ' disabled' : ''}>Dar baixa manual</button>
                            </div>
                            ${alocada ? `<div class="aviso perigo">${icone('alerta')}<p><strong>Cancele a alocação antes de dar baixa.</strong> Esta bolsa está reservada para uma solicitação.</p></div>` : ''}
                        </li>`;
                    }).join('')}</ul>`);
        }

        // Próximas do vencimento
        const alvoProximas = document.getElementById('proximas');
        const contagemProximas = document.getElementById('contagem-proximas');
        const especialAlertas = estadoDaFonte('alertas', 'Não foi possível carregar os alertas de validade', 3);
        if (especialAlertas) {
            alvoProximas.innerHTML = especialAlertas;
            contagemProximas.textContent = '';
        } else {
            const alertas = estado.dados.alertas || [];
            contagemProximas.textContent = plural(alertas.length, 'bolsa', 'bolsas');
            alvoProximas.innerHTML = alertas.length === 0
                ? estadoVazio('calendario', 'Nenhuma bolsa perto de vencer',
                    'Bolsas disponíveis aparecem aqui quando entram na janela de alerta do seu componente.')
                : `<ul class="proximas">${alertas.map((a) => {
                    const dias = a.diasParaVencer;
                    const componente = COMPONENTES[a.tipoComponente];
                    const quando = dias === 0 ? 'Vence hoje' : `Vence em ${plural(dias, 'dia', 'dias')}`;
                    return `<li>
                        <span class="dia ${nivelDia(dias)}"><strong>${dias}</strong><span>${dias === 0 ? 'hoje' : dias === 1 ? 'dia' : 'dias'}</span></span>
                        <div class="corpo">
                            <div class="linha">${tipoHtml(a.tipoSanguineo, 'sm')}<span class="mono">${esc(a.identificador)}</span></div>
                            <div class="linha">${etiquetaComponente(a.tipoComponente)}</div>
                            <span class="detalhe num">validade ${formatarData(a.dataValidade)} · janela de ${plural(componente.janela, 'dia', 'dias')}</span>
                        </div>
                        <span class="quando ${dias <= 2 ? 'pe' : dias <= 7 ? 'at' : ''}">${dias <= 2 ? icone('alerta') : ''} ${quando}</span>
                    </li>`;
                }).join('')}</ul>`;
        }

        // Baixas registradas
        const alvoBaixas = document.getElementById('lista-baixas');
        const contagemBaixas = document.getElementById('contagem-baixas');
        const especialBaixas = estadoDaFonte('baixas', 'Não foi possível carregar as baixas', 3);
        if (especialBaixas) {
            alvoBaixas.innerHTML = especialBaixas;
            contagemBaixas.textContent = '';
            return;
        }
        const baixas = [...(estado.dados.baixas || [])].sort((a, b) => b.id - a.id);
        const mesAtual = hojeIso().slice(0, 7);
        const noMes = baixas.filter((b) => b.dataBaixa.startsWith(mesAtual)).length;
        contagemBaixas.textContent = `${noMes} em ${MESES_EXTENSO[hoje().getMonth()]} · ${baixas.length} no total`;
        if (baixas.length === 0) {
            alvoBaixas.innerHTML = estadoVazio('lixeira', 'Nenhuma baixa registrada',
                'As baixas por vencimento, automáticas ou manuais, aparecem aqui.');
            return;
        }
        const origem = (b) => b.automatica
            ? selo({ rotulo: 'Automática', tom: 'ne', icone: 'engrenagem' })
            : selo({ rotulo: 'Manual', tom: 'at', icone: 'pessoa' });
        alvoBaixas.innerHTML = `<div class="tabela-rolagem"><table class="tabela">
            <thead><tr><th scope="col">Bolsa</th><th scope="col">Tipo</th><th scope="col">Componente</th>
                <th scope="col">Motivo</th><th scope="col">Data da baixa</th><th scope="col">Origem</th></tr></thead>
            <tbody>${baixas.map((b) => {
                const bolsa = bolsaPorId(b.bolsaId);
                return `<tr>
                    <td class="mono">${esc(b.identificadorBolsa)}</td>
                    <td>${bolsa ? tipoHtml(bolsa.tipoSanguineo) : '—'}</td>
                    <td>${bolsa ? etiquetaComponente(bolsa.tipoComponente) : '—'}</td>
                    <td>${b.motivo === 'VENCIMENTO' ? 'Vencimento' : esc(b.motivo)}</td>
                    <td class="num">${formatarData(b.dataBaixa)}</td>
                    <td>${origem(b)}</td>
                </tr>`;
            }).join('')}</tbody></table></div>`;
    }

    /* ---------- Indicadores ---------- */

    function classeCalor(n) {
        if (n === 0) {
            return 'n-0';
        }
        if (n <= 2) {
            return 'n-baixo';
        }
        if (n < 10) {
            return 'n-1';
        }
        if (n < 20) {
            return 'n-2';
        }
        return n < 30 ? 'n-3' : 'n-4';
    }

    function renderizarIndicadores() {
        const kpis = document.getElementById('kpis');
        const mapa = document.getElementById('mapa-estoque');
        const demanda = document.getElementById('demanda-hospitais');
        const especial = estadoDaFonte('indicadores', 'Não foi possível carregar os indicadores', 4);
        if (especial) {
            kpis.innerHTML = '';
            mapa.innerHTML = especial;
            demanda.innerHTML = '';
            return;
        }
        const indicadores = estado.dados.indicadores;
        if (!indicadores) {
            return;
        }
        if (estado.atualizadoEm) {
            document.getElementById('indicadores-descricao').textContent =
                `Situação do estoque e da demanda hospitalar, atualizada às ${hora(estado.atualizadoEm)}.`;
        }

        const estoque = indicadores.estoqueDisponivel || [];
        const quantidade = (componente, tipo) => estoque
            .filter((e) => e.tipoComponente === componente && (!tipo || e.tipoSanguineo === tipo))
            .reduce((soma, e) => soma + e.quantidadeBolsasDisponiveis, 0);
        const totalDisponivel = estoque.reduce((soma, e) => soma + e.quantidadeBolsasDisponiveis, 0);

        const alertas = estado.dados.alertas || [];
        const vencemHoje = alertas.filter((a) => a.diasParaVencer === 0).length;
        const ateDois = alertas.filter((a) => a.diasParaVencer > 0 && a.diasParaVencer <= 2).length;

        const abertas = solicitacoes().filter((s) => s.status === 'PENDENTE' || s.status === 'EM_ATENDIMENTO');
        const naFila = abertas.filter((s) => s.status === 'PENDENTE').length;
        const bolsasPendentes = abertas.reduce((soma, s) => soma + Math.max(0, s.quantidade - bolsasAlocadasPara(s.id).length), 0);

        const mesAtual = hojeIso().slice(0, 7);
        const baixasMes = (estado.dados.baixas || []).filter((b) => b.dataBaixa.startsWith(mesAtual));
        const automaticas = baixasMes.filter((b) => b.automatica).length;

        const kpi = (nomeIcone, titulo, valor, unidade, detalhe, tom = '') => `<div class="cartao kpi">
            <p class="kpi-titulo">${icone(nomeIcone)}${esc(titulo)}</p>
            <p class="kpi-valor ${tom}"><strong>${valor}</strong><span>${esc(unidade)}</span></p>
            <p>${esc(detalhe)}</p></div>`;
        kpis.innerHTML = kpi('check', 'Total disponível', totalDisponivel, totalDisponivel === 1 ? 'bolsa' : 'bolsas',
                Object.entries(COMPONENTES).map(([valor, c]) => `${quantidade(valor)} ${c.minusculo}`).join(' · '))
            + kpi('relogio', 'Próximas do vencimento', alertas.length, alertas.length === 1 ? 'bolsa' : 'bolsas',
                `${vencemHoje} vence${vencemHoje === 1 ? '' : 'm'} hoje · ${ateDois} em até 2 dias`, alertas.length ? 'at' : '')
            + kpi('prancheta', 'Solicitações em aberto', abertas.length, abertas.length === 1 ? 'pedido' : 'pedidos',
                `${abertas.length - naFila} em atendimento · ${naFila} na fila · ${plural(bolsasPendentes, 'bolsa pendente', 'bolsas pendentes')}`)
            + kpi('lixeira', 'Baixas no mês', baixasMes.length, baixasMes.length === 1 ? 'bolsa' : 'bolsas',
                `${automaticas} automáticas · ${baixasMes.length - automaticas} manuais · motivo: vencimento`, baixasMes.length ? 'pe' : '');

        // Mapa de calor tipo x componente
        const componentes = Object.keys(COMPONENTES);
        const tipos = Object.keys(TIPOS_SANGUINEOS);
        const linhas = tipos.map((tipo) => {
            const celulas = componentes.map((componente) => {
                const n = quantidade(componente, tipo);
                const marca = n === 0 ? '× ' : n <= 2 ? '↓ ' : '';
                return `<td class="${classeCalor(n)}" aria-label="${TIPOS_SANGUINEOS[tipo].rotulo}, ${COMPONENTES[componente].rotulo}: ${n}">${marca}${n}</td>`;
            }).join('');
            const total = componentes.reduce((soma, c) => soma + quantidade(c, tipo), 0);
            return `<tr><th scope="row">${tipoHtml(tipo, 'sm')}</th>${celulas}<td class="total">${total}</td></tr>`;
        }).join('');
        const totais = componentes.map((c) => `<td>${quantidade(c)}</td>`).join('');
        mapa.innerHTML = `<div class="tabela-rolagem"><table class="calor">
                <thead><tr><th></th>${componentes.map((c) => `<th scope="col">${etiquetaComponente(c)}</th>`).join('')}<th scope="col">Total</th></tr></thead>
                <tbody>${linhas}<tr class="total"><th scope="row">TOTAL</th>${totais}<td>${totalDisponivel}</td></tr></tbody>
            </table></div>
            <div class="calor-legenda">
                <span><i class="amostra" style="border:1.5px solid var(--pe)"></i>Sem estoque</span>
                <span><i class="amostra" style="background:var(--at-f);border:1px solid var(--at)"></i>Baixo (1–2)</span>
                <span>Mais bolsas <i class="amostra" style="background:var(--calor-1)"></i><i class="amostra" style="background:var(--calor-2)"></i><i class="amostra" style="background:var(--calor-3)"></i><i class="amostra" style="background:var(--calor-4)"></i> 3 → 30+</span>
            </div>`;

        // Demanda por hospital
        const itens = indicadores.demandaEmAberto || [];
        if (itens.length === 0) {
            demanda.innerHTML = estadoVazio('prancheta', 'Nenhuma demanda em aberto',
                'Pedidos pendentes ou em atendimento aparecem aqui, agrupados por hospital.');
            return;
        }
        const porHospital = new Map();
        itens.forEach((item) => {
            const atual = porHospital.get(item.nomeHospital) || { total: 0, pedidos: [] };
            atual.total += item.quantidadeBolsasSolicitadasEmAberto;
            atual.pedidos.push(`${TIPOS_SANGUINEOS[item.tipoSanguineo]?.rotulo} ${COMPONENTES[item.tipoComponente]?.minusculo}`);
            porHospital.set(item.nomeHospital, atual);
        });
        const hospitais = [...porHospital.entries()].sort((a, b) => b[1].total - a[1].total);
        const maior = hospitais[0][1].total;
        const marcas = [...new Set([0, Math.round(maior / 3), Math.round((2 * maior) / 3), maior])];

        const semEstoque = itens.filter((item) => {
            const aceitos = DOADORES_COMPATIVEIS[item.tipoSanguineo] || [];
            return aceitos.every((tipo) => quantidade(item.tipoComponente, tipo) === 0);
        });

        demanda.innerHTML = `<ul class="barras">${hospitais.map(([nome, info]) => `<li>
                <div class="rotulo"><strong>${esc(nome)}</strong><span>${esc(info.pedidos.join(', '))}</span></div>
                <div class="barra"><span style="width:${Math.max(2, (info.total / maior) * 88)}%"></span>${info.total}</div>
            </li>`).join('')}</ul>
            <div class="eixo"><span></span><div>${marcas.map((m) => `<span>${m}</span>`).join('')}</div></div>
            ${semEstoque.map((item) => `<div class="aviso perigo" style="margin-top:14px">${icone('alerta')}<p>
                <strong>${TIPOS_SANGUINEOS[item.tipoSanguineo]?.rotulo} ${esc(COMPONENTES[item.tipoComponente]?.minusculo)} sem estoque:</strong>
                o pedido de ${esc(item.nomeHospital)} não tem bolsa compatível disponível.</p></div>`).join('')}`;
    }

    /* ---------- Histórico ---------- */

    function descreverBolsa(identificador, complemento) {
        const bolsa = bolsas().find((b) => b.identificador === identificador);
        if (!bolsa) {
            return `${identificador} ${complemento}`;
        }
        return `${identificador} · ${TIPOS_SANGUINEOS[bolsa.tipoSanguineo]?.rotulo} ${COMPONENTES[bolsa.tipoComponente]?.rotulo} · ${bolsa.volume} mL ${complemento}`;
    }

    function descreverSolicitacao(id, complemento) {
        const s = solicitacaoPorId(Number(id));
        const codigo = codigoSolicitacao(id);
        if (!s) {
            return `${codigo} ${complemento}`;
        }
        return `${codigo} ${complemento} · ${s.nomeHospital} · ${TIPOS_SANGUINEOS[s.tipoSanguineo]?.rotulo} `
            + `${COMPONENTES[s.tipoComponente]?.rotulo} · ${plural(s.quantidade, 'bolsa', 'bolsas')} · ${URGENCIAS[s.nivelUrgencia]?.rotulo}.`;
    }

    // Traduz os textos empilhados pelos serviços do backend (PilhaHistorico).
    const OPERACOES = [
        [/^Cadastro de bolsa (.+)$/, 'Cadastro de bolsa', 'ok', 'mais', (m) => descreverBolsa(m[1], 'cadastrada.')],
        [/^Atualização de bolsa (.+)$/, 'Edição de bolsa', 'ne', 'lapis', (m) => descreverBolsa(m[1], 'atualizada.')],
        [/^Exclusão de bolsa (.+)$/, 'Exclusão de bolsa', 'pe', 'lixeira', (m) => `${m[1]} excluída do estoque.`],
        [/^Baixa por vencimento da bolsa (.+)$/, 'Baixa', 'pe', 'lixeira', (m) => descreverBolsa(m[1], '· baixa por vencimento.')],
        [/^Cadastro de solicitação (\d+)$/, 'Solicitação', 'at', 'prancheta', (m) => descreverSolicitacao(m[1], 'registrada')],
        [/^Atualização de solicitação (\d+)$/, 'Solicitação', 'at', 'lapis', (m) => descreverSolicitacao(m[1], 'atualizada')],
        [/^Exclusão de solicitação (\d+)$/, 'Solicitação', 'pe', 'lixeira', (m) => `${codigoSolicitacao(m[1])} excluída.`],
        [/^Chamada de solicitação (\d+)$/, 'Atendimento', 'in', 'play', (m) => `${codigoSolicitacao(m[1])} chamada da fila · status EM ATENDIMENTO.`]
    ];

    function interpretarOperacao(texto) {
        for (const [padrao, categoria, tom, nomeIcone, descrever] of OPERACOES) {
            const encontrado = texto.match(padrao);
            if (encontrado) {
                return { categoria, tom, icone: nomeIcone, texto: descrever(encontrado) };
            }
        }
        return { categoria: 'Operação', tom: 'ne', icone: 'historico', texto };
    }

    async function removerUltimaOperacao() {
        const confirmado = await confirmar({
            titulo: 'Remover o registro do topo?',
            texto: 'Só o registro sai do histórico. A operação no estoque não é desfeita.',
            rotuloConfirmar: 'Remover registro'
        });
        if (!confirmado) {
            return;
        }
        try {
            const { dados } = await requisitar(API.removerUltimaOperacao, { metodo: 'DELETE' });
            avisar('sucesso', `Registro removido do histórico: ${interpretarOperacao(dados.operacao).categoria.toLowerCase()}.`);
            await carregar(['historico']);
        } catch (erro) {
            avisar('erro', erro.message);
        }
    }

    function renderizarHistorico() {
        const alvo = document.getElementById('linha-tempo');
        const topo = document.getElementById('registro-topo');
        const contagem = document.getElementById('contagem-historico');
        const especial = estadoDaFonte('historico', 'Não foi possível carregar o histórico', 4);
        if (especial) {
            alvo.innerHTML = especial;
            topo.innerHTML = '';
            contagem.textContent = '';
            return;
        }
        const operacoes = (estado.dados.historico || []).map((o) => interpretarOperacao(o.operacao));
        contagem.textContent = plural(operacoes.length, 'registro', 'registros');
        if (operacoes.length === 0) {
            alvo.innerHTML = estadoVazio('historico', 'Histórico vazio',
                'Cadastros, edições, chamadas da fila e baixas aparecem aqui, do mais recente para o mais antigo.');
            topo.innerHTML = `<div class="registro-topo"><p>Não há registro para remover.</p></div>
                <button type="button" class="botao perigo-contorno largo" disabled>${icone('lixeira')}Remover última operação</button>`;
            return;
        }
        alvo.innerHTML = `<ol class="linha-tempo">${operacoes.map((op, indice) => `<li class="${indice === 0 ? 'topo' : ''}">
                <span class="marcador tom-${op.tom}">${icone(op.icone)}</span>
                <div class="corpo">
                    <div class="linha">${indice === 0 ? '<span class="pilula escura">Topo · mais recente</span>' : ''}<span class="pilula tom-${op.tom}">${esc(op.categoria)}</span></div>
                    <p class="texto">${esc(op.texto)}</p>
                </div>
            </li>`).join('')}</ol>`;
        topo.innerHTML = `<div class="registro-topo"><p class="titulo-lista">Registro no topo · ${esc(operacoes[0].categoria)}</p><p>${esc(operacoes[0].texto)}</p></div>
            <button type="button" class="botao perigo-contorno largo" data-acao="remover-ultima">${icone('lixeira')}Remover última operação</button>`;
    }

    /* ---------- Moldura: menu, relógio, conexão ---------- */

    function renderizarMoldura() {
        const fila = estado.dados.fila;
        const pendentesValidade = (estado.dados.alertas || []).length + vencidasAguardandoBaixa().length;
        document.querySelectorAll('[data-contador="solicitacoes"]').forEach((el) => {
            el.hidden = !fila || fila.length === 0;
            el.textContent = fila ? fila.length : '';
            el.setAttribute('aria-label', `${fila ? fila.length : 0} na fila`);
        });
        document.querySelectorAll('[data-contador="validade"]').forEach((el) => {
            el.hidden = pendentesValidade === 0;
            el.textContent = pendentesValidade;
            el.setAttribute('aria-label', `${pendentesValidade} bolsas vencidas ou perto de vencer`);
        });

        const conexao = document.getElementById('conexao');
        const falhas = Object.keys(estado.erros).length;
        conexao.classList.toggle('falha', falhas > 0);
        conexao.querySelector('strong').textContent = falhas > 0 ? 'Falha ao falar com o servidor' : 'Servidor conectado';
        conexao.querySelector('span.secundario').textContent = estado.atualizadoEm
            ? `Atualizado às ${hora(estado.atualizadoEm)}` : '';
    }

    function atualizarRelogio() {
        const agora = new Date();
        const longo = `${DIAS_SEMANA[agora.getDay()]}, ${agora.getDate()} ${MESES[agora.getMonth()]} ${agora.getFullYear()} · ${hora(agora)}`;
        const curto = `${agora.getDate()} ${MESES[agora.getMonth()]} · ${hora(agora)}`;
        document.querySelectorAll('[data-relogio]').forEach((el) => {
            el.innerHTML = el.dataset.relogio === 'longo' ? `${icone('calendario')}${longo}` : curto;
        });
    }

    const RENDERIZADORES = {
        estoque: renderizarEstoque,
        solicitacoes: renderizarSolicitacoes,
        validade: renderizarValidade,
        indicadores: renderizarIndicadores,
        historico: renderizarHistorico
    };

    function renderizar() {
        renderizarMoldura();
        RENDERIZADORES[estado.pagina]();
    }

    function irPara(pagina, focar) {
        estado.pagina = PAGINAS.includes(pagina) ? pagina : 'estoque';
        document.querySelectorAll('[data-secao]').forEach((secao) => {
            secao.hidden = secao.dataset.secao !== estado.pagina;
        });
        document.querySelectorAll('a[data-pagina]').forEach((link) => {
            if (link.dataset.pagina === estado.pagina) {
                link.setAttribute('aria-current', 'page');
            } else {
                link.removeAttribute('aria-current');
            }
        });
        renderizar();
        if (focar) {
            window.scrollTo(0, 0);
            document.getElementById('conteudo').focus({ preventScroll: true });
        }
    }

    /* ---------- Eventos ---------- */

    function aoClicar(evento) {
        const alvo = evento.target.closest('[data-acao]');
        if (!alvo || alvo.disabled) {
            return;
        }
        const id = Number(alvo.dataset.id);
        switch (alvo.dataset.acao) {
            case 'recarregar':
                carregar();
                break;
            case 'editar-bolsa': {
                const bolsa = bolsaPorId(id);
                if (bolsa) {
                    entrarEdicaoBolsa(bolsa);
                }
                break;
            }
            case 'excluir-bolsa': {
                const bolsa = bolsaPorId(id);
                if (bolsa) {
                    excluirBolsa(bolsa);
                }
                break;
            }
            case 'chamar-proxima':
                chamarProxima();
                break;
            case 'alocar': {
                const solicitacao = solicitacaoPorId(id);
                if (solicitacao) {
                    alocarBolsa(solicitacao);
                }
                break;
            }
            case 'excluir-solicitacao': {
                const solicitacao = solicitacaoPorId(id);
                if (solicitacao) {
                    excluirSolicitacao(solicitacao);
                }
                break;
            }
            case 'selecionar-atendimento':
                estado.atendimentoSelecionado = id;
                renderizar();
                break;
            case 'baixa-manual': {
                const bolsa = bolsaPorId(id);
                if (bolsa) {
                    darBaixaManual(bolsa);
                }
                break;
            }
            case 'verificar-validade':
                verificarValidade();
                break;
            case 'remover-ultima':
                removerUltimaOperacao();
                break;
            default:
                break;
        }
    }

    function iniciar() {
        document.querySelectorAll('[data-icone]').forEach((el) => {
            el.outerHTML = el.className
                ? `<span class="${el.className}">${icone(el.dataset.icone)}</span>`
                : icone(el.dataset.icone);
        });
        preencherOpcoes();

        formBolsa.addEventListener('submit', enviarBolsa);
        formBolsa.addEventListener('input', limparErroDoCampo);
        formBolsa.addEventListener('change', limparErroDoCampo);
        document.getElementById('form-bolsa-cancelar').addEventListener('click', sairEdicaoBolsa);
        formSolicitacao.addEventListener('submit', enviarSolicitacao);
        formSolicitacao.addEventListener('input', limparErroDoCampo);
        formSolicitacao.addEventListener('change', limparErroDoCampo);

        document.querySelectorAll('[data-filtro]').forEach((select) => {
            select.addEventListener('change', () => {
                estado.filtros[select.dataset.filtro] = select.value;
                renderizarEstoque();
            });
        });

        document.addEventListener('click', aoClicar);
        window.addEventListener('hashchange', () => irPara(location.hash.slice(1), true));

        atualizarRelogio();
        setInterval(atualizarRelogio, 30000);
        // Mantém fila, validade e indicadores em dia enquanto a aba estiver aberta.
        setInterval(() => {
            if (document.visibilityState === 'visible') {
                carregar();
            }
        }, 60000);

        irPara(location.hash.slice(1), false);
        carregar();
    }

    iniciar();
}());
