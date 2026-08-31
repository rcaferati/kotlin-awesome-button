package dev.caferati.awesomebutton

import kotlin.math.max
import kotlin.math.min

internal const val PLACEHOLDER_LOOP_DURATION_MILLIS = 3223

internal data class PlaceholderShimmerVisibleSegment(
    val leadingXPx: Float,
    val widthPx: Float,
)

internal fun shouldRunPlaceholderAnimation(
    animated: Boolean,
    measuredWidthPx: Float,
): Boolean = animated && measuredWidthPx > 0f

internal fun placeholderLoopPhase(
    elapsedMillis: Long,
    durationMillis: Int = PLACEHOLDER_LOOP_DURATION_MILLIS,
): Float {
    if (durationMillis <= 0) return 0f
    val wrappedElapsed = elapsedMillis.floorMod(durationMillis.toLong())
    return (wrappedElapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
}

internal fun snappedPlaceholderMeasurementWidth(widthPx: Float): Float = max(0f, kotlin.math.round(widthPx))

internal fun placeholderShimmerWidth(widthPx: Float): Float = max(0f, widthPx * 0.4f)

internal fun placeholderShimmerLeadingX(
    phase: Float,
    laneWidthPx: Float,
    bandWidthPx: Float,
): Float {
    if (laneWidthPx <= 0f || bandWidthPx <= 0f) return 0f

    val clampedPhase = phase.coerceIn(0f, 1f)
    val startX = -bandWidthPx
    val centerX = (laneWidthPx - bandWidthPx) / 2f
    val endX = laneWidthPx

    return when {
        clampedPhase <= 0.25f ->
            lerpPlaceholderValue(
                start = startX,
                end = centerX,
                progress = clampedPhase / 0.25f,
            )
        clampedPhase <= 0.5f ->
            lerpPlaceholderValue(
                start = centerX,
                end = endX,
                progress = (clampedPhase - 0.25f) / 0.25f,
            )
        clampedPhase <= 0.75f ->
            lerpPlaceholderValue(
                start = endX,
                end = centerX,
                progress = (clampedPhase - 0.5f) / 0.25f,
            )
        else ->
            lerpPlaceholderValue(
                start = centerX,
                end = startX,
                progress = (clampedPhase - 0.75f) / 0.25f,
            )
    }
}

internal fun placeholderVisibleShimmerSegment(
    phase: Float,
    laneWidthPx: Float,
    bandWidthPx: Float,
): PlaceholderShimmerVisibleSegment {
    if (laneWidthPx <= 0f || bandWidthPx <= 0f) {
        return PlaceholderShimmerVisibleSegment(leadingXPx = 0f, widthPx = 0f)
    }

    val virtualLeft =
        placeholderShimmerLeadingX(
            phase = phase,
            laneWidthPx = laneWidthPx,
            bandWidthPx = bandWidthPx,
        )
    val virtualRight = virtualLeft + bandWidthPx
    val visibleLeft = min(max(0f, virtualLeft), laneWidthPx)
    val visibleRight = min(max(0f, virtualRight), laneWidthPx)
    val visibleWidth = max(0f, visibleRight - visibleLeft)

    return PlaceholderShimmerVisibleSegment(
        leadingXPx = visibleLeft,
        widthPx = visibleWidth,
    )
}

private fun lerpPlaceholderValue(
    start: Float,
    end: Float,
    progress: Float,
): Float {
    val clampedProgress = progress.coerceIn(0f, 1f)
    return start + ((end - start) * clampedProgress)
}

private fun Long.floorMod(other: Long): Long = ((this % other) + other) % other
