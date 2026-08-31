package dev.caferati.awesomebutton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonTextTransitionInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun themedSizeStyleFramesDoNotFreezeTheVisibleTextTransition() {
        var size by mutableStateOf(ButtonSize.Medium)

        composeRule.setContent {
            ThemedButton(
                child =
                    when (size) {
                        ButtonSize.Small -> "Small"
                        ButtonSize.Medium -> "Medium"
                        ButtonSize.Large -> "Large"
                        ButtonSize.Icon -> "Icon"
                    },
                name = ThemeName.Bruce,
                type = ButtonVariant.Danger,
                size = size,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                size = ButtonSize.Large
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_000)
            composeRule.onNodeWithText("Large").assertIsDisplayed()

            composeRule.runOnIdle {
                size = ButtonSize.Small
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(1_000)
            composeRule.onNodeWithText("Small").assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun fixedWidthTextTransitionShowsIntermediateTextBeforeTarget() {
        var label by mutableStateOf("welcome")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                width = 220.dp,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
            )
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("welcome").assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                label = "Level 2"
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(40)
            composeRule.onAllNodesWithText("welcome").assertCountEquals(0)
            composeRule.onAllNodesWithText("Level 2").assertCountEquals(0)

            composeRule.mainClock.advanceTimeBy(500)
            composeRule.onNodeWithText("Level 2").assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun disabledTextTransitionSnapsImmediately() {
        var label by mutableStateOf("welcome")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                width = 220.dp,
                textTransition = false,
            )
        }

        composeRule.runOnIdle {
            label = "Level 2"
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Level 2").assertIsDisplayed()
    }

    @Test
    fun secondTextTransitionCancelsFirstAndUsesLatestTarget() {
        var label by mutableStateOf("welcome")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                width = 220.dp,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
            )
        }

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                label = "Level 2"
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(40)
            composeRule.runOnIdle {
                label = "Go#3"
            }
            composeRule.mainClock.advanceTimeBy(600)

            composeRule.onNodeWithText("Go#3").assertIsDisplayed()
            composeRule.onAllNodesWithText("Level 2").assertCountEquals(0)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun autoWidthTextTransitionEndsAtExpandedTarget() {
        var label by mutableStateOf("Go")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                style = autoWidthTestStyle(),
            )
        }

        val startWidth = composeRule.onNodeWithTag("AwesomeButton").captureToImage().width
        composeRule.runOnIdle {
            label = "Mission#42"
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithText("Mission#42").fetchSemanticsNodes().isNotEmpty()
        }
        val endWidth = composeRule.onNodeWithTag("AwesomeButton").captureToImage().width

        assertTrue("expected auto-width button to expand", endWidth > startWidth)
        composeRule.onNodeWithText("Mission#42").assertIsDisplayed()
    }

    @Test
    fun autoWidthGrowStartsWidthBeforeTextTransition() {
        var label by mutableStateOf("Launch")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
                style = autoWidthTestStyle(),
            )
        }

        composeRule.waitForIdle()
        val startWidth = buttonImageWidth()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                label = "View analytics dashboard"
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(120)

            val preTextWidth = buttonImageWidth()
            assertTrue("expected width to begin growing before text transition", preTextWidth > startWidth)
            composeRule.onNodeWithText("Launch").assertIsDisplayed()

            composeRule.mainClock.advanceTimeBy(1_200)
            composeRule.onNodeWithText("View analytics dashboard").assertIsDisplayed()
            assertTrue("expected final width to remain expanded", buttonImageWidth() > preTextWidth)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun autoWidthShrinkDelaysWidthUntilAfterTextStarts() {
        var label by mutableStateOf("View analytics dashboard")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
                style = autoWidthTestStyle(),
            )
        }

        composeRule.waitForIdle()
        val startWidth = buttonImageWidth()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                label = "Launch"
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(120)
            val preShrinkWidth = buttonImageWidth()

            assertClose(startWidth.toFloat(), preShrinkWidth.toFloat(), tolerance = 2f)

            composeRule.mainClock.advanceTimeBy(260)
            val shrinkingWidth = buttonImageWidth()
            assertTrue("expected width to shrink after delay", shrinkingWidth < preShrinkWidth)

            composeRule.mainClock.advanceTimeBy(1_200)
            composeRule.onNodeWithText("Launch").assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun autoWidthShrinkRecentersTargetTextBeforeWidthAnimationFinishes() {
        var label by mutableStateOf("View analytics dashboard")
        val source = "View analytics dashboard"
        val target = "Launch"
        val slotStaggerMillis = 20
        val transitionDuration =
            getTextTransitionTimeline(
                fromText = source,
                targetText = target,
                slotStaggerMillis = slotStaggerMillis,
            ).totalDurationMillis
        val widthDelay =
            resolveAutoWidthTextTransitionTiming(
                fromText = source,
                targetText = target,
                flow = AutoWidthTextFlow.ShrinkLast,
                slotStaggerMillis = slotStaggerMillis,
            ).widthDelayMillis
        val sampleMillis = transitionDuration + 32
        val widthCompletionMillis = widthDelay + transitionDuration

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                textTransitionSlotStaggerMillis = slotStaggerMillis,
                style = autoWidthTestStyle(),
            )
        }

        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                label = target
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(sampleMillis.toLong())

            composeRule.onNodeWithText(target).assertIsDisplayed()
            assertTrue(
                "expected target text to become visible before shrink animation completes",
                sampleMillis < widthCompletionMillis,
            )
            assertClose(
                nodeCenterX("AwesomeButtonFace"),
                textCenterX(target),
                tolerance = 1.5f,
            )
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }
}
