#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
dump_path="$repository_root/awesome-button-compose/api/awesome-button-compose.api"

cd "$repository_root"
./gradlew :awesome-button-compose:apiCheck

if [[ ! -s "$dump_path" ]]; then
  echo "ABI dump is missing or empty: $dump_path" >&2
  exit 1
fi

if ! grep -Eq '^public .* class dev/caferati/awesomebutton/' "$dump_path"; then
  echo "ABI dump contains no parsed public declarations in dev.caferati.awesomebutton." >&2
  exit 1
fi

for required_symbol in \
  AwesomeButtonKt \
  ThemedButtonKt \
  AwesomeButtonStyle \
  AwesomeButtonThemeData \
  AwesomeButtonNext; do
  if ! grep -Fq "$required_symbol" "$dump_path"; then
    echo "ABI dump is missing required symbol: $required_symbol" >&2
    exit 1
  fi
done

echo "Validated non-empty standalone-BCV dump: $dump_path"
