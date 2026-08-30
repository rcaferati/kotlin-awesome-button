package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AwesomeButtonPresentationTest {
    @Test
    fun resolvesDefaultsAndPlaceholderEligibility() {
        val presentation = resolveAwesomeButtonPresentation(input())

        assertTrue(presentation.isPlaceholder)
        assertTrue(presentation.effectiveDisabled)
        assertEquals(ButtonWidthMode.Auto, presentation.widthMode)
        assertNull(presentation.normalizedWidth)
        assertEquals(52.dp, presentation.normalizedHeight)
        assertEquals(16.dp, presentation.paddingHorizontal)
        assertEquals(0.dp, presentation.paddingTop)
        assertEquals(0.dp, presentation.paddingBottom)
        assertEquals(52, presentation.targetHeightPx)
    }

    @Test
    fun normalizesGeometryTimingOpacityAndWidthPrecedence() {
        val presentation =
            resolveAwesomeButtonPresentation(
                input(
                    childPresent = true,
                    width = Dp(Float.NaN),
                    height = Dp(Float.NaN),
                    paddingHorizontal = (-2).dp,
                    activeOpacity = Float.NaN,
                    debouncedPressTimeMillis = -20,
                    progressLoadingTimeMillis = -30,
                    pressInAnimationDurationMillis = -40,
                    stretch = true,
                ),
            )

        assertFalse(presentation.isPlaceholder)
        assertFalse(presentation.effectiveDisabled)
        assertEquals(ButtonWidthMode.Stretch, presentation.widthMode)
        assertNull(presentation.normalizedWidth)
        assertEquals(52.dp, presentation.normalizedHeight)
        assertEquals(0.dp, presentation.paddingHorizontal)
        assertEquals(1f, presentation.activeOpacity)
        assertEquals(0, presentation.debounceMillis)
        assertEquals(0, presentation.progressLoadingMillis)
        assertEquals(0, presentation.pressInDurationMillis)
    }

    @Test
    fun explicitStyleOverridesThemeAndEnvironmentFactsRemainExplicit() {
        val presentation =
            resolveAwesomeButtonPresentation(
                input(
                    childPresent = true,
                    customContentPresent = true,
                    themeStyle = AwesomeButtonStyle(backgroundColor = Color.Red, borderWidth = 1.dp),
                    style = AwesomeButtonStyle(backgroundColor = Color.Blue, borderWidth = 3.dp),
                    width = 120.dp,
                    fontScale = 1.5f,
                    reduceMotion = true,
                    density = Density(2f, 1.5f),
                ),
            )

        assertEquals(Color.Blue, presentation.targetStyle.backgroundColor)
        assertEquals(ButtonWidthMode.Fixed, presentation.widthMode)
        assertEquals(120.dp, presentation.normalizedWidth)
        assertEquals(6, presentation.targetBorderWidthPx)
        assertTrue(presentation.hasCustomContent)
        assertTrue(presentation.accessibilityTextGrowth)
        assertTrue(presentation.reduceMotion)
    }

    private fun input(
        themeStyle: AwesomeButtonStyle = AwesomeButtonThemeData.fallbackStyle,
        style: AwesomeButtonStyle? = null,
        childPresent: Boolean = false,
        customContentPresent: Boolean = false,
        width: Dp? = null,
        height: Dp = 52.dp,
        paddingHorizontal: Dp? = null,
        activeOpacity: Float = 1f,
        debouncedPressTimeMillis: Long = 0,
        progressLoadingTimeMillis: Int = 3000,
        pressInAnimationDurationMillis: Int? = null,
        stretch: Boolean = false,
        fontScale: Float = 1f,
        reduceMotion: Boolean = false,
        density: Density = Density(1f, fontScale),
    ): AwesomeButtonPresentationInput =
        AwesomeButtonPresentationInput(
            themeStyle = themeStyle,
            style = style,
            styleIsResolvedFrame = false,
            childPresent = childPresent,
            customContentPresent = customContentPresent,
            beforePresent = false,
            afterPresent = false,
            disabled = false,
            width = width,
            height = height,
            paddingHorizontal = paddingHorizontal,
            paddingTop = null,
            paddingBottom = null,
            stretch = stretch,
            activeOpacity = activeOpacity,
            debouncedPressTimeMillis = debouncedPressTimeMillis,
            progressLoadingTimeMillis = progressLoadingTimeMillis,
            pressInAnimationDurationMillis = pressInAnimationDurationMillis,
            reduceMotion = reduceMotion,
            busyStateDescription = "Busy",
            defaultLongPressLabel = "Long press",
            density = density,
            fontScale = fontScale,
        )
}
