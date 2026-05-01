package dev.caferati.awesomebutton.demo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.caferati.awesomebutton.ThemeName
import kotlinx.coroutines.delay

private data class ThemeCharacterConfig(
    val drawableRes: Int,
    val width: Float,
    val height: Float,
    val x: Float,
    val y: Float,
)

@Composable
internal fun BoxScope.ThemeCharacterOverlay(themeName: ThemeName) {
    val config = themeName.characterConfig() ?: return
    val offsetX = remember { Animatable(InitialOffsetX) }

    LaunchedEffect(themeName) {
        offsetX.snapTo(InitialOffsetX)
        delay(EnterDelayMillis)
        offsetX.animateTo(
            targetValue = config.x,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 447.4f,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.BottomEnd),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Image(
            painter = painterResource(config.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset(x = offsetX.value.dp, y = (-config.y).dp)
                .size(width = config.width.dp, height = config.height.dp),
        )
    }
}

private const val EnterDelayMillis = 185L
private const val InitialOffsetX = 200f

private fun ThemeName.characterConfig(): ThemeCharacterConfig? =
    when (this) {
        ThemeName.Basic -> null
        ThemeName.Bojack -> ThemeCharacterConfig(R.drawable.bojack, width = 246f, height = 348f, x = 42f, y = 0f)
        ThemeName.Rick -> ThemeCharacterConfig(R.drawable.rick, width = 273.5f, height = 384f, x = 90f, y = 0f)
        ThemeName.C137 -> ThemeCharacterConfig(R.drawable.c137, width = 208f, height = 319.5f, x = 40f, y = 0f)
        ThemeName.Cartman -> ThemeCharacterConfig(R.drawable.cartman, width = 320f, height = 295f, x = 70f, y = 0f)
        ThemeName.Bruce -> ThemeCharacterConfig(R.drawable.batman, width = 365.7f, height = 439.4f, x = 90f, y = -25f)
        ThemeName.Mysterion -> ThemeCharacterConfig(R.drawable.mysterion, width = 320f, height = 295f, x = 70f, y = 0f)
        ThemeName.Summer -> ThemeCharacterConfig(R.drawable.summer, width = 197.5f, height = 363.5f, x = 30f, y = -10f)
    }
