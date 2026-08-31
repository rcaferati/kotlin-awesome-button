#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/awesome-button-abi-canary.XXXXXX")"
temporary_repository="$temporary_root/repository"
canary_log="$temporary_root/api-check.log"

cleanup() {
  rm -rf "$temporary_root"
}
trap cleanup EXIT

"$repository_root/scripts/check-abi.sh"

mkdir -p "$temporary_repository"
rsync -a \
  --exclude '.git/' \
  --exclude '.gradle/' \
  --exclude '.kotlin/' \
  --exclude 'build/' \
  --exclude '*/build/' \
  "$repository_root/" "$temporary_repository/"

canary_source="$temporary_repository/awesome-button-compose/src/main/kotlin/dev/caferati/awesomebutton/AbiBreakageCanary.kt"
printf '%s\n' \
  'package dev.caferati.awesomebutton' \
  '' \
  'public class AbiBreakageCanary public constructor()' \
  > "$canary_source"

set +e
(
  cd "$temporary_repository"
  ./gradlew :awesome-button-compose:apiCheck --no-daemon
) >"$canary_log" 2>&1
canary_status=$?
set -e

cat "$canary_log"

if [[ $canary_status -eq 0 ]]; then
  echo "ABI canary failed: apiCheck accepted an added public class." >&2
  exit 1
fi

if ! grep -Fq 'AbiBreakageCanary' "$canary_log"; then
  echo "ABI canary failed for a reason that did not name AbiBreakageCanary." >&2
  exit 1
fi

if ! grep -Eiq '(api.*(change|diff)|public api|api check)' "$canary_log"; then
  echo "ABI canary did not produce an API-difference diagnostic." >&2
  exit 1
fi

echo "ABI canary rejected the isolated public declaration as expected."
