#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temporary_root="$(mktemp -d "${TMPDIR:-/tmp}/awesome-button-release.XXXXXX")"
maven_root="$temporary_root/m2"

cleanup() {
  rm -rf "$temporary_root"
}
trap cleanup EXIT

cd "$repository_root"

./gradlew \
  :awesome-button-compose:ktlintCheck \
  :awesome-button-compose:testDebugUnitTest \
  :awesome-button-compose:koverXmlReportDebug \
  :awesome-button-compose:koverHtmlReportDebug \
  :awesome-button-compose:lintDebug \
  :awesome-button-compose:dokkaGenerate \
  :awesome-button-compose:assembleRelease \
  :awesome-button-compose:assembleDebugAndroidTest

scripts/check-abi.sh
scripts/test-abi-gate.sh

./gradlew :awesome-button-compose:publishToMavenLocal \
  -Dmaven.repo.local="$maven_root"

version_name="$(awk -F= '$1 == "VERSION_NAME" { print $2 }' gradle.properties)"
publication_root="$maven_root/dev/caferati/awesome-button-compose/$version_name"
required_artifacts=(
  "awesome-button-compose-$version_name.aar" \
  "awesome-button-compose-$version_name.module" \
  "awesome-button-compose-$version_name.pom" \
  "awesome-button-compose-$version_name-sources.jar" \
  "awesome-button-compose-$version_name-javadoc.jar"
)
for required_artifact in "${required_artifacts[@]}"; do
  if [[ ! -s "$publication_root/$required_artifact" ]]; then
    echo "Isolated Maven publication is missing $required_artifact." >&2
    exit 1
  fi
done

unexpected_publication_files=()
while IFS= read -r -d '' publication_file; do
  relative_path="${publication_file#"$maven_root/"}"
  allowed=false
  if [[ "$relative_path" == "dev/caferati/awesome-button-compose/maven-metadata-local.xml" ]]; then
    allowed=true
  else
    for required_artifact in "${required_artifacts[@]}"; do
      if [[ "$relative_path" == "dev/caferati/awesome-button-compose/$version_name/$required_artifact" ]]; then
        allowed=true
        break
      fi
    done
  fi
  if [[ "$allowed" == false ]]; then
    unexpected_publication_files+=("$relative_path")
  fi
done < <(find "$maven_root" -type f -print0)

if (( ${#unexpected_publication_files[@]} > 0 )); then
  printf 'Isolated Maven publication contains unexpected files:\n' >&2
  printf '  %s\n' "${unexpected_publication_files[@]}" >&2
  exit 1
fi

pom_path="$publication_root/awesome-button-compose-$version_name.pom"
for pom_entry in \
  '<groupId>dev.caferati</groupId>' \
  '<artifactId>awesome-button-compose</artifactId>' \
  "<version>$version_name</version>"; do
  if ! grep -Fq "$pom_entry" "$pom_path"; then
    echo "Published POM is missing expected entry: $pom_entry" >&2
    exit 1
  fi
done

for archive_path in \
  "$publication_root/awesome-button-compose-$version_name.aar" \
  "$publication_root/awesome-button-compose-$version_name-sources.jar" \
  "$publication_root/awesome-button-compose-$version_name-javadoc.jar"; do
  if jar tf "$archive_path" | grep -Eiq '(^|/)(demo|test)(/|$)'; then
    echo "Published archive contains demo or test content: $(basename "$archive_path")" >&2
    exit 1
  fi
done

if [[ "${AWESOME_BUTTON_SKIP_MANAGED_DEVICE:-0}" == "1" ]]; then
  echo "Managed-device execution explicitly skipped; runtime evidence remains pending."
else
  ./gradlew :awesome-button-compose:pixel2Api35DebugAndroidTest \
    -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
fi

echo "Kotlin package release preflight completed without publishing."
