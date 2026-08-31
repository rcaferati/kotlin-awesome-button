# Performance Evidence

Pass 5 treats performance evidence as informational and defines no wall-clock release threshold.

Compose compiler metrics and stability reports are emitted under `awesome-button-compose/build/compose_compiler/` during package compilation. `AwesomeButtonCompositionLifecycleInstrumentedTest` records content-composition counts for auto-width sizing changes and progress configuration changes while asserting single lifecycle ownership and disposal behavior.

Run the package measurement on the API 35 AOSP ATD device with:

```sh
./gradlew :awesome-button-compose:pixel2Api35DebugAndroidTest \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
```

The local 2026-08-30 host is a 14-core Apple M3 Max MacBook Pro with 36 GB memory, macOS 26.6.2, JDK 21.0.10, Gradle 8.14.4, AGP 8.13.2, and Kotlin/Compose 2.2.21. A forced debug package compilation emitted 24 composables, of which 17 were skippable, and 253 arguments, of which 240 were known stable. These are diagnostic baselines, not release thresholds or performance claims. No Android device was available; runtime composition counts remain pending the pinned hosted JDK 17/API 35 run. Future changes must record runner, build mode, warm-up, repetitions, median, dispersion where timing is measured, and before/after composition evidence. No README performance claim is authorized.
