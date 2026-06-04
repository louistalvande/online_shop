#!/usr/bin/env bash
# run-e2e.sh — Lance la suite E2E complète avec gestion propre des processus.
# Usage : bash run-e2e.sh [-- <playwright-flags>]
#
# Cycle :
#   1. Kill processus dev (Vite + Spring Boot)
#   2. npm install workspaces
#   3. vite build (x3 SPAs) → bundles statiques
#   4. Switch nginx → mode statique (nginx.test.conf)
#   5. Démarre Spring Boot, attend readiness
#   6. Lance le container dédié shop-e2e
#   7. Restaure nginx → mode dev (nginx.dev.conf) + kill Spring Boot

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
  echo ">>> Kill processus dev"
  docker exec "$BACKEND" bash -c \
    "pkill -9 -f 'spring-boot:run' 2>/dev/null; pkill -9 -f 'ShopApplication' 2>/dev/null; true" || true
  for c in "$VENDOR" "$BUYER" "$ADMIN"; do
    docker exec "$c" bash -c \
      "pkill -9 -f 'vite' 2>/dev/null; pkill -9 -f 'node.*dev' 2>/dev/null; true" || true
  done
  sleep 2
}

install_and_build() {
  echo ">>> npm install (workspaces)"
  docker exec "$VENDOR" bash -c "cd /workspace && npm install --workspaces --silent 2>/dev/null" || true

  # npx vite build bypasses tsc type-checking (not needed for E2E)
  echo ">>> vite build (vendor-backoffice)"
  docker exec "$VENDOR" bash -c \
    "cd /workspace/vendor-backoffice && npx vite build 2>&1 | tail -3" \
    || die "Build vendor-backoffice échoué"
  echo ">>> vite build (buyer-portal)"
  docker exec "$BUYER" bash -c \
    "cd /workspace/buyer-portal && npx vite build 2>&1 | tail -3" \
    || die "Build buyer-portal échoué"
  echo ">>> vite build (admin-console)"
  docker exec "$ADMIN" bash -c \
    "cd /workspace/admin-console && npx vite build 2>&1 | tail -3" \
    || die "Build admin-console échoué"
}

nginx_switch() {
  local conf="$1"
  echo ">>> nginx → $conf"
  docker exec "$VENDOR" bash -c \
    "cp /workspace/vendor-backoffice/${conf} /etc/nginx/nginx.conf && nginx -s reload"
  docker exec "$BUYER" bash -c \
    "cp /workspace/buyer-portal/${conf} /etc/nginx/nginx.conf && nginx -s reload"
  docker exec "$ADMIN" bash -c \
    "cp /workspace/admin-console/${conf} /etc/nginx/nginx.conf && nginx -s reload"
  sleep 1
}

nginx_test_mode() { nginx_switch nginx.test.conf; }
nginx_dev_mode()  { nginx_switch nginx.dev.conf || true; }

start_spring() {
  echo ">>> Démarrage Spring Boot"
  docker exec "$BACKEND" bash -c \
    "nohup mvn -f /workspace/pom.xml spring-boot:run >/tmp/spring.log 2>&1 &"
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
      docker exec "$BACKEND" tail -20 /tmp/spring.log 2>/dev/null || true
      die "Spring Boot timeout"
    fi
  done
  echo " prêt"
}

run_tests() {
  echo ">>> Lancement tests E2E (container dédié shop-e2e)"
  if [[ -n "$EXTRA_ARGS" ]]; then
    docker compose -f "$COMPOSE_FILE" --profile e2e run --rm e2e \
      sh -c "npm install --silent 2>/dev/null; npm run test:ci -- $EXTRA_ARGS"
  else
    docker compose -f "$COMPOSE_FILE" --profile e2e run --rm e2e
  fi
}

cleanup() {
  nginx_dev_mode
  kill_dev
}

# ── Main ────────────────────────────────────────────────────────────────────

kill_dev
install_and_build
nginx_test_mode
start_spring
wait_http_host "http://localhost:8080/api/public/theme" "spring-boot"

E2E_EXIT=0
run_tests || E2E_EXIT=$?

cleanup
echo ""
echo ">>> Tests terminés (exit $E2E_EXIT)"
exit $E2E_EXIT
