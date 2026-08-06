#!/usr/bin/env bash
#
# simulate-str-returns.sh
#
# Simula retornos assíncronos do BACEN/STR publicando XML diretamente no
# tópico Kafka str.settlement.return — o mesmo caminho que o SettlementReturnConsumer
# escuta. Não existe endpoint HTTP pra isso de propósito: no domínio real,
# quem decide ACCEPTED/REJECTED é o BACEN, não o seu sistema (ver
# ProcessSettlementReturnServiceImpl).
#
# O que faz:
#   1. Busca N lotes em status EMITTED direto no Postgres (só esses podem
#      transicionar EMITTED -> TRANSMITTED -> ACCEPTED|TRANSMISSION_REJECTED).
#   2. Pra cada lote, gera um XML de retorno em UMA linha (kafka-console-producer
#      trata cada linha como uma mensagem — XML multi-linha quebraria isso).
#   3. Sorteia ACCEPTED ou REJECTED conforme --reject-rate.
#      Se REJECTED, sorteia também um RejectionReason.code válido.
#   4. Produz tudo pro tópico via kafka-console-producer dentro do
#      container str_kafka.
#
# Uso:
#   ./simulate-str-returns.sh [--count N] [--reject-rate 0.0-1.0] [--env-file .env]
#
# Exemplos:
#   ./simulate-str-returns.sh --count 20 --reject-rate 1.0   # todos rejeitados — stress test da saga
#   ./simulate-str-returns.sh --count 50 --reject-rate 0.3   # 30% de rejeição, mix realista
#
# Pré-requisitos:
#   - docker compose já rodando (postgres, kafka, app)
#   - psql instalado no container str_postgres (padrão na imagem postgres:16-alpine)
#   - kafka-console-producer disponível no container str_kafka (padrão na imagem cp-kafka)

set -euo pipefail

# ---------- defaults ----------
COUNT=10
REJECT_RATE=0.5
ENV_FILE=".env"
KAFKA_CONTAINER="str_kafka"
POSTGRES_CONTAINER="str_postgres"
KAFKA_TOPIC="str.settlement.return"
KAFKA_INTERNAL_BOOTSTRAP="localhost:9092"

# Códigos de RejectionReason — precisam bater 1:1 com domain/enum/RejectionReason.kt
REJECTION_CODES=("001" "002" "003" "004" "005" "999")

# ---------- parse args ----------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --count) COUNT="$2"; shift 2 ;;
    --reject-rate) REJECT_RATE="$2"; shift 2 ;;
    --env-file) ENV_FILE="$2"; shift 2 ;;
    -h|--help)
      grep '^#' "$0" | sed 's/^# \{0,1\}//'
      exit 0
      ;;
    *) echo "Argumento desconhecido: $1" >&2; exit 1 ;;
  esac
done

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Arquivo de env '$ENV_FILE' não encontrado. Rode a partir da raiz do projeto ou passe --env-file." >&2
  exit 1
fi

# shellcheck disable=SC1090
set -a; source "$ENV_FILE"; set +a

: "${DB_NAME:?DB_NAME não definido no .env}"
: "${DB_USER:?DB_USER não definido no .env}"

echo "== Buscando até $COUNT lote(s) em status EMITTED =="

BATCH_IDS=$(docker exec -i "$POSTGRES_CONTAINER" \
  psql -U "$DB_USER" -d "$DB_NAME" -tAc \
  "SELECT id FROM file_batch WHERE status = 'EMITTED' ORDER BY generated_at ASC LIMIT $COUNT;")

if [[ -z "$BATCH_IDS" ]]; then
  echo "Nenhum lote em EMITTED encontrado. Rode o fluxo da Primeira/Segunda Milha antes (assemble + emit) pra ter lotes disponíveis." >&2
  exit 1
fi

TOTAL=$(echo "$BATCH_IDS" | wc -l | tr -d ' ')
echo "Encontrados $TOTAL lote(s). Publicando retornos com reject-rate=$REJECT_RATE..."
echo

I=0
while IFS= read -r BATCH_ID; do
  [[ -z "$BATCH_ID" ]] && continue
  I=$((I + 1))

  # sorteio determinístico por linha, sem depender de $RANDOM/bc pra portabilidade
  ROLL=$(awk -v seed="$I$RANDOM" 'BEGIN { srand(seed); print rand() }')
  IS_REJECTED=$(awk -v roll="$ROLL" -v rate="$REJECT_RATE" 'BEGIN { print (roll < rate) ? "1" : "0" }')

  MESSAGE_CODE="MSG-TEST-$(printf '%04d' "$I")"

  if [[ "$IS_REJECTED" == "1" ]]; then
    IDX=$((RANDOM % ${#REJECTION_CODES[@]}))
    REJ_CODE="${REJECTION_CODES[$IDX]}"
    XML="<StrReturnMessage><BatchId>${BATCH_ID}</BatchId><Result>REJECTED</Result><MessageCode>${MESSAGE_CODE}</MessageCode><RejectionCode>${REJ_CODE}</RejectionCode><Description>Simulated rejection (code ${REJ_CODE})</Description></StrReturnMessage>"
    LABEL="REJECTED (code=$REJ_CODE)"
  else
    XML="<StrReturnMessage><BatchId>${BATCH_ID}</BatchId><Result>ACCEPTED</Result><MessageCode>${MESSAGE_CODE}</MessageCode><Description>Simulated acceptance</Description></StrReturnMessage>"
    LABEL="ACCEPTED"
  fi

  echo "[$I/$TOTAL] batchId=$BATCH_ID -> $LABEL"

  echo "$XML" | docker exec -i "$KAFKA_CONTAINER" \
    kafka-console-producer \
    --bootstrap-server "$KAFKA_INTERNAL_BOOTSTRAP" \
    --topic "$KAFKA_TOPIC" \
    > /dev/null

done <<< "$BATCH_IDS"

echo
echo "== Concluído: $TOTAL mensagem(ns) publicada(s) em '$KAFKA_TOPIC' =="
echo "Acompanhe o consumo em:"
echo "  - logs do container str_app_container"
echo "  - Kafka UI: http://localhost:\${KAFKA_UI_PORT:-8081}"
echo "  - SELECT status, count(*) FROM file_batch GROUP BY status;  (via psql)"