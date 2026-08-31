package dev.caferati.awesomebutton

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonAccessibilityInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun explicitIdentityAndHintOverrideWithoutFabricatingPressLifecycle() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Visible",
                accessibilityLabel = "Save draft",
                accessibilityHint = "Saves this draft",
                onPress = { events += "press" },
                onPressIn = { events += "in" },
                onPressOut = { events += "out" },
                onPressedIn = { events += "pressed-in" },
                onPressedOut = { events += "pressed-out" },
            )
        }

        val node = composeRule.onNodeWithTag("AwesomeButton")
        node.assertContentDescriptionEquals("Save draft").assertHasClickAction()
        val click = node.fetchSemanticsNode().config[SemanticsActions.OnClick]
        assertEquals("Saves this draft", click.label)
        node.performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        assertEquals(listOf("press"), events)
    }

    @Test
    fun semanticLongPressIsIndependentAndUsesCurrentLocalizedAction() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            AwesomeButton(
                child = "Menu",
                onLongPress = { events += "long" },
                accessibilityLongPressLabel = "Show options",
                onPressIn = { events += "in" },
                onPressOut = { events += "out" },
            )
        }

        val node = composeRule.onNodeWithTag("AwesomeButton")
        node.assertHasNoClickAction()
        val longAction = node.fetchSemanticsNode().config[SemanticsActions.OnLongClick]
        assertEquals("Show options", longAction.label)
        node.performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.waitForIdle()

        assertEquals(listOf("long"), events)
    }

    @Test
    fun disabledAndUnlabeledPlaceholderExposeNoActivation() {
        composeRule.setContent {
            AwesomeButton(onPress = { error("placeholder must not activate") })
        }

        val config = composeRule.onNodeWithTag("AwesomeButton").fetchSemanticsNode().config
        assertFalse(config.contains(SemanticsActions.OnClick))
        assertFalse(config.contains(SemanticsActions.OnLongClick))
        assertTrue(config.contains(SemanticsProperties.HideFromAccessibility))
    }

    @Test
    fun smallVisualGeometryStillOwnsFortyEightDpLayoutFootprint() {
        composeRule.setContent {
            AwesomeButton(
                child = "A",
                width = 20.dp,
                height = 20.dp,
                style = AwesomeButtonStyle(raiseAmount = 0.dp),
                onPress = {},
            )
        }

        val bounds = composeRule.onNodeWithTag("AwesomeButton").getUnclippedBoundsInRoot()
        assertTrue(bounds.right - bounds.left >= 48.dp)
        assertTrue(bounds.bottom - bounds.top >= 48.dp)
    }

    @Test
    fun reducedMotionProgressIsAtomicStaticAndCompletesOnce() {
        val events = mutableListOf<String>()
        var next: AwesomeButtonNext? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalAwesomeButtonReduceMotionOverride provides true) {
                AwesomeButton(
                    child = "Send",
                    progress = true,
                    onPress = {
                        events += "press"
                        next = it
                    },
                    onProgressStart = { events += "start" },
                    onProgressEnd = { events += "end" },
                    onPressIn = { events += "in" },
                    onPressedOut = { events += "pressed-out" },
                )
            }
        }

        composeRule
            .onNodeWithTag("AwesomeButton")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        next?.invoke { events += "completion" }
        composeRule.waitForIdle()

        assertEquals(listOf("start", "press", "completion", "end"), events)
    }

    @Test
    fun themedButtonForwardsCanonicalAccessibilityOptions() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            ThemedButton(
                child = "Visible",
                accessibilityLabel = "Themed action",
                accessibilityHint = "Runs themed action",
                accessibilityLongPressLabel = "Themed options",
                onPress = { events += "press" },
                onLongPress = { events += "long" },
            )
        }

        val node = composeRule.onNodeWithContentDescription("Themed action")
        assertEquals("Runs themed action", node.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        assertEquals("Themed options", node.fetchSemanticsNode().config[SemanticsActions.OnLongClick].label)
        node.performClick()
        node.performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.waitForIdle()

        assertEquals(listOf("press", "long"), events)
    }

    @Test
    fun multipleButtonsRemainIndependentlyAddressableAndActivatable() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            Row {
                AwesomeButton(
                    child = "First",
                    accessibilityLabel = "First action",
                    onPress = { events += "first" },
                )
                AwesomeButton(
                    child = "Second",
                    accessibilityLabel = "Second action",
                    onPress = { events += "second" },
                )
            }
        }

        composeRule.onNodeWithContentDescription("First action").performClick()
        composeRule.onNodeWithContentDescription("Second action").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("first", "second"), events)
    }
}
