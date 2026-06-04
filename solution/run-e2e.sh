#!/usr/bin/env bash
# run-e2e.sh — Lance la suite E2E complète avec gestion propre des processus.
# Usage : bash run-e2e.sh [-- <playwright-flags>]
#
# Cycle :
#   1. Kill tous les processus dev existants (Vite + Spring Boot)
#   2. Démarre des instances fraîches
#   3. Attend que chaque service soit prêt
#   4. Lance le container e2e (docker compose --profile e2e run --rm e2e)
#   5. Kill à nouveau après les tests
#
# Pré-requis : les containers shop-* doivent déjà tourner (docker compose up -d).

set -euo pipefail

COMPOSE_FILE="$(dirname "$0")/docker-compose.dev.yml"
EXTRA_ARGS="$*"

BACKEND=shop-backend-1
VENDOR=shop-vendor-backoffice-1
BUYER=shop-buyer-portal-1
ADMIN=shop-admin-console-1

kill_dev_processes() {
  echo ">>> Kill processus dev existants"
  docker exec "$BACKEND" bash -c \
    "pkill -9 -f 'spring-boot:run' 2>/dev/null; pkill -9 -f 'ShopApplication' 2>/dev/null; true"
  for c in "$VENDOR" "$BUYER" "$ADMIN"; do
    docker exec "$c" bash -c "pkill -9 -f 'vite' 2>/dev/null; pkill -9 -f 'node.*dev' 2>/dev/null; true"
  done
  sleep 2
}

start_dev_processes() {
  echo ">>> Démarrage Vite (3 SPAs)"
  docker exec "$VENDOR" bash -c \
    "nohup npm run --prefix /workspace/vendor-backoffice dev -- --host --port 5173 > /tmp/vite.log 2>&1 &"
  docker exec "$BUYER" bash -c \
    "nohup npm run --prefix /workspace/buyer-portal dev -- --host --port 5173 > /tmp/vite.log 2>&1 &"
  docker exec "$ADMIN" bash -c \
    "nohup npm run --prefix /workspace/admin-console dev -- --host --port 5173 > /tmp/vite.log 2>&1 &"

  echo ">>> Démarrage Spring Boot"
  docker exec "$BACKEND" bash -c \
    "nohup mvn -f /workspace/pom.xml spring-boot:run > /tmp/spring.log 2>&1 &"
}

wait_vite() {
  echo ">>> Attente Vite servers..."
  for c in "$VENDOR" "$BUYER" "$ADMIN"; do
    i=0
    until docker exec "$c" curl -sf http://localhost:5173/ > /dev/null 2>&1; do
      sleep 3; i=$((i+1))
      [[ $i -ge 40 ]] && { echo "TIMEOUT: $c Vite"; exit 1; }
    done
    echo "    $c: prêt"
  done
}

wait_spring() {
  echo ">>> Attente Spring Boot (~5 min)..."
  i=0
  until docker exec "$BACKEND" curl -sf http://localhost:8080/api/public/theme > /dev/null 2>&1; do
    sleep 8; i=$((i+1))
    [[ $i -ge 50 ]] && { echo "TIMEOUT: Spring Boot"; exit 1; }
  done
  echo "    Spring Boot: prêt"
}

run_tests() {
  echo ">>> Lancement tests E2E (container dédié)"
  if [[ -n "$EXTRA_ARGS" ]]; then
    docker compose -f "$COMPOSE_FILE" --profile e2e run --rm e2e \
      sh -c "npm install && npm run test:ci -- $EXTRA_ARGS"
  else
    docker compose -f "$COMPOSE_FILE" --profile e2e run --rm e2e
  fi
}

# ── Main ────────────────────────────────────────────────────────────────────

kill_dev_processes
start_dev_processes
wait_vite
wait_spring

E2E_EXIT=0
run_tests || E2E_EXIT=$?

kill_dev_processes
echo ">>> Tests terminés (exit $E2E_EXIT)"
exit $E2E_EXIT
