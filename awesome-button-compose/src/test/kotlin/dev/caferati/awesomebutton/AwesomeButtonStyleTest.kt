package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import androidx.compose.ui.graphics.lerp as lerpColor

class AwesomeButtonStyleTest {
    @Test
    fun mergeUsesOverrideValuesOnlyWhenProvided() {
        val base =
            AwesomeButtonStyle(
                backgroundColor = Color.Red,
                depthColor = Color.Blue,
                raiseAmount = 6.dp,
            )
        val override =
            AwesomeButtonStyle(
                backgroundColor = Color.Green,
                borderRadius = 12.dp,
            )

        val merged = base.merge(override)

        assertEquals(Color.Green, merged.backgroundColor)
        assertEquals(Color.Blue, merged.depthColor)
        assertEquals(6.dp, merged.raiseAmount)
        assertEquals(12.dp, merged.borderRadius)
    }

    @Test
    fun interpolationBlendsVisualColors() {
        val from =
            AwesomeButtonStyle(
                backgroundColor = Color(1f, 0f, 0f),
                depthColor = Color.Black,
                foregroundColor = Color.White,
                borderColor = Color(1f, 0f, 0f),
            )
        val to =
            AwesomeButtonStyle(
                backgroundColor = Color(0f, 0f, 1f),
                depthColor = Color.White,
                foregroundColor = Color.Black,
                borderColor = Color(0f, 0f, 1f),
            )

        val interpolated = interpolateAwesomeButtonStyle(from, to, 0.5f)

        assertColorClose(lerpColor(from.backgroundColor!!, to.backgroundColor!!, 0.5f), interpolated.backgroundColor)
        assertColorClose(lerpColor(from.depthColor!!, to.depthColor!!, 0.5f), interpolated.depthColor)
        assertColorClose(lerpColor(from.foregroundColor!!, to.foregroundColor!!, 0.5f), interpolated.foregroundColor)
        assertColorClose(lerpColor(from.borderColor!!, to.borderColor!!, 0.5f), interpolated.borderColor)
    }

    @Test
    fun interpolationPreservesUnsetDisabledOverrides() {
        val interpolated =
            interpolateAwesomeButtonStyle(
                AwesomeButtonStyle(backgroundColor = Color.Red),
                AwesomeButtonStyle(backgroundColor = Color.Blue),
                0.5f,
            )

        assertNull(interpolated.disabledBackgroundColor)
        assertNull(interpolated.disabledDepthColor)
        assertNull(interpolated.disabledShadowColor)
        assertNull(interpolated.disabledForegroundColor)
        assertNull(interpolated.disabledBorderColor)
    }

    @Test
    fun interpolationBlendsDimensionsAndTextUnits() {
        val from =
            AwesomeButtonStyle(
                textSize = 10.sp,
                textLineHeight = 12.sp,
                borderRadius = 4.dp,
                borderWidth = 1.dp,
                raiseAmount = 2.dp,
                contentGap = 6.dp,
            )
        val to =
            AwesomeButtonStyle(
                textSize = 20.sp,
                textLineHeight = 24.sp,
                borderRadius = 12.dp,
                borderWidth = 5.dp,
                raiseAmount = 10.dp,
                contentGap = 14.dp,
            )

        val interpolated = interpolateAwesomeButtonStyle(from, to, 0.5f)

        assertEquals(15.sp, interpolated.textSize)
        assertEquals(18.sp, interpolated.textLineHeight)
        assertEquals(8.dp, interpolated.borderRadius)
        assertEquals(3.dp, interpolated.borderWidth)
        assertEquals(6.dp, interpolated.raiseAmount)
        assertEquals(10.dp, interpolated.contentGap)
        assertNotNull(interpolated.cornerRadii)
        assertEquals(8.dp, interpolated.cornerRadii?.topStart)
    }

    @Test
    fun invalidNumericStyleInputNormalizesWithoutThrowing() {
        val normalized =
            resolvedVisualStyle(
                AwesomeButtonStyle(
                    textSize = TextUnit(Float.NaN, androidx.compose.ui.unit.TextUnitType.Sp),
                    textLineHeight = (-4).sp,
                    borderRadius = Dp(Float.NaN),
                    cornerRadii =
                        AwesomeButtonCornerRadii(
                            topStart = (-1).dp,
                            topEnd = Dp(Float.POSITIVE_INFINITY),
                            bottomEnd = 3.dp,
                            bottomStart = 4.dp,
                        ),
                    borderWidth = (-2).dp,
                    raiseAmount = Dp(Float.POSITIVE_INFINITY),
                    contentGap = (-6).dp,
                    animationDurationMillis = -1,
                ),
            )

        assertEquals(AwesomeButtonThemeData.fallbackStyle.textSize, normalized.textSize)
        assertEquals(0.sp, normalized.textLineHeight)
        assertEquals(AwesomeButtonThemeData.fallbackStyle.borderRadius, normalized.borderRadius)
        assertEquals(0.dp, normalized.cornerRadii?.topStart)
        assertEquals(0.dp, normalized.cornerRadii?.topEnd)
        assertEquals(0.dp, normalized.borderWidth)
        assertEquals(AwesomeButtonThemeData.fallbackStyle.raiseAmount, normalized.raiseAmount)
        assertEquals(0.dp, normalized.contentGap)
        assertEquals(0, normalized.animationDurationMillis)
    }

    @Test
    fun publicBoundaryNormalizationUsesAbsentFallbacksAndClamps() {
        assertNull(normalizeOptionalDp(Dp(Float.NaN)))
        assertNull(normalizeOptionalDp(Dp(Float.POSITIVE_INFINITY)))
        assertEquals(0.dp, normalizeOptionalDp((-10).dp))
        assertEquals(52.dp, normalizeRequiredDp(Dp(Float.NaN), 52.dp))
        assertEquals(0f, normalizeOpacity(-1f))
        assertEquals(1f, normalizeOpacity(Float.NaN))
        assertEquals(1f, normalizeOpacity(4f))
        assertEquals(0, normalizeOptionalMillis(-1))
    }

    @Test
    fun pressInDurationOverridePrecedesStyleAndNegativeValuesClamp() {
        assertEquals(25, resolvePressInDurationMillis(25, 140))
        assertEquals(140, resolvePressInDurationMillis(null, 140))
        assertEquals(0, resolvePressInDurationMillis(-1, 140))
        assertEquals(0, resolvePressInDurationMillis(null, -1))
        assertEquals(140, resolvePressInDurationMillis(null, null))
        assertEquals(200, THEMED_STYLE_TRANSITION_DURATION_MILLIS)
    }

    private fun assertColorClose(
        expected: Color,
        actual: Color?,
        tolerance: Float = 0.01f,
    ) {
        assertNotNull(actual)
        val color = actual!!
        assertTrue("red expected ${expected.red}, was ${color.red}", abs(expected.red - color.red) <= tolerance)
        assertTrue(
            "green expected ${expected.green}, was ${color.green}",
            abs(expected.green - color.green) <= tolerance,
        )
        assertTrue("blue expected ${expected.blue}, was ${color.blue}", abs(expected.blue - color.blue) <= tolerance)
        assertTrue(
            "alpha expected ${expected.alpha}, was ${color.alpha}",
            abs(expected.alpha - color.alpha) <= tolerance,
        )
    }
}
