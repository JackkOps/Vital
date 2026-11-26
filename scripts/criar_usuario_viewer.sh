#!/usr/bin/env bash
# ============================================================================
# Cria o usuário de LEITURA (papel Viewer) no Grafana — requisito R7.2.
#
# Por que um script e não provisionamento por arquivo: o Grafana provisiona
# datasources e dashboards por YAML, mas NÃO usuários. A criação é feita pela
# API de administração, e este script a torna reproduzível e idempotente
# (rodar duas vezes não gera erro nem usuário duplicado).
#
# Por que um usuário separado em vez de entregar o admin ao professor:
#   - Viewer pode ver dashboards, mas NÃO pode editar, apagar, criar datasource
#     nem ler as credenciais configuradas nos datasources.
#   - Princípio do menor privilégio: quem só precisa olhar recebe só leitura.
#
# USO:
#   ./scripts/criar_usuario_viewer.sh
#
# Lê as variáveis do .env (não versionado):
#   GRAFANA_ADMIN_USER, GRAFANA_ADMIN_PASSWORD  -> para autenticar a criação
#   GRAFANA_VIEWER_USER, GRAFANA_VIEWER_PASSWORD -> o usuário a criar
# ============================================================================

set -euo pipefail

# Caminho do projeto, independente de onde o script é chamado.
# ${BASH_SOURCE[0]} = caminho deste arquivo; dirname tira o nome do arquivo.
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$RAIZ/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERRO: $ENV_FILE não encontrado. Copie o .env.example para .env." >&2
  exit 1
fi

# 'set -a' faz toda variável atribuída ser exportada automaticamente;
# assim o '. arquivo' (source) carrega o .env para o ambiente deste script.
set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a

GRAFANA_URL="${GRAFANA_URL:-http://localhost:${GRAFANA_PORT:-3000}}"
VIEWER_USER="${GRAFANA_VIEWER_USER:-professor}"
VIEWER_NAME="${GRAFANA_VIEWER_NAME:-Professor (leitura)}"

if [[ -z "${GRAFANA_VIEWER_PASSWORD:-}" ]]; then
  echo "ERRO: defina GRAFANA_VIEWER_PASSWORD no .env antes de rodar." >&2
  echo "      (não use a mesma senha do admin)" >&2
  exit 1
fi

if [[ -z "${GRAFANA_ADMIN_PASSWORD:-}" ]]; then
  echo "ERRO: GRAFANA_ADMIN_PASSWORD não definida no .env." >&2
  exit 1
fi

ADMIN_AUTH="${GRAFANA_ADMIN_USER:-admin}:${GRAFANA_ADMIN_PASSWORD}"

echo "Grafana: $GRAFANA_URL"

# Confere se o Grafana está no ar antes de tentar qualquer coisa.
if ! curl -sf --max-time 10 "$GRAFANA_URL/api/health" >/dev/null; then
  echo "ERRO: Grafana não respondeu em $GRAFANA_URL (docker compose ps)." >&2
  exit 1
fi

# -s silencioso, -f falha em erro HTTP, -u usuário:senha para autenticação básica.
existente=$(curl -s -u "$ADMIN_AUTH" \
  "$GRAFANA_URL/api/users/lookup?loginOrEmail=$VIEWER_USER" || true)

if echo "$existente" | grep -q '"id"'; then
  echo "Usuário '$VIEWER_USER' já existe — nada a criar."
  user_id=$(echo "$existente" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
else
  echo "Criando usuário '$VIEWER_USER'..."
  resposta=$(curl -s -u "$ADMIN_AUTH" -X POST \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$VIEWER_NAME\",\"login\":\"$VIEWER_USER\",\"password\":\"$GRAFANA_VIEWER_PASSWORD\"}" \
    "$GRAFANA_URL/api/admin/users")
  if ! echo "$resposta" | grep -q '"id"'; then
    echo "ERRO ao criar usuário: $resposta" >&2
    exit 1
  fi
  user_id=$(echo "$resposta" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
  echo "Criado (id=$user_id)."
fi

# Garante o papel Viewer na organização 1. Usuários novos entram como Viewer por
# padrão, mas a chamada é idempotente e protege contra mudança de default.
echo "Aplicando papel Viewer na organização 1..."
curl -s -u "$ADMIN_AUTH" -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"role":"Viewer"}' \
  "$GRAFANA_URL/api/orgs/1/users/$user_id" >/dev/null

# Garante que NÃO é administrador do servidor.
curl -s -u "$ADMIN_AUTH" -X PUT \
  -H "Content-Type: application/json" \
  -d '{"isGrafanaAdmin":false}' \
  "$GRAFANA_URL/api/admin/users/$user_id/permissions" >/dev/null

echo
echo "Conferindo os papéis na organização:"
# Usa aspas simples no Python e indexacao sem aspas duplas internas, para nao
# colidir com o escaping do Bash.
curl -s -u "$ADMIN_AUTH" "$GRAFANA_URL/api/orgs/1/users" | python3 -c '
import json, sys
for u in json.load(sys.stdin):
    login = u.get("login", "?")
    papel = u.get("role", "?")
    print("  %-16s papel=%s" % (login, papel))
'

echo
echo "Pronto. Informe ao professor: usuário '$VIEWER_USER' e a senha definida no .env."
