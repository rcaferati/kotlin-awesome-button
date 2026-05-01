package dev.caferati.awesomebutton

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.math.sqrt

internal const val ReleaseSpringStiffness = 280f
internal const val ReleaseSpringDamping = 20f
internal val ReleaseSpringDampingRatio: Float =
    ReleaseSpringDamping / (2f * sqrt(ReleaseSpringStiffness))
internal const val ReleaseSpringSettleDurationMillis = 240
internal const val ReleaseGeometryPressProgressFloor = -0.25f

internal val AwesomeButtonRawPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonRawPressProgress")
internal val AwesomeButtonVisualPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonVisualPressProgress")
internal val AwesomeButtonGeometryPressProgressKey =
    SemanticsPropertyKey<Float>("AwesomeButtonGeometryPressProgress")

internal var SemanticsPropertyReceiver.awesomeButtonRawPressProgress by AwesomeButtonRawPressProgressKey
internal var SemanticsPropertyReceiver.awesomeButtonVisualPressProgress by AwesomeButtonVisualPressProgressKey
internal var SemanticsPropertyReceiver.awesomeButtonGeometryPressProgress by AwesomeButtonGeometryPressProgressKey

internal fun clampedVisualPressProgress(progress: Float): Float =
    progress.coerceIn(0f, 1f)

internal fun shellGeometryPressProgress(progress: Float): Float =
    progress.coerceIn(ReleaseGeometryPressProgressFloor, 1f)
