package dev.caferati.awesomebutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

@Composable
internal fun AutoWidthButtonLayout(
    modifier: Modifier,
    resolvedWidthPx: Float?,
    height: Dp,
    totalHeight: Dp,
    stretch: Boolean,
    paddingHorizontal: Dp,
    paddingTop: Dp,
    paddingBottom: Dp,
    contentGap: Dp,
    resolvedStyle: AwesomeButtonStyle,
    child: String?,
    before: (@Composable RowScope.() -> Unit)?,
    after: (@Composable RowScope.() -> Unit)?,
    content: (@Composable RowScope.() -> Unit)?,
    contentClipAlignment: ContentClipAlignment,
    shell: @Composable BoxScope.() -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val faceHeightPx = height.roundToPx()
        val totalHeightPx = totalHeight.roundToPx()
        val explicitWidthPx = resolvedWidthPx?.roundToInt()

        val measuredContentWidth =
            if (explicitWidthPx == null && !stretch) {
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                subcompose("measure") {
                    ButtonContent(
                        modifier = Modifier,
                        resolvedStyle = resolvedStyle,
                        disabled = false,
                        alpha = 1f,
                        scale = 1f,
                        paddingHorizontal = paddingHorizontal,
                        paddingTop = paddingTop,
                        paddingBottom = paddingBottom,
                        contentGap = contentGap,
                        child = child,
                        before = before,
                        after = after,
                        content = content,
                        contentClipAlignment = contentClipAlignment,
                    )
                }.maxOfOrNull { it.measure(looseConstraints).width } ?: faceHeightPx
            } else {
                0
            }

        val targetWidthPx =
            when {
                stretch && constraints.maxWidth != Constraints.Infinity -> constraints.maxWidth
                explicitWidthPx != null -> explicitWidthPx
                else -> measuredContentWidth.coerceAtLeast(faceHeightPx)
            }.coerceAtLeast(0)

        val shellPlaceable =
            subcompose("shell") {
                Box(Modifier.requiredSize(targetWidthPx.toDp(), totalHeightPx.toDp()), content = shell)
            }.first().measure(Constraints.fixed(targetWidthPx, totalHeightPx))

        layout(targetWidthPx, totalHeightPx) {
            shellPlaceable.place(0, 0)
        }
    }
}
