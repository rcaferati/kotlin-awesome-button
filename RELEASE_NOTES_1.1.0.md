# Awesome Button Compose 1.1.0

Awesome Button Compose 1.1.0 makes gesture, progress, sizing, and text-transition behavior more
predictable while expanding native accessibility and package-quality coverage.

## Highlights

- Keeps callbacks current across recomposition while preserving transition-scoped release and
  progress completion behavior.
- Strengthens structured cancellation, long-press replacement, one-shot progress completion,
  Reduced Motion, large-text, RTL, and numeric validation behavior.
- Preserves flat progress and disabled styling while correcting activity, text, shadow, and
  edge-to-edge demo rendering.
- Adds canonical `x` vocabulary while retaining `Twitter` as a deprecated compatibility value.
- Adds deterministic ABI, Dokka, ktlint, Kover, instrumentation, publication-shape, and regression
  gates.

## Compatibility

- The published coordinate remains `dev.caferati:awesome-button-compose`.
- The reviewed ABI dump governs the complete 1.1.0 public Compose surface.
- Existing `Twitter` call sites remain source-compatible.

## Verification

- `scripts/release-preflight.sh` — passed.
- JVM tests, API 35 instrumentation, ktlint, lint, Dokka, Kover, ABI validation, release assembly,
  publication-shape validation, and the compatibility canary — passed.

## Installation

```kotlin
implementation("dev.caferati:awesome-button-compose:1.1.0")
```

## Full Changelog

See [v1.0.0...v1.1.0](https://github.com/rcaferati/kotlin-awesome-button/compare/v1.0.0...v1.1.0).
