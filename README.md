# Kotlin Awesome Button

`awesome-button-compose` is the Android Jetpack Compose package for the
Awesome Button component family.

It provides material-independent 3D-style buttons with layered depth,
progress flows, animated press and release behavior, placeholder loading
states, auto/fixed/stretch sizing, text transitions, and built-in typed themes.

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
        alt="Blue demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-blue-new.gif"
      />
    </td>
    <td width="33%">
      <img
        alt="Cartman demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-cartman.gif"
      />
    </td>
    <td width="33%">
      <img
        alt="Rick demo"
        src="https://raw.githubusercontent.com/rcaferati/kotlin-awesome-button/main/screenshots/demo-button-rick.gif"
      />
    </td>
  </tr>
</table>

## Install

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
    implementation("dev.caferati:awesome-button-compose:1.0.0")
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

## Size Changes

Auto-width buttons animate between measured label sizes. Enable
`textTransition` to use the native-style character transition during label
changes.

```kotlin
var expanded by remember { mutableStateOf(false) }
val label = if (expanded) "View analytics dashboard" else "Launch"

AwesomeButton(
    child = label,
    textTransition = true,
    animateSize = true,
    onPress = { expanded = !expanded },
)
```

Use `width` for fixed width or `stretch = true` to fill the available width.

## Progress Buttons

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

## Themed Buttons

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

## Before / After / Extra Content

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

## Transparent Buttons

Use `transparent = true` on `ThemedButton` to clear the face, depth, shadow,
placeholder, and border theme colors. This is useful for flat navigation
controls and disabled flat states.

```kotlin
ThemedButton(
    child = "Prev",
    name = ThemeName.Basic,
    type = ButtonVariant.Flat,
    flat = true,
    transparent = true,
    onPress = { /* Navigate. */ },
)
```

## Built-in Theme Contract

Built-in themes:

- `ThemeName.Basic`
- `ThemeName.Bojack`
- `ThemeName.Cartman`
- `ThemeName.Mysterion`
- `ThemeName.C137`
- `ThemeName.Rick`
- `ThemeName.Summer`
- `ThemeName.Bruce`

Built-in variants:

- `ButtonVariant.Primary`
- `ButtonVariant.Secondary`
- `ButtonVariant.Anchor`
- `ButtonVariant.Danger`
- `ButtonVariant.Disabled`
- `ButtonVariant.Flat`
- `ButtonVariant.Facebook`
- `ButtonVariant.Github`
- `ButtonVariant.Linkedin`
- `ButtonVariant.Messenger`
- `ButtonVariant.Pinterest`
- `ButtonVariant.Reddit`
- `ButtonVariant.Whatsapp`
- `ButtonVariant.X`
- `ButtonVariant.Youtube`
- `ButtonVariant.Twitter`

`ButtonVariant.X` is preferred for new social examples. `ButtonVariant.Twitter`
remains available for compatibility with the original Flutter-aligned API.

Built-in sizes:

- `ButtonSize.Icon`
- `ButtonSize.Small`
- `ButtonSize.Medium`
- `ButtonSize.Large`

Social variants are visual variants only. React/Vue social sharing wrappers are
not part of the Android v1 package.

## Selected Parameters

### `AwesomeButton`

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `child` | `String?` | `null` | Plain text label. A missing `child` and missing custom content render placeholder mode. |
| `content` | `@Composable RowScope.() -> Unit` | `null` | Custom row content instead of a string label. |
| `onPress` | `AwesomeButtonPressCallback?` | `null` | Press handler. Receives `AwesomeButtonNext` only when `progress = true`. |
| `onLongPress` | `(() -> Unit)?` | `null` | Optional long-press handler. |
| `disabled` | `Boolean` | `false` | Blocks pointer, keyboard, and accessibility activation. |
| `width` | `Dp?` | `null` | Fixed width. When omitted, the button measures content unless `stretch = true`. |
| `height` | `Dp` | `52.dp` | Total shell height. |
| `stretch` | `Boolean` | `false` | Fill available width. |
| `style` | `AwesomeButtonStyle?` | `null` | Visual style overrides. |
| `progress` | `Boolean` | `false` | Enables progress lifecycle and spinner transition. |
| `showProgressBar` | `Boolean` | `true` | Hides only the progress fill when false. |
| `progressLoadingTimeMillis` | `Int` | `3000` | Fill travel duration before completion. |
| `animateSize` | `Boolean` | `true` | Animates width/height changes. |
| `textTransition` | `Boolean` | `false` | Animates text changes through the native-style character transition. |
| `textTransitionSlotStaggerMillis` | `Int` | `7` | Milliseconds between character slots during text transitions. |
| `animatedPlaceholder` | `Boolean` | `true` | Enables placeholder shimmer when in placeholder mode. |

### `ThemedButton`

| Parameter | Type | Default | Description |
| --- | --- | --- | --- |
| `config` | `ThemeDefinition?` | `null` | Direct theme definition. Wins over `name` and `index`. |
| `index` | `Int?` | `null` | Built-in theme index. Falls back safely to `0`. |
| `name` | `ThemeName?` | `null` | Built-in theme name. |
| `type` | `ButtonVariant` | `Primary` | Variant resolved from the selected theme. |
| `size` | `ButtonSize` | `Medium` | Size preset resolved from the selected theme. |
| `flat` | `Boolean` | `false` | Uses the theme flat variant when available. |
| `transparent` | `Boolean` | `false` | Clears themed face/depth/shadow/placeholder/border colors. |
| `autoWidth` | `Boolean` | `false` | Lets themed buttons measure string labels instead of using theme width. |
| `textTransitionSlotStaggerMillis` | `Int` | `7` | Milliseconds between character slots during text transitions. |
| `style` | `AwesomeButtonStyle?` | `null` | Explicit overrides applied after theme, size, and variant resolution. |

## Development

```bash
./gradlew :awesome-button-compose:testDebugUnitTest
./gradlew --no-configuration-cache :awesome-button-compose:connectedDebugAndroidTest
./gradlew :awesome-button-compose:assembleRelease
./gradlew :demo:assembleDebug
```

## Publishing

Publication is configured for Maven Central through Sonatype Central Portal.
The released artifact is:

```text
dev.caferati:awesome-button-compose:1.0.0
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

## Demo App

The `demo` module contains the Android parity demo:

- `Themed` tab with theme stack navigation
- `Progress` tab with progress lifecycle examples
- `Social` tab with social visual variants
- `Size Changes` tab with width and text-transition examples

Build it with:

```bash
./gradlew :demo:assembleDebug
```

## Author

Rafael Caferati

- GitHub: [@rcaferati](https://github.com/rcaferati)

## License

MIT. See [LICENSE](LICENSE).
