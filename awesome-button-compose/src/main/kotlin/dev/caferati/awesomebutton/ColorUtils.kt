package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal fun blendColors(
    percentage: Double,
    startColor: Color,
    endColor: Color? = null,
    linear: Boolean = false,
): Color {
    require(percentage in -1.0..1.0) { "percentage must be between -1 and 1" }

    val target = endColor ?: if (percentage < 0) Color.Black else Color.White
    val ratio = kotlin.math.abs(percentage)
    val inverse = 1.0 - ratio

    fun mix(start: Float, end: Float): Float {
        val startValue = (start * 255f).roundToInt().coerceIn(0, 255)
        val endValue = (end * 255f).roundToInt().coerceIn(0, 255)
        val mixed =
            if (linear) {
                inverse * startValue + ratio * endValue
            } else {
                sqrt(inverse * startValue * startValue + ratio * endValue * endValue)
            }
        return mixed.roundToInt().coerceIn(0, 255) / 255f
    }

    fun mixAlpha(start: Float, end: Float): Float {
        val startValue = (start * 255f).roundToInt().coerceIn(0, 255)
        val endValue = (end * 255f).roundToInt().coerceIn(0, 255)
        if (startValue == 255 && endValue == 255) {
            return 1f
        }
        return ((inverse * startValue + ratio * endValue).roundToInt().coerceIn(0, 255) / 255f)
    }

    return Color(
        red = mix(startColor.red, target.red),
        green = mix(startColor.green, target.green),
        blue = mix(startColor.blue, target.blue),
        alpha = mixAlpha(startColor.alpha, target.alpha),
    )
}

internal fun Color?.orTransparent(): Color = this ?: Color.Transparent
