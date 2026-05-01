package dev.caferati.awesomebutton.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun DemoApp() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF038CD9),
            secondary = Color(0xFF4F6FC4),
            background = Color.White,
            surface = Color.White,
        ),
    ) {
        DemoShell()
    }
}
