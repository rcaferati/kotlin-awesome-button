# Kotlin Awesome Button

## Goal
Build `kotlin-awesome-button` as an Android Jetpack Compose package that ports the native Awesome Button model from `flutter_awesome_button` and `swift-awesome-button`.

The package publishes as:

- Maven group: `dev.caferati`
- Artifact: `awesome-button-compose`
- Kotlin namespace: `dev.caferati.awesomebutton`
- Android namespace: `dev.caferati.awesomebutton`

## Public API
The library exposes:

- `AwesomeButton`
- `ThemedButton`
- `AwesomeButtonStyle`
- `AwesomeButtonThemeData`
- `AwesomeButtonTheme`
- `getTheme(index: Int = 0)`
- `getTheme(name: ThemeName)`
- `ThemeName`
- `ButtonVariant`
- `ButtonSize`
- `ThemeButtonStyle`
- `ThemeSizeStyle`
- `ThemeDefinition`
- `RegisteredThemeDefinition`

Progress mode uses:

```kotlin
fun interface AwesomeButtonNext {
    operator fun invoke(callback: (() -> Unit)? = null)
}

typealias AwesomeButtonPressCallback = (AwesomeButtonNext?) -> Unit
```

## Implementation Contract
`AwesomeButton` renders three independent layers:

- Shadow plane: 98% shell width, translucent, height `max(0.dp, height - raiseAmount)`.
- Bottom shell: fixed darker layer, full face width.
- Face: moving interactive layer translated by `raiseAmount * pressValue`.

The Kotlin version follows the Flutter geometry convention:

- Public `height` is the moving face height.
- Total stack height is `height + raiseAmount`.
- Shadow height is `max(0.dp, height - raiseAmount)`.

Interaction requirements:

- Press feedback starts on pointer down.
- Drag-out, tap cancel, disabled, and busy states do not dispatch.
- Successful taps dispatch once.
- Progress mode is one-shot and guarded until completion finishes.
- `next()` completes progress, restores content, releases the press state, and unlocks the button.

## Scope
Version 1 targets Android Jetpack Compose only. Compose Multiplatform, Android View/XML widgets, and React/Vue social-share wrappers are out of scope.

Social variants are visual button themes only.
