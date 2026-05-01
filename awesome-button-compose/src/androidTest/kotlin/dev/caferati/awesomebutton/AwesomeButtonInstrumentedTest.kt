package dev.caferati.awesomebutton

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun successfulClickDispatchesOnce() {
        var presses = 0
        composeRule.setContent {
            AwesomeButton(child = "Save", onPress = { presses += 1 })
        }

        composeRule.onNodeWithTag("AwesomeButton").assertHasClickAction().performClick()
        composeRule.waitForIdle()

        assertEquals(1, presses)
    }

    @Test
    fun longHoldWithoutLongPressHandlerDispatchesNormalPress() {
        var presses = 0
        composeRule.setContent {
            AwesomeButton(child = "Hold", onPress = { presses += 1 })
        }

        composeRule.onNodeWithTag("AwesomeButton").performTouchInput {
            down(center)
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()

        assertEquals(1, presses)
    }

    @Test
    fun disabledButtonDoesNotDispatch() {
        var presses = 0
        composeRule.setContent {
            AwesomeButton(child = "Save", disabled = true, onPress = { presses += 1 })
        }

        composeRule.onNodeWithTag("AwesomeButton").performClick()
        composeRule.waitForIdle()

        assertEquals(0, presses)
    }
}
