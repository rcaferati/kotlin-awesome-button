package dev.caferati.awesomebutton

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AwesomeButtonGeometryTest {
    @Test
    fun totalShellHeightEqualsDocumentedFaceHeightPlusRaiseAmount() {
        val geometry = AwesomeButtonGeometry(faceHeight = 52.dp, raiseAmount = 6.dp)

        assertEquals(58.dp, geometry.totalHeight)
        assertEquals(46.dp, geometry.shadowHeight)
        assertEquals(15.dp, geometry.shadowTopOffset(0f))
        assertEquals(12.dp, geometry.shadowTopOffset(1f))
        assertEquals(15.75f.dp, geometry.shadowTopOffset(-0.25f))
        assertEquals(0.dp, geometry.faceTopOffset(0f))
        assertEquals(6.dp, geometry.faceTopOffset(1f))
        assertEquals((-1.5f).dp, geometry.faceTopOffset(-0.25f))
        assertEquals((-1.5f).dp, geometry.faceTopOffset(-1f))
    }

    @Test
    fun shadowHeightNeverGoesBelowZero() {
        val geometry = AwesomeButtonGeometry(faceHeight = 4.dp, raiseAmount = 8.dp)

        assertEquals(0.dp, geometry.shadowHeight)
    }
}
