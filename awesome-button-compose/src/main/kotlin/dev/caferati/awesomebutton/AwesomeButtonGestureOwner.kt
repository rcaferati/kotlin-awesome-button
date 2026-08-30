package dev.caferati.awesomebutton

/**
 * Owns the terminal decision for one native pointer gesture.
 *
 * The owner deliberately contains no Compose state or callbacks. Compose supplies live dependencies
 * at each dispatch boundary, while this object makes terminal ownership and long-press arming
 * deterministic and independently testable.
 */
internal class AwesomeButtonGestureOwner {
    private var generation = 0L
    private var activeGeneration: Long? = null
    private var longPressArmedAtStart = false
    private var longPressDisarmed = false
    private var longPressDispatched = false
    private var pressInReady = false
    private var pendingFinishInside: Boolean? = null

    val hasActiveGesture: Boolean
        get() = activeGeneration != null

    fun begin(longPressEligible: Boolean): Long? {
        if (activeGeneration != null) {
            return null
        }

        generation += 1
        activeGeneration = generation
        longPressArmedAtStart = longPressEligible
        longPressDisarmed = false
        longPressDispatched = false
        pressInReady = false
        pendingFinishInside = null
        return generation
    }

    fun isActive(token: Long): Boolean = activeGeneration == token

    fun observeLongPressAvailability(hasHandler: Boolean) {
        if (activeGeneration != null && longPressArmedAtStart && !hasHandler) {
            longPressDisarmed = true
        }
    }

    fun tryDispatchLongPress(
        token: Long,
        hasHandler: Boolean,
    ): Boolean {
        if (
            activeGeneration != token ||
            !longPressArmedAtStart ||
            longPressDisarmed ||
            longPressDispatched ||
            !hasHandler
        ) {
            return false
        }

        longPressDispatched = true
        return true
    }

    fun markPressInReady(token: Long): AwesomeButtonGestureOutcome {
        if (activeGeneration != token) {
            return AwesomeButtonGestureOutcome.Ignore
        }

        pressInReady = true
        val inside = pendingFinishInside ?: return AwesomeButtonGestureOutcome.Ignore
        return finishNow(inside)
    }

    fun finish(
        token: Long,
        inside: Boolean,
    ): AwesomeButtonGestureOutcome {
        if (activeGeneration != token) {
            return AwesomeButtonGestureOutcome.Ignore
        }

        if (!pressInReady) {
            pendingFinishInside = inside
            return AwesomeButtonGestureOutcome.Ignore
        }

        return finishNow(inside)
    }

    private fun finishNow(inside: Boolean): AwesomeButtonGestureOutcome {
        activeGeneration = null
        pendingFinishInside = null
        return when {
            longPressDispatched -> AwesomeButtonGestureOutcome.LongPressCleanup
            inside -> AwesomeButtonGestureOutcome.Activate
            else -> AwesomeButtonGestureOutcome.Cancel
        }
    }

    fun cancelActive(): AwesomeButtonGestureOutcome {
        if (activeGeneration == null) {
            return AwesomeButtonGestureOutcome.Ignore
        }

        activeGeneration = null
        pendingFinishInside = null
        return if (longPressDispatched) {
            AwesomeButtonGestureOutcome.LongPressCleanup
        } else {
            AwesomeButtonGestureOutcome.Cancel
        }
    }

    fun teardown() {
        generation += 1
        activeGeneration = null
        longPressArmedAtStart = false
        longPressDisarmed = true
        longPressDispatched = false
        pressInReady = false
        pendingFinishInside = null
    }
}

internal enum class AwesomeButtonGestureOutcome {
    Activate,
    LongPressCleanup,
    Cancel,
    Ignore,
}
