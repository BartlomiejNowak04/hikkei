#!/usr/bin/env bash
# The whole gate, as Yulius CI runs it: every job of yulius.toml [[ci.jobs]] in turn.
set -euo pipefail
cd "$(dirname "$0")/../.."
scripts/ci/scripts.sh
scripts/ci/backend.sh
scripts/ci/frontend.sh
