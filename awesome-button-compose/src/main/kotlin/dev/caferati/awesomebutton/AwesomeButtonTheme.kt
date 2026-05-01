package dev.caferati.awesomebutton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

object AwesomeButtonTheme {
    private val LocalTheme = compositionLocalOf { AwesomeButtonThemeData.fallback }

    val current: AwesomeButtonThemeData
        @Composable
        @ReadOnlyComposable
        get() = LocalTheme.current

    @Composable
    fun Provider(
        themeData: AwesomeButtonThemeData,
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(LocalTheme provides themeData, content = content)
    }
}
