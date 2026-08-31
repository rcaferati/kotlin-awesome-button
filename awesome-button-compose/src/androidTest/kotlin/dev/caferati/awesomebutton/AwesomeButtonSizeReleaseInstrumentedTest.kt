package dev.caferati.awesomebutton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonSizeReleaseInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun fixedSizeChangeUsesSwiftSizeCurve() {
        var expanded by mutableStateOf(false)

        composeRule.setContent {
            AwesomeButton(
                child = "Size",
                width = if (expanded) 250.dp else 120.dp,
                height = if (expanded) 60.dp else 44.dp,
            )
        }

        composeRule.waitForIdle()
        val startWidth = buttonImageWidth()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.runOnIdle {
                expanded = true
            }
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(SIZE_ANIMATION_DURATION_MILLIS / 2L)
            val midpointWidth = buttonImageWidth()

            composeRule.mainClock.advanceTimeBy(SIZE_ANIMATION_DURATION_MILLIS.toLong())
            val endWidth = buttonImageWidth()

            assertTrue(
                "expected midpoint width to be between endpoints",
                midpointWidth in (startWidth + 1) until endWidth,
            )
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun releaseSpringAllowsBoundedGeometryOvershoot() {
        composeRule.setContent {
            AwesomeButton(
                child = "Release",
                width = 200.dp,
                style = AwesomeButtonStyle(animationDurationMillis = 0),
                onPress = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.onNodeWithTag("AwesomeButton").performClick()
            composeRule.mainClock.advanceTimeByFrame()

            var minimumGeometryPress = semanticsFloat("AwesomeButtonFace", awesomeButtonGeometryPressProgressKey)
            var minimumVisualPress = semanticsFloat("AwesomeButtonFace", awesomeButtonVisualPressProgressKey)
            repeat(24) {
                composeRule.mainClock.advanceTimeByFrame()
                minimumGeometryPress =
                    minOf(
                        minimumGeometryPress,
                        semanticsFloat("AwesomeButtonFace", awesomeButtonGeometryPressProgressKey),
                    )
                minimumVisualPress =
                    minOf(
                        minimumVisualPress,
                        semanticsFloat("AwesomeButtonFace", awesomeButtonVisualPressProgressKey),
                    )
            }

            assertTrue("expected release geometry to overshoot upward", minimumGeometryPress < -0.01f)
            assertTrue(
                "expected geometry overshoot to stay bounded",
                minimumGeometryPress >= RELEASE_GEOMETRY_PRESS_PROGRESS_FLOOR,
            )
            assertTrue("expected visual press progress to stay clamped", minimumVisualPress >= 0f)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun onPressedOutWaitsForSwiftReleaseSettleWindow() {
        var pressedOutCalls = 0

        composeRule.setContent {
            AwesomeButton(
                child = "Release",
                width = 200.dp,
                style = AwesomeButtonStyle(animationDurationMillis = 0),
                onPressedOut = { pressedOutCalls += 1 },
                onPress = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.onNodeWithTag("AwesomeButton").performClick()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(RELEASE_SPRING_SETTLE_DURATION_MILLIS / 2L)
            composeRule.runOnIdle {
                assertEquals(0, pressedOutCalls)
            }

            composeRule.mainClock.advanceTimeBy(RELEASE_SPRING_SETTLE_DURATION_MILLIS / 2L + 1)
            composeRule.runOnIdle {
                assertEquals(1, pressedOutCalls)
            }
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun autoWidthTextUpdateDefersUntilReleaseSettleCompletes() {
        var label by mutableStateOf("View analytics dashboard")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
                style = autoWidthTestStyle(animationDurationMillis = 0),
                onPress = {},
            )
        }
        composeRule.waitForIdle()
        val startWidth = buttonImageWidth()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.onNodeWithTag("AwesomeButton").performClick()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle {
                label = "Launch"
            }
            composeRule.mainClock.advanceTimeBy(RELEASE_SPRING_SETTLE_DURATION_MILLIS / 2L)

            composeRule.onNodeWithText("View analytics dashboard").assertIsDisplayed()
            assertClose(startWidth.toFloat(), buttonImageWidth().toFloat(), tolerance = 2f)

            composeRule.mainClock.advanceTimeBy(RELEASE_SPRING_SETTLE_DURATION_MILLIS / 2L + 1_200)
            composeRule.onNodeWithText("Launch").assertIsDisplayed()
            assertTrue("expected auto-width button to shrink after release", buttonImageWidth() < startWidth)
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun newPressDuringReleaseInvalidatesDeferredAutoWidthDrain() {
        var label by mutableStateOf("View analytics dashboard")

        composeRule.setContent {
            AwesomeButton(
                child = label,
                textTransition = true,
                textTransitionSlotStaggerMillis = 20,
                style = autoWidthTestStyle(animationDurationMillis = 0),
                onPress = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.onNodeWithTag("AwesomeButton").performClick()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.runOnIdle {
                label = "Launch"
            }
            composeRule.mainClock.advanceTimeBy(80)
            composeRule.onNodeWithTag("AwesomeButton").performClick()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeBy(180)

            composeRule.onNodeWithText("View analytics dashboard").assertIsDisplayed()
            composeRule.onAllNodesWithText("Launch").assertCountEquals(0)

            composeRule.mainClock.advanceTimeBy(1_200)
            composeRule.onNodeWithText("Launch").assertIsDisplayed()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }
}
