# Contributing

## Toolchain

- Temurin JDK 17 for CI and release evidence
- Gradle 8.14.4 wrapper
- Android Gradle Plugin 8.13.2
- Kotlin and Compose compiler 2.2.21

Run the complete non-publishing package preflight with:

```sh
scripts/release-preflight.sh
```

That command runs formatting, JVM tests and informational Kover reports, Android lint, fatal-undocumented Dokka generation, release/instrumentation assembly, standalone binary-API checks and their isolated known-break canary, isolated Maven publication-shape validation, and the API 35 AOSP ATD managed device. It never updates an ABI dump or publishes.

When a local Android runtime is unavailable, run the host-only evidence with:

```sh
AWESOME_BUTTON_SKIP_MANAGED_DEVICE=1 scripts/release-preflight.sh
```

That opt-out is not runtime completion evidence. The required hosted managed-device job must still pass.

Intentional public API changes use `./gradlew :awesome-button-compose:apiDump`, followed by human review of `awesome-button-compose/api/awesome-button-compose.api`, `scripts/check-abi.sh`, `scripts/test-abi-gate.sh`, and an update to `CHANGELOG.md` plus `ABI_REVIEW.md`. CI never runs `apiDump`.

Tests belong to `awesome-button-compose/src/test` or `awesome-button-compose/src/androidTest`. The demo is a showcase and is not an automated test target.
