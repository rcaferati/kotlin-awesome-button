package dev.caferati.awesomebutton

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonPlaceholderInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun placeholderRendersAndOmitsBeforeAfterSlots() {
        composeRule.setContent {
            AwesomeButton(
                width = 200.dp,
                before = { BasicText("BeforeSlot") },
                after = { BasicText("AfterSlot") },
            )
        }

        composeRule.onNodeWithTag("AwesomeButtonPlaceholder", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("BeforeSlot").assertCountEquals(0)
        composeRule.onAllNodesWithText("AfterSlot").assertCountEquals(0)
    }

    @Test
    fun placeholderSuppressesPressAndExposesDisabledSemantics() {
        var presses = 0

        composeRule.setContent {
            AwesomeButton(
                width = 200.dp,
                onPress = { presses += 1 },
            )
        }

        composeRule.onNodeWithTag("AwesomeButton").assertIsNotEnabled().performClick()
        composeRule.waitForIdle()

        assertEquals(0, presses)
    }

    @Test
    fun placeholderUsesResolvedBackgroundPlaceholderColor() {
        val placeholderColor = Color(0xFF334155)

        composeRule.setContent {
            AwesomeButton(
                width = 200.dp,
                animatedPlaceholder = false,
                style = AwesomeButtonStyle(backgroundPlaceholder = placeholderColor),
            )
        }

        val image =
            composeRule
                .onNodeWithTag("AwesomeButtonPlaceholder", useUnmergedTree = true)
                .captureToImage()
        val pixels = image.toPixelMap()
        val sampledColor = pixels[pixels.width / 2, pixels.height / 2]

        assertColorClose(placeholderColor, sampledColor, 0.02f)
    }

    @Test
    fun placeholderShimmerTogglesWithAnimatedPlaceholder() {
        var animated by mutableStateOf(false)

        composeRule.setContent {
            AwesomeButton(
                width = 200.dp,
                animatedPlaceholder = animated,
            )
        }

        composeRule.onAllNodesWithTag("AwesomeButtonPlaceholder", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("AwesomeButtonPlaceholderShimmer", useUnmergedTree = true).assertCountEquals(0)

        composeRule.runOnIdle {
            animated = true
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("AwesomeButtonPlaceholderShimmer", useUnmergedTree = true).assertCountEquals(1)

        composeRule.runOnIdle {
            animated = false
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("AwesomeButtonPlaceholderShimmer", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun placeholderLaneUsesContentWidthAndTextLineHeight() {
        composeRule.setContent {
            AwesomeButton(
                width = 200.dp,
                height = 60.dp,
                paddingHorizontal = 16.dp,
                animatedPlaceholder = false,
                style = AwesomeButtonStyle(textLineHeight = 24.sp),
            )
        }

        val faceBounds =
            composeRule
                .onNodeWithTag("AwesomeButtonFace", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        val placeholderBounds =
            composeRule
                .onNodeWithTag("AwesomeButtonPlaceholder", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        val faceWidth = faceBounds.right.value - faceBounds.left.value
        val placeholderWidth = placeholderBounds.right.value - placeholderBounds.left.value
        val placeholderHeight = placeholderBounds.bottom.value - placeholderBounds.top.value
        val expectedWidth = (faceWidth - 32f) * 0.55f

        assertClose(expectedWidth, placeholderWidth, tolerance = 1f)
        assertClose(24f, placeholderHeight, tolerance = 1f)
    }
}
