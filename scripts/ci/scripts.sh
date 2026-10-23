#!/usr/bin/env bash
# Lint the repository's own shell scripts (CI and git hooks).
set -euo pipefail
cd "$(dirname "$0")/../.."
shellcheck --version | head -n 2
shellcheck -x scripts/ci/*.sh .githooks/pre-commit
python3 -c "import ast, sys; ast.parse(open(sys.argv[1]).read(), sys.argv[1])" scripts/ci/tsc_baseline.py
