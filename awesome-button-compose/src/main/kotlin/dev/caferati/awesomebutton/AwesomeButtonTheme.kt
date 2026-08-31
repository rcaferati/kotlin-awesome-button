package dev.caferati.awesomebutton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/** Composition-local owner for the base [AwesomeButtonThemeData]. */
public object AwesomeButtonTheme {
    private val LocalTheme = compositionLocalOf { AwesomeButtonThemeData.fallback }

    /** Current theme data, or [AwesomeButtonThemeData.fallback] outside a provider. */
    public val current: AwesomeButtonThemeData
        @Composable
        @ReadOnlyComposable
        get() = LocalTheme.current

    @Composable
    /** Provides [themeData] to [content] through normal Compose-local ownership. */
    public fun Provider(
        themeData: AwesomeButtonThemeData,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(LocalTheme provides themeData, content = content)
    }
}
