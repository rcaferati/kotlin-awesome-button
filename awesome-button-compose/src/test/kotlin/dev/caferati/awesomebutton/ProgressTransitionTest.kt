package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTransitionTest {
    @Test
    fun constantsMatchSwiftProgressTimings() {
        assertEquals(300, PROGRESS_SWAP_DURATION_MILLIS)
        assertEquals(120, PROGRESS_FILL_COMPLETION_DURATION_MILLIS)
        assertEquals(120, PROGRESS_OVERLAY_FADE_DELAY_MILLIS)
        assertEquals(160, PROGRESS_OVERLAY_FADE_DURATION_MILLIS)
    }

    @Test
    fun swapCurveStartsEndsAndOvershoots() {
        assertEquals(0f, progressSwapEasingValue(0f), 0.0001f)
        assertEquals(1f, progressSwapEasingValue(1f), 0.0001f)

        val maxValue =
            (0..100).maxOf { index ->
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
        assertEquals(1f, progressOverlayOpacityValue(PROGRESS_OVERLAY_FADE_DELAY_MILLIS), 0.0001f)

        val midpoint = progressOverlayOpacityValue(PROGRESS_OVERLAY_FADE_DELAY_MILLIS + 80)
        assertTrue("expected midpoint opacity below 1, was $midpoint", midpoint < 1f)
        assertTrue("expected midpoint opacity above 0, was $midpoint", midpoint > 0f)

        assertEquals(
            0f,
            progressOverlayOpacityValue(PROGRESS_OVERLAY_FADE_DELAY_MILLIS + PROGRESS_OVERLAY_FADE_DURATION_MILLIS),
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
