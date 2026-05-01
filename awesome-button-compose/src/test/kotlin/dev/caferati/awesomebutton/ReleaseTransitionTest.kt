package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class ReleaseTransitionTest {
    @Test
    fun releaseConstantsMatchSwift() {
        assertEquals(280f, ReleaseSpringStiffness, 0f)
        assertEquals(20f, ReleaseSpringDamping, 0f)
        assertEquals(240, ReleaseSpringSettleDurationMillis)
        assertEquals(-0.25f, ReleaseGeometryPressProgressFloor, 0f)
        assertEquals(
            20f / (2f * sqrt(280f)),
            ReleaseSpringDampingRatio,
            0.0001f,
        )
    }

    @Test
    fun visualPressProgressIsClampedToNormalRange() {
        assertEquals(0f, clampedVisualPressProgress(-0.12f), 0f)
        assertEquals(0.5f, clampedVisualPressProgress(0.5f), 0f)
        assertEquals(1f, clampedVisualPressProgress(1.2f), 0f)
    }

    @Test
    fun shellGeometryPressProgressAllowsBoundedReleaseOvershoot() {
        assertEquals(-0.12f, shellGeometryPressProgress(-0.12f), 0f)
        assertEquals(0.5f, shellGeometryPressProgress(0.5f), 0f)
        assertEquals(-0.25f, shellGeometryPressProgress(-1f), 0f)
        assertEquals(1f, shellGeometryPressProgress(1.2f), 0f)
    }
}
