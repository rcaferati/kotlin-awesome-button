package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextTransitionTest {
    @Test
    fun timelineMatchesSwiftTimingForEqualGrowingAndShrinkingText() {
        val equal = getTextTransitionTimeline("abc", "xyz")
        assertEquals(3, equal.maxLength)
        assertEquals(14, equal.lastRandomizeStartMillis)
        assertEquals(24, equal.collapseStartMillis)
        assertEquals(38, equal.totalDurationMillis)

        val growing = getTextTransitionTimeline("Go", "Mission#42")
        assertEquals(10, growing.maxLength)
        assertEquals(63, growing.lastRandomizeStartMillis)
        assertEquals(73, growing.collapseStartMillis)
        assertEquals(136, growing.totalDurationMillis)

        val shrinking = getTextTransitionTimeline("welcome", "Go")
        assertEquals(7, shrinking.maxLength)
        assertEquals(42, shrinking.lastRandomizeStartMillis)
        assertEquals(52, shrinking.collapseStartMillis)
        assertEquals(94, shrinking.totalDurationMillis)
    }

    @Test
    fun collapseOrderingMatchesSwiftForShrinkingText() {
        val timeline = getTextTransitionTimeline("welcome", "Go")

        assertEquals(87, getTextTransitionCollapseMillis(0, timeline))
        assertEquals(94, getTextTransitionCollapseMillis(1, timeline))
        assertEquals(80, getTextTransitionCollapseMillis(2, timeline))
        assertEquals(52, getTextTransitionCollapseMillis(6, timeline))
    }

    @Test
    fun charsetsMatchSwiftCharacterGroups() {
        assertEquals("iljtfr".toList(), getTextTransitionCharset('i'))
        assertEquals("ACESUVXZNHO".toList(), getTextTransitionCharset('A'))
        assertEquals("MWDBPQGY".toList(), getTextTransitionCharset('M'))
        assertEquals("0123456789".toList(), getTextTransitionCharset('7'))
        assertEquals("#%&^+=-".toList(), getTextTransitionCharset('@'))
        assertNull(getTextTransitionCharset(' '))
    }

    @Test
    fun frameBuilderUsesDeterministicRandomCharactersBeforeCollapse() {
        val frame =
            buildTextTransitionFrame(
                fromText = "1#",
                targetText = "9-",
                elapsedMillis = 8,
                random = { 0.0 },
            )

        assertEquals("0#", frame)
        assertEquals("9-", buildTextTransitionFrame("1#", "9-", elapsedMillis = 80))
    }

    @Test
    fun textUpdatePlanMatchesSwiftRules() {
        assertEquals(
            ButtonTextUpdatePlan.Assign("next"),
            resolveButtonTextUpdatePlan(false, "next", currentTarget = "old", displayedText = "old"),
        )
        assertEquals(
            ButtonTextUpdatePlan.Assign(""),
            resolveButtonTextUpdatePlan(true, "", currentTarget = "old", displayedText = "old"),
        )
        assertEquals(
            ButtonTextUpdatePlan.Keep,
            resolveButtonTextUpdatePlan(true, "old", currentTarget = "old", displayedText = "old"),
        )
        assertEquals(
            ButtonTextUpdatePlan.Transition("old", "next"),
            resolveButtonTextUpdatePlan(true, "next", currentTarget = "old", displayedText = "old"),
        )
    }

    @Test
    fun autoWidthTimingUsesThirtyPercentOffset() {
        val growing = resolveAutoWidthTextTransitionTiming("Go", "Mission#42", AutoWidthTextFlow.GrowFirst)
        assertEquals(0, growing.widthDelayMillis)
        assertEquals(41, growing.textDelayMillis)
        assertEquals(136, growing.widthDurationMillis)

        val shrinking = resolveAutoWidthTextTransitionTiming("welcome", "Go", AutoWidthTextFlow.ShrinkLast)
        assertEquals(28, shrinking.widthDelayMillis)
        assertEquals(0, shrinking.textDelayMillis)
        assertEquals(94, shrinking.widthDurationMillis)
    }

    @Test
    fun autoWidthFlowResolvesFromMeasuredWidths() {
        assertEquals(AutoWidthTextFlow.Initial, resolveAutoWidthTextFlow(null, 100))
        assertEquals(AutoWidthTextFlow.TextOnly, resolveAutoWidthTextFlow(100, 100))
        assertEquals(AutoWidthTextFlow.GrowFirst, resolveAutoWidthTextFlow(80, 100))
        assertEquals(AutoWidthTextFlow.ShrinkLast, resolveAutoWidthTextFlow(120, 100))
        assertEquals(0, normalizeTextTransitionSlotStaggerMillis(0))
    }

    @Test
    fun autoWidthUpdatePlansMatchSwiftRules() {
        assertEquals(
            AutoWidthTextUpdatePlan.FallbackToTextSync,
            resolveAutoWidthTextUpdatePlan(
                isEligible = false,
                targetText = "Launch",
                currentWidthPx = null,
                targetWidthPx = 120,
                displayedText = null,
                animateSize = true,
                textTransition = true,
            ),
        )

        assertEquals(
            AutoWidthTextUpdatePlan.Initial("Launch", 120),
            resolveAutoWidthTextUpdatePlan(
                isEligible = true,
                targetText = "Launch",
                currentWidthPx = null,
                targetWidthPx = 120,
                displayedText = null,
                animateSize = true,
                textTransition = true,
            ),
        )

        assertEquals(
            AutoWidthTextUpdatePlan.TextOnly("Save", "Open", animateText = true),
            resolveAutoWidthTextUpdatePlan(
                isEligible = true,
                targetText = "Open",
                currentWidthPx = 120,
                targetWidthPx = 120,
                displayedText = "Save",
                animateSize = true,
                textTransition = true,
            ),
        )

        val growTiming =
            resolveAutoWidthTextTransitionTiming("Launch", "View analytics dashboard", AutoWidthTextFlow.GrowFirst)
        assertEquals(
            AutoWidthTextUpdatePlan.GrowFirst(
                sourceText = "Launch",
                targetText = "View analytics dashboard",
                targetWidthPx = 240,
                timing = growTiming,
                animateSize = true,
                animateText = false,
            ),
            resolveAutoWidthTextUpdatePlan(
                isEligible = true,
                targetText = "View analytics dashboard",
                currentWidthPx = 80,
                targetWidthPx = 240,
                displayedText = "Launch",
                animateSize = true,
                textTransition = false,
            ),
        )
    }

    @Test
    fun releaseDeferralMatchesSwiftEligibility() {
        assertTrue(
            shouldDeferReleaseAutoWidthTransition(
                isReleaseActive = true,
                previousWidthMode = ButtonWidthMode.Auto,
                nextWidthMode = ButtonWidthMode.Auto,
                currentAutoWidthTextEligible = true,
                nextAutoWidthTextEligible = true,
                currentTextTransition = true,
                nextTextTransition = true,
                nextAnimateSize = true,
                sizeSignatureUnchanged = true,
                currentText = "Launch",
                nextText = "View analytics dashboard",
                currentWidthPx = 80,
                targetWidthPx = 240,
            ),
        )

        assertEquals(
            false,
            shouldDeferReleaseAutoWidthTransition(
                isReleaseActive = true,
                previousWidthMode = ButtonWidthMode.Auto,
                nextWidthMode = ButtonWidthMode.Fixed,
                currentAutoWidthTextEligible = true,
                nextAutoWidthTextEligible = true,
                currentTextTransition = true,
                nextTextTransition = true,
                nextAnimateSize = true,
                sizeSignatureUnchanged = true,
                currentText = "Launch",
                nextText = "View analytics dashboard",
                currentWidthPx = 80,
                targetWidthPx = 240,
            ),
        )
    }

    @Test
    fun sizeAnimationSpecMatchesSwift() {
        assertEquals(175, SIZE_ANIMATION_DURATION_MILLIS)
        assertEquals(0f, sizeAnimationEasing.transform(0f), 0.0001f)
        assertEquals(1f, sizeAnimationEasing.transform(1f), 0.0001f)
        assertTrue(sizeAnimationEasing.transform(0.5f) > 0.5f)
    }
}
