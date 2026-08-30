package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceholderShimmerTest {
    @Test
    fun loopDurationMatchesFlutterAndSwift() {
        assertEquals(3223, PLACEHOLDER_LOOP_DURATION_MILLIS)
    }

    @Test
    fun animationRequiresFlagAndPositiveWidth() {
        assertFalse(shouldRunPlaceholderAnimation(animated = false, measuredWidthPx = 100f))
        assertFalse(shouldRunPlaceholderAnimation(animated = true, measuredWidthPx = 0f))
        assertTrue(shouldRunPlaceholderAnimation(animated = true, measuredWidthPx = 100f))
    }

    @Test
    fun loopPhaseWrapsAcrossRepeatedCycles() {
        assertEquals(0f, placeholderLoopPhase(0), 0.0001f)
        assertEquals(0.5f, placeholderLoopPhase(PLACEHOLDER_LOOP_DURATION_MILLIS / 2L), 0.001f)
        assertEquals(0f, placeholderLoopPhase(PLACEHOLDER_LOOP_DURATION_MILLIS.toLong()), 0.0001f)
        assertEquals(0.25f, placeholderLoopPhase((PLACEHOLDER_LOOP_DURATION_MILLIS * 1.25f).toLong()), 0.001f)
        assertEquals(0.75f, placeholderLoopPhase((PLACEHOLDER_LOOP_DURATION_MILLIS * 3.75f).toLong()), 0.001f)
    }

    @Test
    fun shimmerWidthUsesFortyPercentOfLaneWidth() {
        assertEquals(40f, placeholderShimmerWidth(100f), 0.0001f)
    }

    @Test
    fun shimmerLeadingXPingPongsAcrossLane() {
        val laneWidthPx = 100f
        val bandWidthPx = placeholderShimmerWidth(laneWidthPx)

        assertEquals(-40f, placeholderShimmerLeadingX(0f, laneWidthPx, bandWidthPx), 0.0001f)
        assertEquals(30f, placeholderShimmerLeadingX(0.25f, laneWidthPx, bandWidthPx), 0.0001f)
        assertEquals(100f, placeholderShimmerLeadingX(0.5f, laneWidthPx, bandWidthPx), 0.0001f)
        assertEquals(30f, placeholderShimmerLeadingX(0.75f, laneWidthPx, bandWidthPx), 0.0001f)
        assertEquals(-40f, placeholderShimmerLeadingX(1f, laneWidthPx, bandWidthPx), 0.0001f)
    }

    @Test
    fun visibleSegmentClipsVirtualPathToLane() {
        val laneWidthPx = 100f
        val bandWidthPx = placeholderShimmerWidth(laneWidthPx)

        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 0f, widthPx = 0f),
            placeholderVisibleShimmerSegment(0f, laneWidthPx, bandWidthPx),
        )
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 30f, widthPx = 40f),
            placeholderVisibleShimmerSegment(0.25f, laneWidthPx, bandWidthPx),
        )
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 100f, widthPx = 0f),
            placeholderVisibleShimmerSegment(0.5f, laneWidthPx, bandWidthPx),
        )
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 30f, widthPx = 40f),
            placeholderVisibleShimmerSegment(0.75f, laneWidthPx, bandWidthPx),
        )
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 0f, widthPx = 0f),
            placeholderVisibleShimmerSegment(1f, laneWidthPx, bandWidthPx),
        )
    }

    @Test
    fun visibleSegmentGrowsAndShrinksAtEdges() {
        val laneWidthPx = 100f
        val bandWidthPx = placeholderShimmerWidth(laneWidthPx)

        val enteringLeft = placeholderVisibleShimmerSegment(0.125f, laneWidthPx, bandWidthPx)
        assertEquals(0f, enteringLeft.leadingXPx, 0.0001f)
        assertEquals(35f, enteringLeft.widthPx, 0.0001f)

        val exitingRight = placeholderVisibleShimmerSegment(0.375f, laneWidthPx, bandWidthPx)
        assertEquals(65f, exitingRight.leadingXPx, 0.0001f)
        assertEquals(35f, exitingRight.widthPx, 0.0001f)
    }

    @Test
    fun visibleSegmentStaysInsideLaneForSampledPhases() {
        val laneWidthPx = 100f
        val bandWidthPx = placeholderShimmerWidth(laneWidthPx)

        for (index in 0..40) {
            val phase = index / 40f
            val segment = placeholderVisibleShimmerSegment(phase, laneWidthPx, bandWidthPx)

            assertTrue("phase: $phase", segment.leadingXPx >= 0f)
            assertTrue("phase: $phase", segment.widthPx >= 0f)
            assertTrue("phase: $phase", segment.leadingXPx + segment.widthPx <= laneWidthPx)
        }
    }

    @Test
    fun helpersHandleZeroWidthGracefully() {
        assertEquals(0f, placeholderShimmerWidth(0f), 0.0001f)
        assertEquals(0f, placeholderShimmerLeadingX(0.5f, laneWidthPx = 0f, bandWidthPx = 0f), 0.0001f)
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 0f, widthPx = 0f),
            placeholderVisibleShimmerSegment(0.5f, laneWidthPx = 0f, bandWidthPx = 40f),
        )
        assertEquals(
            PlaceholderShimmerVisibleSegment(leadingXPx = 0f, widthPx = 0f),
            placeholderVisibleShimmerSegment(0.5f, laneWidthPx = 100f, bandWidthPx = 0f),
        )
    }
}
