package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTransitionTest {
    @Test
    fun constantsMatchSwiftProgressTimings() {
        assertEquals(300, ProgressSwapDurationMillis)
        assertEquals(120, ProgressFillCompletionDurationMillis)
        assertEquals(120, ProgressOverlayFadeDelayMillis)
        assertEquals(160, ProgressOverlayFadeDurationMillis)
    }

    @Test
    fun swapCurveStartsEndsAndOvershoots() {
        assertEquals(0f, progressSwapEasingValue(0f), 0.0001f)
        assertEquals(1f, progressSwapEasingValue(1f), 0.0001f)

        val maxValue = (0..100).maxOf { index ->
            progressSwapEasingValue(index / 100f)
        }
        assertTrue("expected swap curve to overshoot above 1, was $maxValue", maxValue > 1f)
    }

    @Test
    fun completionCurveMatchesEaseOutCubic() {
        assertEquals(0f, progressCompletionEasingValue(0f), 0.0001f)
        assertEquals(0.875f, progressCompletionEasingValue(0.5f), 0.0001f)
        assertEquals(1f, progressCompletionEasingValue(1f), 0.0001f)
    }

    @Test
    fun overlayOpacityHoldsThenFades() {
        assertEquals(1f, progressOverlayOpacityValue(0), 0.0001f)
        assertEquals(1f, progressOverlayOpacityValue(ProgressOverlayFadeDelayMillis), 0.0001f)

        val midpoint = progressOverlayOpacityValue(ProgressOverlayFadeDelayMillis + 80)
        assertTrue("expected midpoint opacity below 1, was $midpoint", midpoint < 1f)
        assertTrue("expected midpoint opacity above 0, was $midpoint", midpoint > 0f)

        assertEquals(
            0f,
            progressOverlayOpacityValue(ProgressOverlayFadeDelayMillis + ProgressOverlayFadeDurationMillis),
            0.0001f,
        )
    }

    @Test
    fun progressHelpersClampInput() {
        assertEquals(0f, progressSwapEasingValue(-1f), 0.0001f)
        assertEquals(1f, progressSwapEasingValue(2f), 0.0001f)
        assertEquals(0f, progressCompletionEasingValue(-1f), 0.0001f)
        assertEquals(1f, progressCompletionEasingValue(2f), 0.0001f)
    }
}
