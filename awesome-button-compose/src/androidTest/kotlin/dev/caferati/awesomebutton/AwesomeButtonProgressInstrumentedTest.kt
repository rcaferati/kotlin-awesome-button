package dev.caferati.awesomebutton

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
            composeRule.mainClock.advanceTimeBy(ProgressSwapDurationMillis / 2L)

            val midContentScale = semanticsFloat("AwesomeButtonContent", AwesomeButtonContentScaleKey)
            val midSpinnerScale = semanticsFloat("AwesomeButtonSpinner", AwesomeButtonActivityScaleKey)
            assertTrue("expected content to scale down during progress", midContentScale < 0.2f)
            assertTrue("expected spinner to scale in during progress", midSpinnerScale > 1f)

            composeRule.runOnIdle {
                capturedNext?.invoke()
            }
            composeRule.mainClock.advanceTimeBy(1_200)

            composeRule.onAllNodesWithTag("AwesomeButtonSpinner", useUnmergedTree = true).assertCountEquals(0)
            assertClose(1f, semanticsFloat("AwesomeButtonContent", AwesomeButtonContentScaleKey), tolerance = 0.01f)
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
                style = AwesomeButtonStyle(
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
            val initialProgressValue = semanticsFloat("AwesomeButtonProgressFill", AwesomeButtonProgressValueKey)
            assertTrue("expected progress fill to start near zero", initialProgressValue < 0.1f)

            composeRule.runOnIdle {
                capturedNext?.invoke()
            }
            composeRule.mainClock.advanceTimeBy(ProgressFillCompletionDurationMillis / 2L)
            val midProgressValue = semanticsFloat("AwesomeButtonProgressFill", AwesomeButtonProgressValueKey)
            assertTrue("expected progress fill to advance during completion", midProgressValue > initialProgressValue)

            composeRule.mainClock.advanceTimeBy(ProgressFillCompletionDurationMillis / 2L)
            assertClose(1f, semanticsFloat("AwesomeButtonProgressFill", AwesomeButtonProgressValueKey), tolerance = 0.01f)

            composeRule.onAllNodesWithTag("AwesomeButtonProgress", useUnmergedTree = true).assertCountEquals(1)
            composeRule.mainClock.advanceTimeBy(ProgressOverlayFadeDelayMillis.toLong())
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
