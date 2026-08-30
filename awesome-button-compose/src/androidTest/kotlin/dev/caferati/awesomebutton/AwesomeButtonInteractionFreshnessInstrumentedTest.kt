package dev.caferati.awesomebutton

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonInteractionFreshnessInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun idleAndRapidCallbackReplacementUsesOnlyLatestUndispatchedCallbacks() {
        val callbackVersion = mutableStateOf("A")
        val events = mutableListOf<String>()
        composeRule.setContent {
            val version = callbackVersion.value
            AwesomeButton(
                child = "Replace",
                onPressIn = { events += "in-$version" },
                onPressOut = { events += "out-$version" },
                onPress = { events += "press-$version" },
            )
        }

        composeRule.runOnIdle {
            callbackVersion.value = "B"
            callbackVersion.value = "C"
        }
        composeRule.waitForIdle()
        tapButton()
        composeRule.waitForIdle()

        assertEquals(listOf("in-C", "out-C", "press-C"), events)
    }

    @Test
    fun callbackReplacementWhileHeldDoesNotReplayPressInAndUsesLatestReleaseOwners() {
        val callbackVersion = mutableStateOf("A")
        val events = mutableListOf<String>()
        composeRule.setContent {
            val version = callbackVersion.value
            AwesomeButton(
                child = "Held",
                onPressIn = { events += "in-$version" },
                onPressedIn = { events += "pressed-in-$version" },
                onPressOut = { events += "out-$version" },
                onPressedOut = { events += "pressed-out-$version" },
                onPress = { events += "press-$version" },
            )
        }

        pressButtonDown()
        composeRule.runOnIdle {
            callbackVersion.value = "B"
        }
        composeRule.waitForIdle()
        releaseButton()
        composeRule.waitForIdle()

        assertEquals(
            listOf("in-A", "pressed-in-A", "out-B", "press-B", "pressed-out-B"),
            events,
        )
    }

    @Test
    fun lifecycleCallbackUpdatesUseLatestUndispatchedOwnerAndKeepReleaseSnapshot() {
        val callbackVersion = mutableStateOf("A")
        val events = mutableListOf<String>()
        composeRule.setContent {
            val version = callbackVersion.value
            AwesomeButton(
                child = "Boundaries",
                onPressIn = {
                    events += "in-$version"
                    callbackVersion.value = "B"
                },
                onPressedIn = { events += "pressed-in-$version" },
                onPressOut = {
                    events += "out-$version"
                    callbackVersion.value = "C"
                },
                onPress = { events += "press-$version" },
                onPressedOut = { events += "pressed-out-$version" },
            )
        }

        tapButton()
        composeRule.waitForIdle()

        assertEquals(
            listOf("in-A", "pressed-in-B", "out-B", "press-C", "pressed-out-B"),
            events,
        )
    }

    @Test
    fun progressAndDebounceReplacementWhileHeldDoesNotCancelGesture() {
        val progress = mutableStateOf(true)
        val debounce = mutableStateOf(10_000L)
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Options",
                progress = progress.value,
                debouncedPressTimeMillis = debounce.value,
                onPressOut = { events += "out" },
                onPress = { next -> events += if (next == null) "ordinary" else "progress" },
            )
        }

        pressButtonDown()
        composeRule.runOnIdle {
            progress.value = false
            debounce.value = 0
        }
        composeRule.waitForIdle()
        releaseButton()
        composeRule.waitForIdle()

        assertEquals(listOf("out", "ordinary"), events)
    }

    @Test
    fun firstActivationWithLargeDebounceIsAcceptedAndReplacementUsesRealHistory() {
        val debounce = mutableStateOf(Long.MAX_VALUE)
        var presses = 0
        composeRule.setContent {
            AwesomeButton(
                child = "Debounce",
                debouncedPressTimeMillis = debounce.value,
                onPress = { presses += 1 },
            )
        }

        tapButton()
        composeRule.waitForIdle()
        assertEquals(1, presses)

        tapButton()
        composeRule.waitForIdle()
        assertEquals(1, presses)

        composeRule.runOnIdle { debounce.value = 0 }
        tapButton()
        composeRule.waitForIdle()
        assertEquals(2, presses)
    }

    @Test
    fun disablementAndPlaceholderCancelHeldGestureAndSettleExactlyOnce() {
        val disabled = mutableStateOf(false)
        val child = mutableStateOf<String?>("Ready")
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = child.value,
                disabled = disabled.value,
                onPress = { events += "press" },
                onLongPress = { events += "long" },
                onPressOut = { events += "out" },
                onPressedOut = { events += "pressed-out" },
            )
        }

        pressButtonDown()
        composeRule.runOnIdle { disabled.value = true }
        composeRule.waitForIdle()

        assertEquals(listOf("out", "pressed-out"), events)
        assertClose(
            0f,
            semanticsFloat("AwesomeButtonFace", awesomeButtonRawPressProgressKey),
            tolerance = 0.01f,
        )
        releaseTouchOnRoot()

        composeRule.runOnIdle {
            disabled.value = false
            child.value = "Ready"
            events.clear()
        }
        composeRule.waitForIdle()
        pressButtonDown()
        composeRule.runOnIdle { child.value = null }
        composeRule.waitForIdle()

        assertEquals(listOf("out", "pressed-out"), events)
        assertClose(
            0f,
            semanticsFloat("AwesomeButtonFace", awesomeButtonRawPressProgressKey),
            tolerance = 0.01f,
        )
        releaseTouchOnRoot()
    }

    @Test
    fun removalDuringHoldIsSilentAfterAlreadyDispatchedPressIn() {
        val mounted = mutableStateOf(true)
        val events = mutableListOf<String>()
        composeRule.setContent {
            if (mounted.value) {
                AwesomeButton(
                    child = "Remove",
                    onPressIn = { events += "in" },
                    onPressOut = { events += "out" },
                    onPressedOut = { events += "pressed-out" },
                    onPress = { events += "press" },
                    onLongPress = { events += "long" },
                )
            }
        }

        pressButtonDown()
        composeRule.runOnIdle { mounted.value = false }
        composeRule.waitForIdle()
        releaseTouchOnRoot()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(listOf("in"), events)
    }

    @Test
    fun longPressHandlerAdditionRemovalAndReplacementFollowMonotonicArming() {
        val handler = mutableStateOf<(() -> Unit)?>(null)
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Long",
                onPress = { events += "press" },
                onLongPress = handler.value,
                onPressOut = { events += "out" },
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            pressButtonDown()
            composeRule.runOnIdle { handler.value = { events += "late" } }
            composeRule.mainClock.advanceTimeBy(600)
            releaseButton()
            composeRule.mainClock.advanceTimeBy(1_000)
            assertEquals(listOf("out", "press"), events)

            events.clear()
            composeRule.runOnIdle { handler.value = { events += "A" } }
            pressButtonDown()
            composeRule.runOnIdle { handler.value = { events += "B" } }
            composeRule.mainClock.advanceTimeBy(600)
            releaseButton()
            composeRule.mainClock.advanceTimeBy(1_000)
            assertEquals(listOf("B", "out"), events)

            events.clear()
            composeRule.runOnIdle { handler.value = { events += "A" } }
            pressButtonDown()
            composeRule.runOnIdle { handler.value = null }
            composeRule.runOnIdle { handler.value = { events += "B" } }
            composeRule.mainClock.advanceTimeBy(600)
            releaseButton()
            composeRule.mainClock.advanceTimeBy(1_000)
            assertEquals(listOf("out", "press"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun ordinaryHoldWithoutHandlerAndPointerLossHaveDistinctTerminalOutcomes() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Pointer",
                onPress = { events += "press" },
                onPressOut = { events += "out" },
                onPressedOut = { events += "pressed-out" },
            )
        }

        composeRule.onNodeWithTag("AwesomeButton").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf("out", "press", "pressed-out"), events)

        events.clear()
        composeRule.onNodeWithTag("AwesomeButton").performTouchInput {
            down(center)
            moveTo(Offset(-100f, -100f))
            up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf("out", "pressed-out"), events)
    }

    @Test
    fun releaseCompletionKeepsTransitionStartSnapshot() {
        val pressedOutVersion = mutableStateOf("A")
        val events = mutableListOf<String>()
        composeRule.setContent {
            val version = pressedOutVersion.value
            AwesomeButton(
                child = "Snapshot",
                onPress = { events += "press" },
                onPressedOut = { events += "pressed-out-$version" },
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle { pressedOutVersion.value = "B" }
            composeRule.mainClock.advanceTimeBy(1_000)

            assertEquals(listOf("press", "pressed-out-A"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun reentrantStructuralUpdatesDoNotLeakASecondTerminalPath() {
        val disabled = mutableStateOf(false)
        val mounted = mutableStateOf(true)
        val scenario = mutableStateOf(0)
        val events = mutableListOf<String>()
        composeRule.setContent {
            if (mounted.value) {
                if (scenario.value == 0) {
                    AwesomeButton(
                        child = "Reentrant",
                        disabled = disabled.value,
                        onPressIn = {
                            events += "in"
                            disabled.value = true
                        },
                        onPressedIn = { events += "pressed-in" },
                        onPressOut = { events += "out" },
                        onPressedOut = { events += "pressed-out" },
                        onPress = { events += "press" },
                        onLongPress = { events += "long" },
                    )
                } else {
                    AwesomeButton(
                        child = "Teardown",
                        onPressOut = {
                            events += "out"
                            mounted.value = false
                        },
                        onPressedOut = { events += "pressed-out" },
                        onPress = { events += "press" },
                    )
                }
            }
        }

        tapButton()
        composeRule.waitForIdle()

        assertEquals(listOf("in", "out", "pressed-out"), events)

        composeRule.runOnIdle {
            disabled.value = false
            events.clear()
            scenario.value = 1
        }
        composeRule.waitForIdle()
        tapButton()
        composeRule.waitForIdle()

        assertEquals(listOf("out"), events)
    }

    @Test
    fun mountedDisablementFromOnPressedInSettlesWithoutActivation() {
        val disabled = mutableStateOf(false)
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Pressed-in invalidation",
                disabled = disabled.value,
                onPressIn = { events += "in" },
                onPressedIn = {
                    events += "pressed-in"
                    disabled.value = true
                },
                onPressOut = { events += "out" },
                onPress = { events += "press" },
                onPressedOut = { events += "pressed-out" },
            )
        }

        tapButton()
        composeRule.waitForIdle()

        assertEquals(listOf("in", "pressed-in", "out", "pressed-out"), events)
    }
}
