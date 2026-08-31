package dev.caferati.awesomebutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

private enum class ButtonLayoutChild {
    Chrome,
    Content,
    Activity,
}

/**
 * Measures and places the same content composition that is rendered.
 *
 * Consumer content is never duplicated for intrinsic measurement. In auto-width mode its single
 * measurable is measured loosely once, that placeable determines the shell width, and the same
 * placeable is centered in the face. Fixed and stretch modes measure it once at the resolved width.
 */
@Composable
internal fun AutoWidthButtonLayout(
    modifier: Modifier,
    resolvedWidthPx: Float?,
    minimumFaceHeight: Dp,
    raiseAmount: Dp,
    pressValue: Float,
    stretch: Boolean,
    contentClipShape: Shape,
    chrome: @Composable BoxScope.(AwesomeButtonGeometry) -> Unit,
    content: (@Composable BoxScope.() -> Unit)?,
    activity: (@Composable BoxScope.() -> Unit)?,
) {
    SubcomposeLayout(
        modifier = modifier,
    ) { constraints ->
        val minimumFaceHeightPx = minimumFaceHeight.roundToPx()
        val raiseAmountPx = raiseAmount.roundToPx()
        val explicitWidthPx = resolvedWidthPx?.roundToInt()
        val maximumContentWidth =
            when {
                stretch && constraints.maxWidth != Constraints.Infinity -> constraints.maxWidth
                explicitWidthPx != null -> explicitWidthPx.coerceAtMost(constraints.maxWidth)
                else -> constraints.maxWidth
            }
        val contentMeasurable =
            content?.let {
                subcompose(ButtonLayoutChild.Content) {
                    Box(
                        modifier = Modifier.clip(contentClipShape),
                        // Fixed and stretch widths must reach ButtonContent so its centered/leading
                        // arrangement owns the label position. Auto width still arrives with a zero
                        // minimum and therefore measures the same content at its intrinsic width.
                        propagateMinConstraints = true,
                        content = it,
                    )
                }.single()
            }

        val autoContentPlaceable =
            if (explicitWidthPx == null && !stretch && contentMeasurable != null) {
                contentMeasurable.measure(
                    Constraints(
                        minWidth = 0,
                        maxWidth = maximumContentWidth,
                        minHeight = 0,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            } else {
                null
            }

        val targetWidthPx =
            when {
                stretch && constraints.maxWidth != Constraints.Infinity -> constraints.maxWidth
                explicitWidthPx != null -> explicitWidthPx
                else -> (autoContentPlaceable?.width ?: minimumFaceHeightPx).coerceAtLeast(minimumFaceHeightPx)
            }.coerceIn(0, constraints.maxWidth)

        val contentPlaceable =
            autoContentPlaceable
                ?: contentMeasurable?.measure(
                    Constraints(
                        minWidth = targetWidthPx,
                        maxWidth = targetWidthPx,
                        minHeight = 0,
                        maxHeight = constraints.maxHeight,
                    ),
                )
        val faceHeightPx = maxOf(minimumFaceHeightPx, contentPlaceable?.height ?: 0)
        val totalHeightPx = faceHeightPx + raiseAmountPx
        val geometry = AwesomeButtonGeometry(faceHeightPx.toDp(), raiseAmountPx.toDp())
        val faceTopOffsetPx = geometry.faceTopOffset(pressValue).roundToPx()

        val chromeMeasurable =
            subcompose(ButtonLayoutChild.Chrome) {
                // The shell is fixed-size, but each chrome layer owns its own height. Forwarding the
                // shell minimum would inflate the shorter shadow layer beyond its geometry bounds.
                Box(propagateMinConstraints = false) { chrome(geometry) }
            }.single()
        val activityMeasurable =
            activity?.let {
                subcompose(ButtonLayoutChild.Activity) {
                    Box(
                        modifier = Modifier.clip(contentClipShape),
                        // The wrapper fills the moving face so BoxScope alignment can center the
                        // indicator. Its fixed minimum must not replace the indicator's own size.
                        propagateMinConstraints = false,
                        content = it,
                    )
                }.single()
            }

        val chromePlaceable =
            chromeMeasurable.measure(Constraints.fixed(targetWidthPx, totalHeightPx))
        val activityPlaceable =
            activityMeasurable?.measure(Constraints.fixed(targetWidthPx, faceHeightPx))

        val layoutWidth = targetWidthPx.coerceIn(constraints.minWidth, constraints.maxWidth)
        val layoutHeight = totalHeightPx.coerceIn(constraints.minHeight, constraints.maxHeight)
        val visualX = (layoutWidth - targetWidthPx) / 2
        val visualY = (layoutHeight - totalHeightPx) / 2

        layout(layoutWidth, layoutHeight) {
            chromePlaceable.place(visualX, visualY)
            contentPlaceable?.place(
                x = visualX + ((targetWidthPx - contentPlaceable.width) / 2),
                y =
                    visualY +
                        ((faceHeightPx - contentPlaceable.height) / 2) +
                        faceTopOffsetPx,
            )
            activityPlaceable?.place(visualX, visualY + faceTopOffsetPx)
        }
    }
}
