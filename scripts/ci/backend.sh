#!/usr/bin/env bash
# Backend gate: formatting (spotless), compilation, unit and integration tests, PMD
# unused-code rules, the jar and the unused-dependency check (buildHealth).
#
# Runs `./gradlew build` against a disposable PostgreSQL container, so the JDBC and
# route integration tests run instead of being skipped. The tests read their
# settings from the environment (or `.test-env`, see README "Testing").
#
# Usage: scripts/ci/backend.sh [gradle tasks...]   (default: cleanTest build buildHealth;
# Gradle does not track the database variables, so cleanTest makes the tests always rerun)
# Set HIKKEI_TEST_DB=none to skip the container (integration tests are then skipped).
set -euo pipefail

cd "$(dirname "$0")/../.."

# Gradle defaults to the passwd home (user.home), not $HOME; keep it under $HOME so
# CI runs use their own cache.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

PG_IMAGE="postgres:16.10-alpine@sha256:029660641a0cfc575b14f336ba448fb8a75fd595d42e1fa316b9fb4378742297"
container=""

cleanup() {
  if [ -n "$container" ]; then
    docker rm -f "$container" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

free_port() {
  python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1", 0)); print(s.getsockname()[1])'
}

if [ "${HIKKEI_TEST_DB:-docker}" = "docker" ]; then
  container="hikkei-ci-pg-$$-$(date +%s%N)"
  docker run -d --rm --name "$container" \
    -e POSTGRES_USER=hikkei -e POSTGRES_PASSWORD=hikkei -e POSTGRES_DB=hikkei \
    -p 127.0.0.1::5432 "$PG_IMAGE" >/dev/null
  pg_port="$(docker port "$container" 5432/tcp | head -n1 | sed 's/.*://')"

  # Wait until the server accepts connections over TCP (the init phase runs on a socket).
  ready=""
  for _ in $(seq 1 120); do
    if docker exec "$container" pg_isready -h 127.0.0.1 -U hikkei -d hikkei >/dev/null 2>&1; then
      ready=1
      break
    fi
    sleep 0.5
  done
  if [ -z "$ready" ]; then
    echo "PostgreSQL did not become ready" >&2
    docker logs "$container" >&2 || true
    exit 1
  fi

  export POSTGRES_HOST="127.0.0.1:$pg_port"
  export POSTGRES_NAME=hikkei POSTGRES_USER=hikkei POSTGRES_PASS=hikkei
  PORT="$(free_port)"
  export PORT
  echo "PostgreSQL ($PG_IMAGE) on $POSTGRES_HOST, test HTTP port $PORT"
fi

if [ "$#" -eq 0 ]; then
  set -- cleanTest build buildHealth
fi

started="$(date +%s)"
./gradlew --console=plain ${CI:+--no-daemon} "$@"

# Fail when the integration tests were silently skipped although a database was provided.
if [ -n "$container" ] && [ -d build/test-results/test ]; then
  python3 - "$started" <<'EOF'
import glob, os, sys, xml.etree.ElementTree as ET
started = int(sys.argv[1])
results = [p for p in glob.glob("build/test-results/test/*.xml") if os.path.getmtime(p) >= started]
skipped = [
    (case.get("classname"), case.get("name"))
    for path in results
    for case in ET.parse(path).getroot().iter("testcase")
    if case.find("skipped") is not None
]
if skipped:
    print(f"{len(skipped)} tests were skipped although the test database was up:", file=sys.stderr)
    for cls, name in skipped[:20]:
        print(f"  {cls} > {name}", file=sys.stderr)
    sys.exit(1)
if results:
    print(f"All tests ran (none skipped, {len(results)} result files).")
EOF
fi
