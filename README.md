# Kotlin Awesome Button

`awesome-button-compose` brings the Awesome Button interaction, progress,
sizing, and theme system to Android with native Jetpack Compose recomposition,
coroutines, semantics, and animation behavior.

The library exports:

- `AwesomeButton`
- `ThemedButton`
- `getTheme`
- `AwesomeButtonStyle`
- `AwesomeButtonThemeData`
- `AwesomeButtonTheme`
- `ThemeName`, `ButtonVariant`, and `ButtonSize`
- `ThemeButtonStyle`, `ThemeSizeStyle`, `ThemeDefinition`, and `RegisteredThemeDefinition`

<table>
  <tr>
    <td width="33%">
      <img
        alt="Blue Awesome Button theme demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-blue-new.gif"
      />
    </td>
    <td width="33%">
      <img
        alt="Cartman Awesome Button theme demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-cartman.gif"
      />
    </td>
    <td width="33%">
      <img
        alt="Rick Awesome Button theme demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-rick.gif"
      />
    </td>
  </tr>
</table>

## Figma File

Explore the shared Awesome Button visual system in the [Figma design file](https://www.figma.com/file/Ug8sNPzmevU3ZQus9Klu5aHq/react-awesome-button-theme-blue). The Figma file is a visual design reference; this package's documentation defines its behavior, accessibility, and public API contract.

## Installation

Make sure the consuming app resolves dependencies from Maven Central:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
dependencies {
    implementation("dev.caferati:awesome-button-compose:1.1.0")
}
```

Current Android support:

- Android minSdk 23
- Android compileSdk 36
- Kotlin 2.2.21
- Android Gradle Plugin 8.13.2
- Jetpack Compose BOM 2026.02.01
- Jetpack Compose UI, Foundation, Runtime, and Animation

This first package targets Android Jetpack Compose only. Compose Multiplatform
and Android View/XML widgets are out of scope for v1.

## Basic Usage

```kotlin
import dev.caferati.awesomebutton.AwesomeButton

@Composable
fun SaveButton() {
    AwesomeButton(
        child = "Save",
        onPress = {
            // Handle press.
        },
    )
}
```

`AwesomeButton` supports plain string labels and arbitrary Compose row content.

```kotlin
AwesomeButton(
    onPress = { /* Handle press. */ },
) {
    Text("Save")
}
```

## Features

### Size Changes

`animateSize` is enabled by default.

- fixed `width` / `height` changes use the package's 175 ms size transition
- `ThemedButton` size preset changes animate because they resolve to fixed
  width and height updates
- auto-width string labels grow and shrink from their measured target widths
- `textTransition` uses Unicode grapheme clusters and a 7 ms slot stagger
- without `textTransition`, wider labels wait until the target fits while
  narrower labels replace before width shrinks
- `animateSize = false` and Reduced Motion settle size and text immediately
- fixed-to-auto and auto-to-fixed changes remain instant

```kotlin
val label = if (isLong) "Open analytics dashboard" else "Open"

ThemedButton(
    child = label,
    name = ThemeName.Basic,
    autoWidth = true,
    textTransition = true,
)

ThemedButton(
    child = label,
    name = ThemeName.Basic,
    autoWidth = true,
    animateSize = false,
)
```

Arbitrary Compose content remains single-composed in the rendered row. Use
`width` for a fixed width or `stretch = true` to fill the available width.

### Progress Buttons

Set `progress = true` to route presses through the typed progress contract.
Call `next?.invoke()` when the async work finishes.

```kotlin
val scope = rememberCoroutineScope()

AwesomeButton(
    child = "Submit",
    progress = true,
    onPress = { next ->
        scope.launch {
            submitForm()
            next?.invoke()
        }
    },
)
```

The progress callback shape is:

```kotlin
fun interface AwesomeButtonNext {
    fun complete(callback: (() -> Unit)?)

    operator fun invoke(callback: (() -> Unit)? = null) {
        complete(callback)
    }
}

typealias AwesomeButtonPressCallback = (AwesomeButtonNext?) -> Unit
```

Set `showProgressBar = false` to hide only the traveling progress fill. The
button still shows the activity indicator and keeps the progress lifecycle.

### Themed Buttons

`ThemedButton` resolves built-in theme, variant, and size values before
delegating to `AwesomeButton`.

```kotlin
import dev.caferati.awesomebutton.ButtonSize
import dev.caferati.awesomebutton.ButtonVariant
import dev.caferati.awesomebutton.ThemeName
import dev.caferati.awesomebutton.ThemedButton

ThemedButton(
    child = "Rick Primary",
    name = ThemeName.Rick,
    type = ButtonVariant.Primary,
    size = ButtonSize.Medium,
)
```

Theme registry lookup is available by index or name:

```kotlin
val firstTheme = getTheme()
val rickTheme = getTheme(ThemeName.Rick)
val secondTheme = getTheme(index = 1)
```

You can pass a theme definition directly:

```kotlin
ThemedButton(
    child = "Custom",
    config = getTheme(ThemeName.Bojack),
    type = ButtonVariant.Secondary,
)
```

### Before / After / Extra Content

`before` and `after` render inline inside the content row. `extra` fills the
face layer behind active/progress/content layers.

```kotlin
AwesomeButton(
    child = "Upload",
    before = {
        Icon(
            imageVector = Icons.Default.Upload,
            contentDescription = null,
        )
    },
    after = {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
        )
    },
    extra = {
        Box(
            Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.08f))
        )
    },
    onPress = { /* Upload. */ },
)
```

### Transparent Buttons

`transparent` is supported on `ThemedButton`. It removes the visible shell
layers while preserving the content, hit target, and active/progress feedback.

```kotlin
ThemedButton(
    child = "Transparent",
    name = ThemeName.Bruce,
    type = ButtonVariant.Anchor,
    transparent = true,
)
```

## Built-in Theme Contract

### Theme Names

- `ThemeName.Basic`
- `ThemeName.Bojack`
- `ThemeName.Cartman`
- `ThemeName.Mysterion`
- `ThemeName.C137`
- `ThemeName.Rick`
- `ThemeName.Summer`
- `ThemeName.Bruce`

### Variants

- `ButtonVariant.Primary`
- `ButtonVariant.Secondary`
- `ButtonVariant.Anchor`
- `ButtonVariant.Danger`
- `ButtonVariant.Disabled`
- `ButtonVariant.Flat`
- `ButtonVariant.X`
- `ButtonVariant.Twitter`
- `ButtonVariant.Messenger`
- `ButtonVariant.Facebook`
- `ButtonVariant.Github`
- `ButtonVariant.Linkedin`
- `ButtonVariant.Whatsapp`
- `ButtonVariant.Reddit`
- `ButtonVariant.Pinterest`
- `ButtonVariant.Youtube`

`ButtonVariant.X` is preferred for new social examples. `ButtonVariant.Twitter`
remains as a deprecated compatibility alias and resolves to the same built-in
visual treatment.

### Sizes

- `ButtonSize.Icon`
- `ButtonSize.Small`
- `ButtonSize.Medium`
- `ButtonSize.Large`

Social variants are visual variants only; the package does not invoke Android
sharing APIs.

## API Reference

The tables below cover the primary consumer-facing parameters. The generated
Dokka documentation and checked-in
[ABI dump](awesome-button-compose/api/awesome-button-compose.api) define the
complete public Compose surface.

### AwesomeButton

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `modifier` | `Modifier` | `Modifier` | Modifier applied to the outer Compose surface. |
| `child` | `String?` | `null` | Plain text label. A missing `child` and missing custom content render placeholder mode. |
| `content` | `(@Composable RowScope.() -> Unit)?` | `null` | Custom row content instead of a string label. |
| `onPress` | `AwesomeButtonPressCallback?` | `null` | Press handler. Receives `AwesomeButtonNext` only when `progress = true`. |
| `onLongPress` | `(() -> Unit)?` | `null` | Optional long-press handler. |
| `disabled` | `Boolean` | `false` | Blocks pointer, keyboard, and accessibility activation. |
| `width` | `Dp?` | `null` | Fixed width. When omitted, the button measures content unless `stretch = true`. |
| `height` | `Dp` | `52.dp` | Interactive face height before the raise/depth layer. Total shell height equals face height plus the resolved raise amount. |
| `paddingHorizontal` | `Dp?` | `16.dp` resolved | Horizontal content padding. |
| `paddingTop` | `Dp?` | `0.dp` resolved | Additional top content padding. |
| `paddingBottom` | `Dp?` | `0.dp` resolved | Additional bottom content padding. |
| `before` | `(@Composable RowScope.() -> Unit)?` | `null` | Content rendered before the primary label inside the face. |
| `after` | `(@Composable RowScope.() -> Unit)?` | `null` | Content rendered after the primary label inside the face. |
| `extra` | `(@Composable BoxScope.() -> Unit)?` | `null` | Content rendered behind the active/content layers. |
| `stretch` | `Boolean` | `false` | Fill available width. |
| `style` | `AwesomeButtonStyle?` | `null` | Visual style overrides. |
| `activeOpacity` | `Float` | `1f` | Opacity applied while a non-progress button is pressed. |
| `debouncedPressTimeMillis` | `Long` | `0` | Debounces accepted `onPress` dispatches in milliseconds. |
| `pressInAnimationDurationMillis` | `Int?` | `null` | Optional press-down override. When absent, `style.animationDurationMillis` and then the 140 ms package fallback apply. |
| `progress` | `Boolean` | `false` | Enables progress lifecycle and spinner transition. |
| `showProgressBar` | `Boolean` | `true` | Shows or hides the progress fill. The spinner, busy state, callbacks, and completion handle remain active when false. |
| `progressLoadingTimeMillis` | `Int` | `3000` | Fill travel duration before completion. |
| `animateSize` | `Boolean` | `true` | Animates fixed geometry and auto-width string-label changes. |
| `textTransition` | `Boolean` | `false` | Animates text changes through the native-style character transition. |
| `textTransitionSlotStaggerMillis` | `Int` | `7` | Milliseconds between character slots during text transitions. |
| `animatedPlaceholder` | `Boolean` | `true` | Enables placeholder shimmer when in placeholder mode. |
| `accessibilityLabel` | `String?` | `null` | Spoken identity override. Plain text and meaningful custom-content semantics are inferred when absent. |
| `accessibilityHint` | `String?` | `null` | Label for the ordinary Compose semantic click action. It is not added to the spoken identity or state. |
| `accessibilityLongPressLabel` | `String?` | `null` | Spoken semantic long-action name. The package-localized default is “Long press.” |
| `onPressIn` | `(() -> Unit)?` | `null` | Fires when an eligible pointer gesture arms. |
| `onPressOut` | `(() -> Unit)?` | `null` | Fires after release or cancellation owns the terminal outcome. |
| `onPressedIn` | `(() -> Unit)?` | `null` | Fires after pressed state is committed. |
| `onPressedOut` | `(() -> Unit)?` | `null` | Fires after the captured release transition settles. |
| `onProgressStart` | `(() -> Unit)?` | `null` | Fires when accepted progress begins. |
| `onProgressEnd` | `(() -> Unit)?` | `null` | Fires after accepted progress completion settles. |

For example, a `height` of `52.dp` with a resolved raise amount of `6.dp`
produces a total shell height of `58.dp`:

```text
total shell height = face height + resolved raise amount
58.dp = 52.dp + 6.dp
```

### ThemedButton

`ThemedButton` accepts the `AwesomeButton` parameters plus these
theme-resolution parameters.

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `config` | `ThemeDefinition?` | `null` | Direct theme definition. Wins over `name` and `index`. |
| `index` | `Int?` | `null` | Built-in theme index. Falls back safely to `0`. |
| `name` | `ThemeName?` | `null` | Built-in theme name. |
| `type` | `ButtonVariant` | `Primary` | Variant resolved from the selected theme. |
| `size` | `ButtonSize` | `Medium` | Size preset resolved from the selected theme. |
| `flat` | `Boolean` | `false` | Uses the theme flat variant when available, including while disabled. |
| `transparent` | `Boolean` | `false` | Clears themed face/depth/shadow/placeholder/border colors. |
| `autoWidth` | `Boolean` | `false` | Lets themed buttons measure string labels instead of using theme width. |
| `textTransitionSlotStaggerMillis` | `Int` | `7` | Milliseconds between character slots during text transitions. |
| `style` | `AwesomeButtonStyle?` | `null` | Explicit overrides applied after theme, size, and variant resolution. |

## Interaction and Lifecycle

Callback/configuration replacements committed during a hold are live, while
release and progress-completion callbacks are captured when their transition
starts. Removing a long handler disarms the active gesture; adding one takes
effect on the next gesture. Pointer cancellation, disablement, placeholder
transition, and removal terminate once without activation. Atomic keyboard and
semantic activation shares debounce and progress ownership without fabricating
pointer-only lifecycle callbacks.

Requested flat styling is preserved while disabled; otherwise disabled styling
overrides the requested variant. Explicit dimensions and style values win over
variant, then size, then package fallback. Width resolves stretch, fixed width,
auto width, variant, size, fallback; height resolves explicit, variant, size,
fallback. The themed wrapper owns its 200 ms variant interpolation and the inner
button does not apply a second style animation. Other direct resolved-style
changes use `style.animationDurationMillis`; theme-source and transparency
changes snap.

## Accessibility, Reduced Motion, and Numeric Validation

`AwesomeButton` and `ThemedButton` expose one Compose button semantics node.
Ordinary semantic activation and semantic long activation are atomic actions:
they use the same debounce/progress ownership as touch, but do not fabricate
pointer-only `onPressIn`, `onPressedIn`, `onPressOut`, or `onPressedOut`
callbacks. Disabled, busy, and placeholder buttons expose no activation;
unlabeled placeholders are hidden, while an explicitly labeled placeholder is
discoverable as unavailable.

The package requests a minimum 48 dp layout and interaction footprint, keeps
typography in `sp`, allows labels to wrap and grow the face when font scale is
above 1.0, and follows the active layout direction while keeping physical
corner names physical. Android animator scale `0` enables the package Reduced
Motion path: press, release, size, text, style, placeholder, and progress
effects snap while callback ordering, debounce, long-press thresholds, and
progress-handle ownership remain unchanged. Package-owned spoken state/action
strings are Android resources and may be localized by adding resource locales.

Numeric inputs are normalized before Compose layout or animation consumes
them. Non-finite optional values act as absent, non-finite required values use
their declared defaults, negative dimensions and durations clamp to zero, and
opacity clamps to `[0, 1]`. Fixed width zero remains explicit.

## Android and Compose

The package targets Android Jetpack Compose and follows Compose ownership for
recomposition, remembered state, structured coroutine cancellation, pointer
and key input, and semantics. Android View/XML widgets and Compose
Multiplatform targets are not part of the `1.1.0` package surface.

## Development

Run the package release preflight from the repository root:

```bash
AWESOME_BUTTON_SKIP_MANAGED_DEVICE=1 scripts/release-preflight.sh
```

The gate validates formatting, JVM tests, ABI compatibility, lint, Dokka,
Kover reports, instrumentation assembly, release assembly, and publication
shape. Android instrumentation runtime evidence remains pending when no device
or managed emulator is available. See
[`CONTRIBUTING.md`](CONTRIBUTING.md), [`ABI_REVIEW.md`](ABI_REVIEW.md), and
[`PERFORMANCE.md`](PERFORMANCE.md) for review and evidence policies.

Focused iteration commands include:

```bash
./gradlew :awesome-button-compose:testDebugUnitTest
./gradlew --no-configuration-cache :awesome-button-compose:connectedDebugAndroidTest
./gradlew :awesome-button-compose:assembleRelease
./gradlew :demo:assembleDebug
```

### Publishing

Publication is configured for Maven Central through Sonatype Central Portal.
The released artifact is:

```text
dev.caferati:awesome-button-compose:1.1.0
```

Required release credentials are supplied through user Gradle properties or CI
environment variables:

```text
ORG_GRADLE_PROJECT_mavenCentralUsername
ORG_GRADLE_PROJECT_mavenCentralPassword
ORG_GRADLE_PROJECT_signingInMemoryKey
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
```

The included GitHub Actions workflow maps those values from repository secrets
named `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`,
`SIGNING_IN_MEMORY_KEY`, and `SIGNING_IN_MEMORY_KEY_PASSWORD`.

Local publication inspection:

```bash
./gradlew :awesome-button-compose:publishToMavenLocal
```

Release publication:

```bash
./gradlew :awesome-button-compose:publishAndReleaseToMavenCentral
```

See [RELEASING.md](RELEASING.md) for the full checklist.

## Demo Application

The `demo` module contains the Android parity demo:

- `Themed` tab with theme stack navigation
- `Progress` tab with progress lifecycle examples
- `Social` tab with social visual variants
- `Size Changes` tab with width and text-transition examples

Build it with:

```bash
./gradlew :demo:assembleDebug
```

## Awesome Button Family

Awesome Button is maintained as four native packages that share product
semantics while following each platform's implementation model:

- [React Native Awesome Button](https://github.com/rcaferati/react-native-awesome-button)
- [Flutter Awesome Button](https://github.com/rcaferati/flutter_awesome_button)
- [Kotlin Awesome Button](https://github.com/rcaferati/kotlin-awesome-button)
- [Swift Awesome Button](https://github.com/rcaferati/swift-awesome-button)

## Author

Created and maintained by [Rafael Caferati](https://caferati.dev).

- [GitHub](https://github.com/rcaferati)
- [LinkedIn](https://linkedin.com/in/rcaferati)
- [Instagram](https://instagram.com/rcaferati)

## License

MIT. See [LICENSE](LICENSE).
