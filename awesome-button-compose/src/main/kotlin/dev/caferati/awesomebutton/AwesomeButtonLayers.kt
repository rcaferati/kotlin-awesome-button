package dev.caferati.awesomebutton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val PlaceholderLaneWidthFactor = 0.55f
private const val PlaceholderShimmerAlpha = 0.2f

@Composable
internal fun BoxScope.ButtonLayers(
    geometry: AwesomeButtonGeometry,
    resolvedStyle: AwesomeButtonStyle,
    disabled: Boolean,
    isPlaceholder: Boolean,
    activeOpacity: Float,
    progress: Boolean,
    showProgressBar: Boolean,
    busy: Boolean,
    showProgressVisuals: Boolean,
    pressValue: Float,
    contentAlpha: Float,
    activityAlpha: Float,
    progressOverlayAlpha: Float,
    progressValue: Float,
    paddingHorizontal: Dp,
    paddingTop: Dp,
    paddingBottom: Dp,
    contentGap: Dp,
    child: String?,
    before: (@Composable RowScope.() -> Unit)?,
    after: (@Composable RowScope.() -> Unit)?,
    extra: (@Composable BoxScope.() -> Unit)?,
    animatedPlaceholder: Boolean,
    contentClipAlignment: ContentClipAlignment,
    content: (@Composable RowScope.() -> Unit)?,
) {
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val shape = resolvedStyle.toShape()
    val backgroundColor =
        if (disabled) {
            resolvedStyle.disabledBackgroundColor ?: resolvedStyle.backgroundColor ?: fallback.disabledBackgroundColor!!
        } else {
            resolvedStyle.backgroundColor ?: fallback.backgroundColor!!
        }
    val depthColor =
        if (disabled) {
            resolvedStyle.disabledDepthColor ?: resolvedStyle.depthColor ?: fallback.disabledDepthColor!!
        } else {
            resolvedStyle.depthColor ?: fallback.depthColor!!
        }
    val shadowColor =
        if (disabled) {
            resolvedStyle.disabledShadowColor ?: resolvedStyle.shadowColor ?: fallback.disabledShadowColor!!
        } else {
            resolvedStyle.shadowColor ?: fallback.shadowColor!!
        }
    val borderColor =
        if (disabled) {
            resolvedStyle.disabledBorderColor ?: resolvedStyle.borderColor ?: fallback.disabledBorderColor!!
        } else {
            resolvedStyle.borderColor ?: fallback.borderColor!!
        }
    val pressedFaceColor =
        resolvedStyle.backgroundActive ?: (resolvedStyle.pressedOverlayColor ?: fallback.pressedOverlayColor!!).compositeOver(backgroundColor)
    val visualPressValue = clampedVisualPressProgress(pressValue)
    val geometryPressValue = shellGeometryPressProgress(pressValue)
    val colorPressValue = if (progress && busy && !showProgressBar) 0f else visualPressValue
    val faceColor = lerp(backgroundColor, pressedFaceColor, colorPressValue)
    val borderWidth = resolvedStyle.borderWidth ?: fallback.borderWidth!!
    val density = LocalDensity.current
    val placeholderHeightPx =
        with(density) {
            (resolvedStyle.textLineHeight ?: fallback.textLineHeight!!).toPx()
        }
    val contentOpacity =
        if (progress) {
            contentAlpha.coerceIn(0f, 1f)
        } else {
            1f - ((1f - activeOpacity.coerceIn(0f, 1f)) * visualPressValue)
        }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth(ShadowWidthFactor)
            .height(geometry.shadowHeight)
            .offset { IntOffset(0, geometry.shadowTopOffset(geometryPressValue).roundToPx()) }
            .background(shadowColor, shape)
            .testTag("AwesomeButtonShadow"),
    )

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset { IntOffset(0, geometry.raiseAmount.roundToPx()) }
            .fillMaxWidth()
            .requiredHeight(geometry.faceHeight)
            .background(depthColor, shape)
            .testTag("AwesomeButtonDepth"),
    )

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset { IntOffset(0, geometry.faceTopOffset(geometryPressValue).roundToPx()) }
            .fillMaxWidth()
            .requiredHeight(geometry.faceHeight)
            .clip(shape)
            .background(faceColor, shape)
            .then(if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape) else Modifier)
            .semantics {
                awesomeButtonRawPressProgress = pressValue
                awesomeButtonVisualPressProgress = visualPressValue
                awesomeButtonGeometryPressProgress = geometryPressValue
            }
            .testTag("AwesomeButtonFace"),
    ) {
        if (extra != null) {
            Box(Modifier.matchParentSize(), content = extra)
        }

        if (showProgressVisuals && showProgressBar) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = progressOverlayAlpha.coerceIn(0f, 1f)
                    }
                    .semantics {
                        awesomeButtonProgressOverlayAlpha = progressOverlayAlpha.coerceIn(0f, 1f)
                    }
                    .testTag("AwesomeButtonProgress"),
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = (progressValue.coerceIn(0f, 1f) - 1f) * size.width
                        }
                        .background(resolvedStyle.backgroundProgress ?: fallback.backgroundProgress!!, shape)
                        .semantics {
                            awesomeButtonProgressValue = progressValue.coerceIn(0f, 1f)
                        }
                        .testTag("AwesomeButtonProgressFill"),
                )
            }
        }

        if (isPlaceholder) {
            PlaceholderContent(
                modifier = Modifier.matchParentSize(),
                color = resolvedStyle.backgroundPlaceholder ?: fallback.backgroundPlaceholder!!,
                animated = animatedPlaceholder,
                paddingHorizontal = paddingHorizontal,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                heightPx = placeholderHeightPx,
            )
        } else {
            ButtonContent(
                modifier = Modifier.matchParentSize(),
                resolvedStyle = resolvedStyle,
                disabled = disabled,
                alpha = contentOpacity,
                scale = if (progress) contentAlpha else 1f,
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
        }

        if (showProgressVisuals) {
            LoadingSpinner(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
                    .graphicsLayer {
                        alpha = activityAlpha.coerceIn(0f, 1f)
                        scaleX = activityAlpha
                        scaleY = activityAlpha
                    }
                    .semantics {
                        awesomeButtonActivityAlpha = activityAlpha.coerceIn(0f, 1f)
                        awesomeButtonActivityScale = activityAlpha
                    }
                    .testTag("AwesomeButtonSpinner"),
                color = resolvedStyle.activityColor ?: fallback.activityColor!!,
            )
        }
    }
}

@Composable
internal fun ButtonContent(
    modifier: Modifier,
    resolvedStyle: AwesomeButtonStyle,
    disabled: Boolean,
    alpha: Float,
    scale: Float,
    paddingHorizontal: Dp,
    paddingTop: Dp,
    paddingBottom: Dp,
    contentGap: Dp,
    child: String?,
    before: (@Composable RowScope.() -> Unit)?,
    after: (@Composable RowScope.() -> Unit)?,
    content: (@Composable RowScope.() -> Unit)?,
    contentClipAlignment: ContentClipAlignment = ContentClipAlignment.Center,
) {
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val foregroundColor =
        if (disabled) {
            resolvedStyle.disabledForegroundColor ?: resolvedStyle.foregroundColor ?: fallback.disabledForegroundColor!!
        } else {
            resolvedStyle.foregroundColor ?: fallback.foregroundColor!!
        }
    Row(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha.coerceIn(0f, 1f)
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                awesomeButtonContentAlpha = alpha.coerceIn(0f, 1f)
                awesomeButtonContentScale = scale
            }
            .testTag("AwesomeButtonContent")
            .padding(
                start = paddingHorizontal,
                end = paddingHorizontal,
                top = paddingTop,
                bottom = paddingBottom,
            ),
        horizontalArrangement =
            if (contentClipAlignment == ContentClipAlignment.Leading) {
                Arrangement.Start
            } else {
                Arrangement.Center
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (before != null) {
            before()
            if (child != null || content != null || after != null) {
                Spacer(Modifier.width(contentGap))
            }
        }

        when {
            content != null -> content()
            child != null -> BasicText(
                text = child,
                style = TextStyle(
                    color = foregroundColor,
                    fontSize = resolvedStyle.textSize ?: fallback.textSize!!,
                    lineHeight = resolvedStyle.textLineHeight ?: fallback.textLineHeight!!,
                    fontFamily = resolvedStyle.textFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                softWrap = false,
            )
        }

        if (after != null) {
            if (child != null || content != null) {
                Spacer(Modifier.width(contentGap))
            }
            after()
        }
    }
}

@Composable
private fun LoadingSpinner(
    modifier: Modifier,
    color: Color,
) {
    val transition = rememberInfiniteTransition(label = "awesomeButtonSpinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.14f
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun PlaceholderContent(
    modifier: Modifier,
    color: Color,
    animated: Boolean,
    paddingHorizontal: Dp,
    paddingTop: Dp,
    paddingBottom: Dp,
    heightPx: Float,
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.padding(
            start = paddingHorizontal,
            end = paddingHorizontal,
            top = paddingTop,
            bottom = paddingBottom,
        ),
        contentAlignment = Alignment.Center,
    ) {
        val laneWidthPx =
            with(density) {
                snappedPlaceholderMeasurementWidth(maxWidth.toPx() * PlaceholderLaneWidthFactor)
            }
        val laneHeightPx =
            with(density) {
                heightPx.coerceAtMost(maxHeight.toPx()).coerceAtLeast(0f)
            }
        val canAnimate = shouldRunPlaceholderAnimation(animated, laneWidthPx)

        Box(
            modifier = Modifier
                .requiredSize(
                    width = with(density) { laneWidthPx.toDp() },
                    height = with(density) { laneHeightPx.toDp() },
                )
                .clipToBounds()
                .background(color)
                .testTag("AwesomeButtonPlaceholder"),
        ) {
            if (canAnimate) {
                val transition = rememberInfiniteTransition(label = "awesomeButtonPlaceholder")
                val phase by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = PlaceholderLoopDurationMillis, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "placeholderPhase",
                )
                val bandWidthPx = placeholderShimmerWidth(laneWidthPx)
                val segment =
                    placeholderVisibleShimmerSegment(
                        phase = phase,
                        laneWidthPx = laneWidthPx,
                        bandWidthPx = bandWidthPx,
                    )

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = segment.leadingXPx.roundToInt(),
                                y = 0,
                            )
                        }
                        .requiredSize(
                            width = with(density) { segment.widthPx.toDp() },
                            height = with(density) { laneHeightPx.toDp() },
                        )
                        .background(Color.Black.copy(alpha = PlaceholderShimmerAlpha))
                        .testTag("AwesomeButtonPlaceholderShimmer"),
                )
            }
        }
    }
}

private fun AwesomeButtonStyle.toShape(): RoundedCornerShape {
    val fallbackRadius = AwesomeButtonThemeData.fallbackStyle.borderRadius!!
    val radii = cornerRadii
    return if (radii != null) {
        RoundedCornerShape(
            topStart = radii.topStart,
            topEnd = radii.topEnd,
            bottomEnd = radii.bottomEnd,
            bottomStart = radii.bottomStart,
        )
    } else {
        RoundedCornerShape(borderRadius ?: fallbackRadius)
    }
}
