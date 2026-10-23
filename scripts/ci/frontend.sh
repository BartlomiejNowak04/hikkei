#!/usr/bin/env bash
# Frontend gate: install, ESLint, production bundle, type check, dead code.
#
# Existing findings are ratcheted, not fixed here: ESLint errors through
# frontend/eslint-suppressions.json (plus a warning cap), TypeScript errors through
# frontend/tsc-baseline.json and knip findings through frontend/knip.jsonc.
# New findings fail; fixed ones ask you to shrink the baseline.
#
# Usage: scripts/ci/frontend.sh [all|lint|build]   (default: all)
#   lint:  ESLint and knip    build: production bundle and type check
set -euo pipefail

mode="${1:-all}"
case "$mode" in
  all | lint | build) ;;
  *)
    echo "usage: $0 [all|lint|build]" >&2
    exit 2
    ;;
esac

cd "$(dirname "$0")/../../frontend"

export NEXT_TELEMETRY_DISABLED=1
# next.config.ts builds its /api rewrite from this; any absolute URL works for a build.
export NEXT_PUBLIC_API_BASE="${NEXT_PUBLIC_API_BASE:-http://localhost:8080}"
# Keep npm's cache under $HOME so CI runs use their own.
export npm_config_cache="${npm_config_cache:-$HOME/.npm}"
export npm_config_update_notifier=false

ESLINT_MAX_WARNINGS=11
KNIP_VERSION=6.42.1

echo "== npm ci"
npm ci --no-audit --no-fund

if [ "$mode" != build ]; then
  echo "== eslint"
  npx --no-install eslint src --max-warnings "$ESLINT_MAX_WARNINGS"

  echo "== knip"
  npx -y "knip@$KNIP_VERSION" --no-progress
fi

if [ "$mode" = lint ]; then
  exit 0
fi

echo "== next build"
# Compile mode bundles every route but leaves type checking to the step below, so the
# type errors that exist today are ratcheted instead of failing the build.
rm -rf .next
npx --no-install next build --no-lint --experimental-build-mode=compile

echo "== tsc"
# Runs after the build, which generates .next/types (the route prop checks of `next build`).
tsc_output="$(mktemp)"
trap 'rm -f "$tsc_output"' EXIT
npx --no-install tsc --noEmit --pretty false >"$tsc_output" 2>&1 || true
python3 ../scripts/ci/tsc_baseline.py tsc-baseline.json <"$tsc_output"
