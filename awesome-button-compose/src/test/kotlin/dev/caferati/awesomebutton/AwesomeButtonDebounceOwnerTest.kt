package dev.caferati.awesomebutton

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AwesomeButtonDebounceOwnerTest {
    @Test
    fun firstActivationAtUptimeZeroIsAccepted() {
        val owner = AwesomeButtonDebounceOwner()

        assertTrue(owner.tryAccept(nowMillis = 0, durationMillis = 500))
        assertFalse(owner.tryAccept(nowMillis = 1, durationMillis = 500))
    }

    @Test
    fun insideBoundaryAndOutsideWindowFollowMonotonicContract() {
        val owner = AwesomeButtonDebounceOwner()

        assertTrue(owner.tryAccept(nowMillis = 10, durationMillis = 100))
        assertFalse(owner.tryAccept(nowMillis = 109, durationMillis = 100))
        assertTrue(owner.tryAccept(nowMillis = 110, durationMillis = 100))
        assertTrue(owner.tryAccept(nowMillis = 211, durationMillis = 100))
    }

    @Test
    fun durationReplacementUsesOnlyRealAcceptanceHistory() {
        val owner = AwesomeButtonDebounceOwner()

        assertTrue(owner.tryAccept(nowMillis = 0, durationMillis = 0))
        assertFalse(owner.tryAccept(nowMillis = 50, durationMillis = 100))
        assertTrue(owner.tryAccept(nowMillis = 100, durationMillis = 100))

        val freshOwner = AwesomeButtonDebounceOwner()
        assertTrue(freshOwner.tryAccept(nowMillis = 1, durationMillis = 10_000))
    }

    @Test
    fun rejectedActivationDoesNotMoveTheWindow() {
        val owner = AwesomeButtonDebounceOwner()

        assertTrue(owner.tryAccept(nowMillis = 100, durationMillis = 100))
        assertFalse(owner.tryAccept(nowMillis = 150, durationMillis = 100))
        assertTrue(owner.tryAccept(nowMillis = 200, durationMillis = 100))
    }
}
