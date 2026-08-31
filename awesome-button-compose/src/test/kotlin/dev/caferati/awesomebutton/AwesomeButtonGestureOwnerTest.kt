package dev.caferati.awesomebutton

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AwesomeButtonGestureOwnerTest {
    @Test
    fun ordinaryGestureActivatesExactlyOnce() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = false))
        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.markPressInReady(token))

        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.finish(token, inside = true))
        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.finish(token, inside = true))
    }

    @Test
    fun cancellationTerminatesExactlyOnce() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = true))

        assertEquals(AwesomeButtonGestureOutcome.Cancel, owner.cancelActive())
        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.finish(token, inside = true))
        assertFalse(owner.isActive(token))
    }

    @Test
    fun gestureBeginningWithoutHandlerCannotBeArmedLater() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = false))

        owner.observeLongPressAvailability(hasHandler = true)
        owner.markPressInReady(token)

        assertFalse(owner.tryDispatchLongPress(token, hasHandler = true))
        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.finish(token, inside = true))
    }

    @Test
    fun directNonNilReplacementRetainsLongPressEligibility() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = true))

        owner.observeLongPressAvailability(hasHandler = true)
        owner.markPressInReady(token)

        assertTrue(owner.tryDispatchLongPress(token, hasHandler = true))
        assertEquals(AwesomeButtonGestureOutcome.LongPressCleanup, owner.finish(token, inside = true))
    }

    @Test
    fun committedNilPermanentlyDisarmsCurrentGesture() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = true))

        owner.observeLongPressAvailability(hasHandler = false)
        owner.observeLongPressAvailability(hasHandler = true)
        owner.markPressInReady(token)

        assertFalse(owner.tryDispatchLongPress(token, hasHandler = true))
        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.finish(token, inside = true))
    }

    @Test
    fun longPressBecomesTerminalOnlyAfterARealDispatch() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = true))
        owner.markPressInReady(token)

        assertFalse(owner.tryDispatchLongPress(token, hasHandler = false))
        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.finish(token, inside = true))
    }

    @Test
    fun terminalRequestWaitsForPressInCallbackBoundary() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = false))

        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.finish(token, inside = true))
        assertTrue(owner.isActive(token))
        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.markPressInReady(token))
        assertFalse(owner.isActive(token))
        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.markPressInReady(token))
    }

    @Test
    fun teardownInvalidatesTheTokenWithoutATerminalCallbackPath() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = true))

        owner.teardown()

        assertFalse(owner.isActive(token))
        assertEquals(AwesomeButtonGestureOutcome.Ignore, owner.finish(token, inside = true))
        assertFalse(owner.tryDispatchLongPress(token, hasHandler = true))
    }

    @Test
    fun overlappingBeginCannotStealTheActiveGesture() {
        val owner = AwesomeButtonGestureOwner()
        val token = requireNotNull(owner.begin(longPressEligible = false))

        assertEquals(null, owner.begin(longPressEligible = true))
        owner.markPressInReady(token)
        assertEquals(AwesomeButtonGestureOutcome.Activate, owner.finish(token, inside = true))
        assertTrue(owner.begin(longPressEligible = true) != null)
    }
}
