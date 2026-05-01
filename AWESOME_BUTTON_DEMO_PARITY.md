# Kotlin Awesome Button Demo Parity

## Summary
The Kotlin demo is being brought to parity with the native-style Awesome Button demos from `flutter_awesome_button` and `swift-awesome-button`. The original Kotlin demo was a single scrolling `MainActivity.kt` with a handful of examples. The parity target is a structured Android Jetpack Compose app with the same manual QA surface as the Flutter and SwiftUI demos:

- four tabs: Themed, Progress, Social, and Size Changes
- dynamic themed header for theme navigation
- static secondary headers for non-themed tabs
- reusable demo layout primitives
- animated character overlays on themed pages
- progress, social, icon, placeholder, flat, auto-width, stretch, and size-transition examples

SwiftUI is the canonical reference where the Flutter and SwiftUI demos differ. The main current difference is the social demo: Kotlin uses the SwiftUI `X` social example instead of Flutter's older Twitter example while keeping `Twitter` in the library API for compatibility.

## Source Architecture
The parity model maps the reference demos to Kotlin Compose as follows:

| Reference | Kotlin Compose target |
| --- | --- |
| Flutter `_DemoShell` / SwiftUI `DemoShell` | `DemoShell` owns selected tab, theme stack, header state, back handling, and bottom navigation. |
| Flutter `_AnimatedThemedAppBar` / SwiftUI `ThemedHeaderBar` | `ThemedHeaderBar` animates theme background/foreground and exposes Prev/Next themed buttons. |
| Flutter `_DemoContainer` / SwiftUI `DemoContainer` | `DemoContainer` scrolls content, centers it, constrains width to `520.dp`, and applies top/bottom padding. |
| Flutter `_DemoSection` / SwiftUI `DemoSection` | `DemoSection` renders uppercase underlined section headings with reusable content spacing. |
| Flutter `_ThemeCharacterOverlay` / SwiftUI `ThemeCharacterOverlay` | `ThemeCharacterOverlay` loads demo-only PNG assets and animates them in with the converted native spring. |
| Flutter/SwiftUI screen structs | `ThemedButtonsScreen`, `ProgressScreen`, `SocialScreen`, and `SizeChangesScreen`. |

## Screen Matrix
The Themed tab cycles through all registered themes with Prev/Next and Android back-stack behavior. Each theme page includes:

- Common: primary, secondary, anchor, danger, and disabled buttons.
- Progress: progress versions of primary, secondary, anchor, and danger.
- Variant Transition: a button cycling primary, secondary, anchor, and danger.
- Text Transition: string label transitions for `welcome`, `Level 2`, `Mission#42`, and `Go#3`.
- Size Transition: auto-width transition between `Launch` and `View analytics dashboard`.
- Empty Placeholder: placeholder buttons for the four core variants.
- Flat Buttons: zero-raise versions of core variants, including progress danger.
- Before / After / Icon: before icon, after icon, progress icon, and icon-only buttons.
- With auto and stretch: auto-width small/medium/large examples and stretch progress with an app-local async task.

The Progress tab uses the Mysterion theme and includes fixed-width progress, slower progress loading, hidden progress bar, flat progress, and round icon progress examples.

The Social tab uses the Bojack theme and includes Facebook, LinkedIn, Messenger, Instagram gradient, WhatsApp, YouTube, X, and Pinterest. Social variants remain visual-only examples; no sharing wrappers are introduced.

The Size Changes tab includes auto-width string changes and themed fixed-size changes, each showing animated text transition, animated size without text transition, and instant opt-out cases.

## Assets And Compatibility
Demo-only character PNGs are copied from the Flutter demo into `demo/src/main/res/drawable-nodpi`. Social icons are Android vector drawables ported from the SwiftUI demo asset catalog.

The library compatibility change is intentionally small:

- `ButtonVariant.X` is added.
- `ButtonVariant.Twitter` remains available.
- every built-in theme maps `ButtonVariant.X` to SwiftUI colors: background `#171717`, darker layer `#050505`.

The demo module may use Material3, Material icons, and Android resources. The `awesome-button-compose` library remains Material-independent.

## Verification Checklist
Run:

```bash
./gradlew :awesome-button-compose:testDebugUnitTest :demo:assembleDebug
```

Manual emulator acceptance:

- install and launch the demo on the Pixel emulator
- verify the four bottom-navigation tabs render
- verify Prev/Next updates themed header colors, title, page examples, and character overlay
- verify Android back pops the themed stack before leaving the app
- verify progress buttons complete and unlock
- verify social icons render and tint correctly
- verify the Instagram example keeps the gradient behind content
- verify size-change examples animate or opt out as labeled

## Assumptions
Demo parity means architecture and manual QA surface parity, not pixel-perfect platform chrome. SwiftUI is canonical for conflicts. Demo-only helpers and assets stay in the demo module; the library only changes for the compatible `ButtonVariant.X` addition.
