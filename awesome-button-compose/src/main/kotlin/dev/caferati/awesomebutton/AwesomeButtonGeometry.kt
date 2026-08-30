package dev.caferati.awesomebutton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val SHADOW_WIDTH_FACTOR = 0.98f

internal data class AwesomeButtonGeometry(
    val faceHeight: Dp,
    val raiseAmount: Dp,
) {
    val totalHeight: Dp = faceHeight + raiseAmount
    val shadowHeight: Dp = maxOf(0.dp, faceHeight - raiseAmount)

    fun shadowTopOffset(pressValue: Float): Dp {
        val geometryPressValue = shellGeometryPressProgress(pressValue)
        return (raiseAmount * 2.5f) - ((raiseAmount / 2f) * geometryPressValue)
    }

    fun faceTopOffset(pressValue: Float): Dp = raiseAmount * shellGeometryPressProgress(pressValue)
}
