package dev.caferati.awesomebutton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonVariantInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun themedVariantChangeAnimatesFaceColor() {
        var variant by mutableStateOf(ButtonVariant.Primary)

        composeRule.setContent {
            ThemedButton(
                child = "Variant",
                name = ThemeName.Basic,
                type = variant,
            )
        }

        composeRule.waitForIdle()
        val startColor = sampleFaceColor()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                variant = ButtonVariant.Secondary
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(100)
            val midpointColor = sampleFaceColor()

            composeRule.mainClock.advanceTimeBy(200)
            val endColor = sampleFaceColor()
            val easedMidpoint = AwesomeButtonAnimationCurve.EaseOutCubic.toEasing().transform(0.5f)
            val expectedMidpoint = lerpColor(startColor, endColor, easedMidpoint)

            assertColorDistanceGreaterThan(startColor, midpointColor, 0.03f)
            assertColorDistanceGreaterThan(endColor, midpointColor, 0.03f)
            assertColorClose(expectedMidpoint, midpointColor, 0.18f)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }
}
