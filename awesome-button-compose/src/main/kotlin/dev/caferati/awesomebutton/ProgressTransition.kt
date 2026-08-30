package dev.caferati.awesomebutton

import androidx.compose.animation.core.Easing
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

internal const val PROGRESS_SWAP_DURATION_MILLIS = 300
internal const val PROGRESS_FILL_COMPLETION_DURATION_MILLIS = 120
internal const val PROGRESS_OVERLAY_FADE_DELAY_MILLIS = 120
internal const val PROGRESS_OVERLAY_FADE_DURATION_MILLIS = 160

internal val progressSwapEasing = Easing { progressSwapEasingValue(it) }
internal val progressCompletionEasing = Easing { progressCompletionEasingValue(it) }

internal val awesomeButtonContentScaleKey = SemanticsPropertyKey<Float>("AwesomeButtonContentScale")
internal val awesomeButtonContentAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonContentAlpha")
internal val awesomeButtonActivityScaleKey = SemanticsPropertyKey<Float>("AwesomeButtonActivityScale")
internal val awesomeButtonActivityAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonActivityAlpha")
internal val awesomeButtonProgressValueKey = SemanticsPropertyKey<Float>("AwesomeButtonProgressValue")
internal val awesomeButtonProgressOverlayAlphaKey = SemanticsPropertyKey<Float>("AwesomeButtonProgressOverlayAlpha")

internal var SemanticsPropertyReceiver.awesomeButtonContentScale by awesomeButtonContentScaleKey
internal var SemanticsPropertyReceiver.awesomeButtonContentAlpha by awesomeButtonContentAlphaKey
internal var SemanticsPropertyReceiver.awesomeButtonActivityScale by awesomeButtonActivityScaleKey
internal var SemanticsPropertyReceiver.awesomeButtonActivityAlpha by awesomeButtonActivityAlphaKey
internal var SemanticsPropertyReceiver.awesomeButtonProgressValue by awesomeButtonProgressValueKey
internal var SemanticsPropertyReceiver.awesomeButtonProgressOverlayAlpha by awesomeButtonProgressOverlayAlphaKey

internal fun progressSwapEasingValue(progress: Float): Float {
    val value = progress.coerceIn(0f, 1f).toDouble()
    return (1.0 - cos(value * PI / 2.0).pow(3.0) * cos(value * 1.2 * PI)).toFloat()
}

internal fun progressCompletionEasingValue(progress: Float): Float {
    val value = progress.coerceIn(0f, 1f)
    return 1f - (1f - value).pow(3)
}

internal fun progressOverlayOpacityValue(elapsedMillis: Int): Float {
    if (elapsedMillis <= PROGRESS_OVERLAY_FADE_DELAY_MILLIS) return 1f

    val fadeEnd = PROGRESS_OVERLAY_FADE_DELAY_MILLIS + PROGRESS_OVERLAY_FADE_DURATION_MILLIS
    if (elapsedMillis >= fadeEnd) return 0f

    val localElapsed = elapsedMillis - PROGRESS_OVERLAY_FADE_DELAY_MILLIS
    val progress = localElapsed.toFloat() / PROGRESS_OVERLAY_FADE_DURATION_MILLIS.toFloat()
    return 1f - progressCompletionEasingValue(progress)
}
