# Kotlin API Compatibility Review

The reviewed standalone Binary Compatibility Validator 0.18.1 baseline is `awesome-button-compose/api/awesome-button-compose.api`. It represents the accepted post–Pass 4 surface, is non-empty, and includes the public anchors `AwesomeButtonKt`, `ThemedButtonKt`, `AwesomeButtonStyle`, `AwesomeButtonThemeData`, and `AwesomeButtonNext`.

## Comparison with `v1.0.0`

The current work retains the 1.0.0 entry points and adds the Passes 1–4 contract surface: canonical `x` compatibility, accessibility text, Reduced Motion-aware timing ownership, explicit press-down timing, per-corner and disabled style fields, and the associated theme values. `Twitter` remains a deprecated callable compatibility value. Explicit visibility and return types do not intentionally change the source contract; the checked-in binary dump is authoritative for JVM signatures.

These additive changes have not selected or published a new version. A release owner must review the complete ABI diff and migration notes before choosing a version. `scripts/check-abi.sh` enforces the accepted dump, while `scripts/test-abi-gate.sh` proves in an isolated copy that an added public `AbiBreakageCanary` fails for the expected API-difference reason.
