package dev.caferati.awesomebutton

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.math.sqrt

internal const val RELEASE_SPRING_STIFFNESS = 280f
internal const val RELEASE_SPRING_DAMPING = 20f
internal val releaseSpringDampingRatio: Float =
    RELEASE_SPRING_DAMPING / (2f * sqrt(RELEASE_SPRING_STIFFNESS))
internal const val RELEASE_SPRING_SETTLE_DURATION_MILLIS = 240
internal const val RELEASE_GEOMETRY_PRESS_PROGRESS_FLOOR = -0.25f

internal val awesomeButtonRawPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonRawPressProgress")
internal val awesomeButtonVisualPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonVisualPressProgress")
internal val awesomeButtonGeometryPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonGeometryPressProgress")

internal var SemanticsPropertyReceiver.awesomeButtonRawPressProgress by awesomeButtonRawPressProgressKey
internal var SemanticsPropertyReceiver.awesomeButtonVisualPressProgress by awesomeButtonVisualPressProgressKey
internal var SemanticsPropertyReceiver.awesomeButtonGeometryPressProgress by awesomeButtonGeometryPressProgressKey

internal fun clampedVisualPressProgress(progress: Float): Float = progress.coerceIn(0f, 1f)

internal fun shellGeometryPressProgress(progress: Float): Float =
    progress.coerceIn(RELEASE_GEOMETRY_PRESS_PROGRESS_FLOOR, 1f)
