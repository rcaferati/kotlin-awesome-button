package dev.caferati.awesomebutton

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertTrue
import org.junit.Rule
import kotlin.math.sqrt

abstract class AwesomeButtonInstrumentedTestBase {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    protected fun sampleFaceColor(): Color {
        val image = composeRule.onNodeWithTag("AwesomeButton").captureToImage()
        val pixels = image.toPixelMap()
        return pixels[pixels.width / 4, pixels.height / 3]
    }

    protected fun tapButton() {
        composeRule.onNodeWithTag("AwesomeButton").performTouchInput {
            down(center)
            up()
        }
    }

    protected fun nodeWidth(tag: String): Float {
        val bounds = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        return bounds.right.value - bounds.left.value
    }

    protected fun nodeCenterX(tag: String): Float {
        val bounds = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        return (bounds.left.value + bounds.right.value) / 2f
    }

    protected fun textCenterX(text: String): Float {
        val bounds = composeRule.onNodeWithText(text, useUnmergedTree = true).getUnclippedBoundsInRoot()
        return (bounds.left.value + bounds.right.value) / 2f
    }

    protected fun semanticsFloat(
        tag: String,
        key: SemanticsPropertyKey<Float>,
    ): Float =
        composeRule
            .onNodeWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[key]

    protected fun buttonImageWidth(): Int =
        composeRule.onNodeWithTag("AwesomeButton").captureToImage().width

    protected fun autoWidthTestStyle(animationDurationMillis: Int? = null): AwesomeButtonStyle =
        AwesomeButtonStyle(
            textSize = 14.sp,
            textLineHeight = 20.sp,
            animationDurationMillis = animationDurationMillis,
        )

    protected fun assertColorClose(
        expected: Color,
        actual: Color,
        tolerance: Float,
    ) {
        assertTrue(
            "expected $expected, was $actual",
            colorDistance(expected, actual) <= tolerance,
        )
    }

    protected fun assertColorDistanceGreaterThan(
        first: Color,
        second: Color,
        minimum: Float,
    ) {
        assertTrue(
            "expected $first and $second to differ by more than $minimum",
            colorDistance(first, second) > minimum,
        )
    }

    protected fun assertClose(
        expected: Float,
        actual: Float,
        tolerance: Float,
    ) {
        assertTrue(
            "expected $actual to be within $tolerance of $expected",
            kotlin.math.abs(expected - actual) <= tolerance,
        )
    }

    protected fun assertContainsInOrder(
        values: List<String>,
        vararg expectedValues: String,
    ) {
        var searchStart = 0
        for (expected in expectedValues) {
            val foundIndex = values.subList(searchStart, values.size).indexOf(expected)
            assertTrue("expected $expected after index $searchStart in $values", foundIndex >= 0)
            searchStart += foundIndex + 1
        }
    }

    private fun colorDistance(
        first: Color,
        second: Color,
    ): Float {
        val red = first.red - second.red
        val green = first.green - second.green
        val blue = first.blue - second.blue
        val alpha = first.alpha - second.alpha
        return sqrt(red * red + green * green + blue * blue + alpha * alpha)
    }
}
