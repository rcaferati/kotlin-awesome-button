package dev.caferati.awesomebutton

import androidx.compose.animation.core.Easing
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

internal const val ProgressSwapDurationMillis = 300
internal const val ProgressFillCompletionDurationMillis = 120
internal const val ProgressOverlayFadeDelayMillis = 120
internal const val ProgressOverlayFadeDurationMillis = 160

internal val ProgressSwapEasing = Easing { progressSwapEasingValue(it) }
internal val ProgressCompletionEasing = Easing { progressCompletionEasingValue(it) }

internal val AwesomeButtonContentScaleKey = SemanticsPropertyKey<Float>("AwesomeButtonContentScale")
internal val AwesomeButtonContentAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonContentAlpha")
internal val AwesomeButtonActivityScaleKey = SemanticsPropertyKey<Float>("AwesomeButtonActivityScale")
internal val AwesomeButtonActivityAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonActivityAlpha")
internal val AwesomeButtonProgressValueKey = SemanticsPropertyKey<Float>("AwesomeButtonProgressValue")
internal val AwesomeButtonProgressOverlayAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonProgressOverlayAlpha")

internal var SemanticsPropertyReceiver.awesomeButtonContentScale by AwesomeButtonContentScaleKey
internal var SemanticsPropertyReceiver.awesomeButtonContentAlpha by AwesomeButtonContentAlphaKey
internal var SemanticsPropertyReceiver.awesomeButtonActivityScale by AwesomeButtonActivityScaleKey
internal var SemanticsPropertyReceiver.awesomeButtonActivityAlpha by AwesomeButtonActivityAlphaKey
internal var SemanticsPropertyReceiver.awesomeButtonProgressValue by AwesomeButtonProgressValueKey
internal var SemanticsPropertyReceiver.awesomeButtonProgressOverlayAlpha by AwesomeButtonProgressOverlayAlphaKey

internal fun progressSwapEasingValue(progress: Float): Float {
    val value = progress.coerceIn(0f, 1f).toDouble()
    return (1.0 - cos(value * PI / 2.0).pow(3.0) * cos(value * 1.2 * PI)).toFloat()
}

internal fun progressCompletionEasingValue(progress: Float): Float {
    val value = progress.coerceIn(0f, 1f)
    return 1f - (1f - value).pow(3)
}

internal fun progressOverlayOpacityValue(elapsedMillis: Int): Float {
    if (elapsedMillis <= ProgressOverlayFadeDelayMillis) return 1f

    val fadeEnd = ProgressOverlayFadeDelayMillis + ProgressOverlayFadeDurationMillis
    if (elapsedMillis >= fadeEnd) return 0f

    val localElapsed = elapsedMillis - ProgressOverlayFadeDelayMillis
    val progress = localElapsed.toFloat() / ProgressOverlayFadeDurationMillis.toFloat()
    return 1f - progressCompletionEasingValue(progress)
}
