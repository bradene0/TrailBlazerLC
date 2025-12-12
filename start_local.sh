#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/back_end"
FRONTEND_DIR="$ROOT_DIR/front_end"
BACKEND_LOG="$BACKEND_DIR/build/bootRun.log"

HEALTH_URL="${HEALTH_URL:-http://localhost:8080/health}"
BACKEND_READY_TIMEOUT_SECONDS="${BACKEND_READY_TIMEOUT_SECONDS:-60}"

command -v java >/dev/null 2>&1 || { echo "Java is required (Java 17+ recommended)."; exit 1; }
command -v node >/dev/null 2>&1 || { echo "Node.js is required (v18+ recommended)."; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "npm is required."; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "curl is required for backend readiness checks."; exit 1; }

if [ ! -x "$BACKEND_DIR/gradlew" ]; then
  echo "Making gradlew executable..."
  chmod +x "$BACKEND_DIR/gradlew"
fi

mkdir -p "$BACKEND_DIR/build"

echo "Starting Spring Boot API (logs -> $BACKEND_LOG)"
(
  cd "$BACKEND_DIR"
  ./gradlew bootRun --no-daemon >"$BACKEND_LOG" 2>&1
) &
BACKEND_PID=$!

cleanup() {
  echo "Stopping Spring Boot API (pid $BACKEND_PID)"
  kill "$BACKEND_PID" 2>/dev/null || true
}
trap cleanup EXIT

wait_for_backend() {
  echo "Waiting for backend to become ready on ${HEALTH_URL} ..."
  local attempts=0
  local max_attempts=$(( (BACKEND_READY_TIMEOUT_SECONDS + 1) / 2 ))
  while [ "$attempts" -lt "$max_attempts" ]; do
    if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
      echo "Backend process exited early; check $BACKEND_LOG"
      if [ -f "$BACKEND_LOG" ]; then
        echo "---- Last 120 lines of backend log ----"
        tail -n 120 "$BACKEND_LOG" || true
        echo "--------------------------------------"
      fi
      return 1
    fi

    if curl -fsS "${HEALTH_URL}" >/dev/null 2>&1; then
      echo "Backend is ready."
      return 0
    fi

    attempts=$((attempts + 1))
    sleep 2
  done

  echo "Backend did not become ready within ${BACKEND_READY_TIMEOUT_SECONDS} seconds; check $BACKEND_LOG"
  if [ -f "$BACKEND_LOG" ]; then
    echo "---- Last 120 lines of backend log ----"
    tail -n 120 "$BACKEND_LOG" || true
    echo "--------------------------------------"
  fi
  return 1
}

wait_for_backend

if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
  echo "Installing frontend dependencies..."
  (cd "$FRONTEND_DIR" && npm install)
fi

echo "Starting React dev server..."
cd "$FRONTEND_DIR"
npm start
