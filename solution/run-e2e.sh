#!/usr/bin/env bash
# run-e2e.sh — Lance la suite E2E complète avec gestion propre des processus.
# Usage : bash run-e2e.sh [-- <playwright-flags>]

COMPOSE_FILE="$(cd "$(dirname "$0")" && pwd)/docker-compose.dev.yml"

EXTRA_ARGS=""
for arg in "$@"; do
  [[ "$arg" == "--" ]] && continue
  EXTRA_ARGS="$EXTRA_ARGS $arg"
done
EXTRA_ARGS="${EXTRA_ARGS# }"

BACKEND=shop-backend-1
VENDOR=shop-vendor-backoffice-1
BUYER=shop-buyer-portal-1
ADMIN=shop-admin-console-1

die() { echo "ERROR: $*" >&2; exit 1; }

kill_dev() {
  echo ">>> Kill processus dev existants"
  docker exec "$BACKEND" bash -c \
    "pkill -9 -f 'spring-boot:run' 2>/dev/null; pkill -9 -f 'ShopApplication' 2>/dev/null; true" || true
  for c in "$VENDOR" "$BUYER" "$ADMIN"; do
    docker exec "$c" bash -c \
      "pkill -9 -f 'vite' 2>/dev/null; pkill -9 -f 'node.*dev' 2>/dev/null; true" || true
  done
  sleep 2
}

install_frontend_deps() {
  echo ">>> npm install (workspaces)"
  docker exec "$VENDOR" bash -c "cd /workspace && npm install --workspaces --silent 2>/dev/null" || true
}

start_vite() {
  local container="$1" prefix="$2"
  docker exec "$container" bash -c \
    "nohup npm run --prefix /workspace/${prefix} dev -- --host --port 5173 >/tmp/vite.log 2>&1 &"
}

start_dev() {
  echo ">>> Démarrage Vite (3 SPAs)"
  start_vite "$VENDOR" "vendor-backoffice"
  start_vite "$BUYER"  "buyer-portal"
  start_vite "$ADMIN"  "admin-console"

  echo ">>> Démarrage Spring Boot"
  docker exec "$BACKEND" bash -c \
    "nohup mvn -f /workspace/pom.xml spring-boot:run >/tmp/spring.log 2>&1 &"
}

# Attend que le port $2 accepte des connexions TCP depuis l'intérieur du container $1.
wait_port_inside() {
  local container="$1" port="${2:-5173}" label="$3" max="${4:-40}"
  echo -n "    $label "
  i=0
  until docker exec "$container" bash -c \
      "(exec 3<>/dev/tcp/localhost/${port}) 2>/dev/null"; do
    echo -n "."
    sleep 3
    i=$((i+1))
    if [[ $i -ge $max ]]; then
      echo " TIMEOUT"
      echo "--- dernières lignes de /tmp/vite.log dans $container ---"
      docker exec "$container" tail -20 /tmp/vite.log 2>/dev/null || true
      die "Vite timeout dans $container"
    fi
  done
  echo " prêt"
}

wait_http_host() {
  local url="$1" label="$2" max="${3:-50}" interval="${4:-8}"
  echo -n "    $label "
  i=0
  until curl -sf "$url" >/dev/null 2>&1; do
    echo -n "."
    sleep "$interval"
    i=$((i+1))
    if [[ $i -ge $max ]]; then
      echo " TIMEOUT"
      echo "--- dernières lignes de /tmp/spring.log ---"
      docker exec "$BACKEND" tail -20 /tmp/spring.log 2>/dev/null || true
      die "Spring Boot timeout"
    fi
  done
  echo " prêt"
}

reset_db() {
  echo "    [db] reset"
  docker exec "$VENDOR" bash -c \
    "cd /workspace/e2e && node --env-file=.env.docker \
      -e \"import('./helpers/db-reset.js').then(m=>m.resetDatabase()).catch(e=>{console.error(e);process.exit(1)})\"" \
    2>/dev/null || true
}

run_project() {
  local project="$1"
  echo ">>> Projet : $project"
  docker exec "$VENDOR" bash -c \
    "cd /workspace/e2e && npm run test:docker -- --project=${project}" || true
  reset_db
}

run_tests() {
  echo ">>> Lancement tests E2E (depuis vendor-backoffice, reset DB entre chaque projet)"
  if [[ -n "$EXTRA_ARGS" ]]; then
    # Mode custom : on ne splitte pas par projet
    docker exec "$VENDOR" bash -c "cd /workspace/e2e && npm run test:docker -- $EXTRA_ARGS" || true
    return
  fi
  # Chaque projet repart sur une BDD propre — limite l'accumulation de données
  run_project admin-console
  run_project buyer-portal
  run_project vendor-portal
  run_project auth
  run_project cart
  run_project order
}

# ── Main ────────────────────────────────────────────────────────────────────

kill_dev
install_frontend_deps
start_dev

# Vite : check TCP port 5173 depuis l'intérieur de chaque container (pas besoin de curl)
wait_port_inside "$VENDOR" 5173 "vendor-vite"
wait_port_inside "$BUYER"  5173 "buyer-vite"
wait_port_inside "$ADMIN"  5173 "admin-vite"

# Spring Boot : check depuis le host via port mappé 8080
wait_http_host "http://localhost:8080/api/public/theme" "spring-boot"

E2E_EXIT=0
run_tests || E2E_EXIT=$?

kill_dev
echo ""
echo ">>> Tests terminés (exit $E2E_EXIT)"
exit $E2E_EXIT
