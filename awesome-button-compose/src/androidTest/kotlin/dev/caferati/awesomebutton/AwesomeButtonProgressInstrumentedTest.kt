package dev.caferati.awesomebutton

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonProgressInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun deferredProgressPressUsesLatestCallbackAndRollsBackWhenItBecomesAbsent() {
        val callback =
            mutableStateOf<AwesomeButtonPressCallback?>(
                { next -> next?.invoke() },
            )
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Fresh",
                width = 200.dp,
                progress = true,
                onPress = callback.value,
                onPressedOut = { events += "pressed-out" },
                onProgressStart = { events += "start" },
                onProgressEnd = { events += "end" },
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.runOnIdle {
                callback.value = { next ->
                    events += "B"
                    next?.invoke()
                }
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_200)
            assertEquals(listOf("start", "B", "pressed-out", "end"), events)

            events.clear()
            composeRule.runOnIdle {
                callback.value = { events += "initial" }
            }
            tapButton()
            composeRule.runOnIdle {
                callback.value = null
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_200)
            assertEquals(listOf("start", "pressed-out", "end"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressCompletionSnapshotsAcceptedOwnersAndFinalReleaseUsesItsOwnSnapshot() {
        val pressedOutVersion = mutableStateOf("A")
        val progressEndVersion = mutableStateOf("A")
        val events = mutableListOf<String>()
        var capturedNext: AwesomeButtonNext? = null
        composeRule.setContent {
            val pressedOut = pressedOutVersion.value
            val progressEnd = progressEndVersion.value
            AwesomeButton(
                child = "Snapshot",
                width = 200.dp,
                progress = true,
                onPress = { next -> capturedNext = next },
                onPressedOut = { events += "pressed-out-$pressedOut" },
                onProgressEnd = { events += "end-$progressEnd" },
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle {
                capturedNext?.invoke { events += "completion" }
                progressEndVersion.value = "B"
                pressedOutVersion.value = "B"
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            assertEquals(
                listOf("pressed-out-B", "completion", "end-A"),
                events,
            )
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun synchronousProgressCompletionIsQueuedAndTeardownCancelsLaterCallbacks() {
        val mounted = mutableStateOf(true)
        val events = mutableListOf<String>()
        composeRule.setContent {
            if (mounted.value) {
                AwesomeButton(
                    child = "Remove",
                    width = 200.dp,
                    progress = true,
                    onPress = { next ->
                        events += "press"
                        next?.invoke { events += "completion" }
                        next?.invoke { events += "duplicate" }
                        mounted.value = false
                        events += "press-return"
                    },
                    onPressedOut = { events += "pressed-out" },
                    onProgressEnd = { events += "end" },
                )
            }
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_200)

            assertEquals(listOf("press", "press-return"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun structuralUpdatesFromProgressCallbacksSettleOrTeardownOnce() {
        val disabled = mutableStateOf(false)
        val mounted = mutableStateOf(true)
        val scenario = mutableStateOf(0)
        val events = mutableListOf<String>()
        composeRule.setContent {
            if (mounted.value) {
                if (scenario.value == 0) {
                    AwesomeButton(
                        child = "Reentrant",
                        width = 200.dp,
                        disabled = disabled.value,
                        progress = true,
                        onProgressStart = {
                            events += "start"
                            disabled.value = true
                        },
                        onPress = { events += "press" },
                        onPressedOut = { events += "pressed-out" },
                        onProgressEnd = { events += "end" },
                    )
                } else {
                    AwesomeButton(
                        child = "Deferred",
                        width = 200.dp,
                        progress = true,
                        onProgressStart = { events += "start" },
                        onPress = { next ->
                            events += "press"
                            next?.invoke()
                            mounted.value = false
                        },
                        onPressedOut = { events += "pressed-out" },
                        onProgressEnd = { events += "end" },
                    )
                }
            }
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_200)

            assertEquals(listOf("start", "pressed-out", "end"), events)

            composeRule.runOnIdle {
                disabled.value = false
                events.clear()
                scenario.value = 1
            }
            composeRule.waitForIdle()
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_200)

            assertEquals(listOf("start", "press"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun mountedInvalidationAfterProgressDispatchRollsBackAndRejectsCompletion() {
        val disabled = mutableStateOf(false)
        val events = mutableListOf<String>()
        var capturedNext: AwesomeButtonNext? = null
        composeRule.setContent {
            AwesomeButton(
                child = "Invalidate",
                width = 200.dp,
                disabled = disabled.value,
                progress = true,
                onProgressStart = { events += "start" },
                onPress = { next ->
                    events += "press"
                    capturedNext = next
                },
                onPressedOut = { events += "pressed-out" },
                onProgressEnd = { events += "end" },
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle { disabled.value = true }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle {
                capturedNext?.invoke { events += "completion" }
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            assertEquals(listOf("start", "press", "pressed-out", "end"), events)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressStartDefersOnPressUntilVisualsMount() {
        var calls = 0
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Upload",
                width = 200.dp,
                progress = true,
                onPress = { next ->
                    calls += 1
                    capturedNext = next
                },
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.runOnIdle {
                assertEquals(0, calls)
            }

            composeRule.mainClock.advanceTimeByFrame()
            composeRule.onAllNodesWithTag("AwesomeButtonSpinner", useUnmergedTree = true).assertCountEquals(1)
            composeRule.runOnIdle {
                assertEquals(1, calls)
                assertNotNull(capturedNext)
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressSwapTransitionsContentOutAndSpinnerInThenRestores() {
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Upload",
                width = 220.dp,
                progress = true,
                onPress = { next -> capturedNext = next },
            )
        }
        composeRule.waitForIdle()
        val startContentWidth = nodeWidth("AwesomeButtonContent")

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(PROGRESS_SWAP_DURATION_MILLIS / 2L)

            val midContentScale = semanticsFloat("AwesomeButtonContent", awesomeButtonContentScaleKey)
            val midSpinnerScale = semanticsFloat("AwesomeButtonSpinner", awesomeButtonActivityScaleKey)
            assertTrue("expected content to scale down during progress", midContentScale < 0.2f)
            assertTrue("expected spinner to scale in during progress", midSpinnerScale > 1f)

            composeRule.runOnIdle {
                capturedNext?.invoke()
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            composeRule.onAllNodesWithTag("AwesomeButtonSpinner", useUnmergedTree = true).assertCountEquals(0)
            assertClose(1f, semanticsFloat("AwesomeButtonContent", awesomeButtonContentScaleKey), tolerance = 0.01f)
            assertClose(startContentWidth, nodeWidth("AwesomeButtonContent"), tolerance = 2f)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressFillCompletesOverSwiftCompletionDuration() {
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Upload",
                width = 220.dp,
                progress = true,
                progressLoadingTimeMillis = 1_000,
                style =
                    AwesomeButtonStyle(
                        backgroundColor = Color(0xFF2563EB),
                        backgroundProgress = Color(0xFFEF4444),
                    ),
                onPress = { next -> capturedNext = next },
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            val initialProgressValue = semanticsFloat("AwesomeButtonProgressFill", awesomeButtonProgressValueKey)
            assertTrue("expected progress fill to start near zero", initialProgressValue < 0.1f)

            composeRule.runOnIdle {
                capturedNext?.invoke()
            }
            composeRule.mainClock.advanceTimeBy(PROGRESS_FILL_COMPLETION_DURATION_MILLIS / 2L)
            val midProgressValue = semanticsFloat("AwesomeButtonProgressFill", awesomeButtonProgressValueKey)
            assertTrue("expected progress fill to advance during completion", midProgressValue > initialProgressValue)

            composeRule.mainClock.advanceTimeBy(PROGRESS_FILL_COMPLETION_DURATION_MILLIS / 2L)
            assertClose(
                1f,
                semanticsFloat("AwesomeButtonProgressFill", awesomeButtonProgressValueKey),
                tolerance = 0.01f,
            )

            composeRule.onAllNodesWithTag("AwesomeButtonProgress", useUnmergedTree = true).assertCountEquals(1)
            composeRule.mainClock.advanceTimeBy(PROGRESS_OVERLAY_FADE_DELAY_MILLIS.toLong())
            composeRule.onAllNodesWithTag("AwesomeButtonProgress", useUnmergedTree = true).assertCountEquals(1)
            composeRule.mainClock.advanceTimeBy(1_000)
            composeRule.onAllNodesWithTag("AwesomeButtonProgress", useUnmergedTree = true).assertCountEquals(0)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun spinnerOnlyProgressOmitsProgressOverlayAndFill() {
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Spinner",
                width = 200.dp,
                progress = true,
                showProgressBar = false,
                onPress = { next -> capturedNext = next },
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()

            composeRule.onAllNodesWithTag("AwesomeButtonSpinner", useUnmergedTree = true).assertCountEquals(1)
            composeRule.onAllNodesWithTag("AwesomeButtonProgress", useUnmergedTree = true).assertCountEquals(0)
            composeRule.onAllNodesWithTag("AwesomeButtonProgressFill", useUnmergedTree = true).assertCountEquals(0)

            composeRule.runOnIdle {
                capturedNext?.invoke()
            }
            composeRule.mainClock.advanceTimeBy(1_200)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressBlocksReentryAndDuplicateNextCalls() {
        var calls = 0
        var completionCalls = 0
        var progressEndCalls = 0
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Busy",
                width = 200.dp,
                progress = true,
                onPress = { next ->
                    calls += 1
                    capturedNext = next
                },
                onProgressEnd = { progressEndCalls += 1 },
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()

            composeRule.runOnIdle {
                assertEquals(1, calls)
                capturedNext?.invoke { completionCalls += 1 }
                capturedNext?.invoke { completionCalls += 1 }
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            composeRule.runOnIdle {
                assertEquals(1, calls)
                assertEquals(1, completionCalls)
                assertEquals(1, progressEndCalls)
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun progressLifecycleCallbacksMatchSwiftOrder() {
        val events = mutableListOf<String>()
        var capturedNext: AwesomeButtonNext? = null

        composeRule.setContent {
            AwesomeButton(
                child = "Progress",
                width = 200.dp,
                progress = true,
                onPressIn = { events.add("onPressIn") },
                onPressOut = { events.add("onPressOut") },
                onProgressStart = { events.add("onProgressStart") },
                onPressedOut = { events.add("onPressedOut") },
                onProgressEnd = { events.add("onProgressEnd") },
                onPress = { next ->
                    events.add("onPress")
                    capturedNext = next
                },
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            tapButton()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle {
                capturedNext?.invoke { events.add("completion") }
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            composeRule.runOnIdle {
                assertContainsInOrder(
                    events,
                    "onPressIn",
                    "onPressOut",
                    "onProgressStart",
                    "onPress",
                    "onPressedOut",
                    "completion",
                    "onProgressEnd",
                )
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }
}
