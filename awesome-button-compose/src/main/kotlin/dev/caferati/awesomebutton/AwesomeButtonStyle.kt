package dev.caferati.awesomebutton

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.unit.lerp as lerpUnit

/**
 * Per-corner radius configuration for the shell, face, and shadow layers.
 *
 * Physical start/end values follow the current Compose layout direction.
 *
 * @property topStart top-start radius in density-independent pixels.
 * @property topEnd top-end radius in density-independent pixels.
 * @property bottomEnd bottom-end radius in density-independent pixels.
 * @property bottomStart bottom-start radius in density-independent pixels.
 */
public data class AwesomeButtonCornerRadii(
    public val topStart: Dp,
    public val topEnd: Dp,
    public val bottomEnd: Dp,
    public val bottomStart: Dp,
) {
    /** Factories for common corner configurations. */
    public companion object {
        /** Returns a configuration that applies [radius] to all four corners. */
        public fun all(radius: Dp): AwesomeButtonCornerRadii =
            AwesomeButtonCornerRadii(
                topStart = radius,
                topEnd = radius,
                bottomEnd = radius,
                bottomStart = radius,
            )
    }
}

/** Timing curves available to button press and resolved-style transitions. */
public enum class AwesomeButtonAnimationCurve {
    EaseOutCubic,
    EaseOut,
    Linear,
}

internal fun AwesomeButtonAnimationCurve.toEasing(): Easing =
    when (this) {
        AwesomeButtonAnimationCurve.EaseOutCubic -> CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
        AwesomeButtonAnimationCurve.EaseOut -> CubicBezierEasing(0f, 0f, 0.58f, 1f)
        AwesomeButtonAnimationCurve.Linear -> LinearEasing
    }

/**
 * Visual overrides for [AwesomeButton]; null fields defer to the active theme.
 *
 * Invalid numeric inputs are normalized by the button: negative dimensions and durations become
 * zero, and non-finite optional dimensions are treated as absent.
 *
 * @property backgroundColor idle face color.
 * @property backgroundActive pressed face color.
 * @property backgroundPlaceholder placeholder face color.
 * @property backgroundProgress progress-layer color.
 * @property depthColor color of the visible lower depth layer.
 * @property shadowColor outer shadow color.
 * @property activityColor progress-indicator color.
 * @property pressedOverlayColor overlay blended into the pressed face when needed.
 * @property foregroundColor foreground color for the built-in string label.
 * @property textSize label size as a Compose text unit.
 * @property textLineHeight label line height as a Compose text unit.
 * @property textFontFamily label font family.
 * @property borderRadius fallback radius for every corner in density-independent pixels.
 * @property cornerRadii physical per-corner overrides.
 * @property borderWidth face border width in density-independent pixels.
 * @property borderColor face border color.
 * @property raiseAmount visible depth height in density-independent pixels.
 * @property contentGap space between auxiliary slots and primary content.
 * @property animationDurationMillis general press/style timing in milliseconds.
 * @property animationCurve timing curve for timing-based visual transitions.
 * @property disabledBackgroundColor disabled face override.
 * @property disabledDepthColor disabled depth-layer override.
 * @property disabledShadowColor disabled shadow override.
 * @property disabledForegroundColor disabled foreground override.
 * @property disabledBorderColor disabled border override.
 */
public data class AwesomeButtonStyle(
    public val backgroundColor: Color? = null,
    public val backgroundActive: Color? = null,
    public val backgroundPlaceholder: Color? = null,
    public val backgroundProgress: Color? = null,
    public val depthColor: Color? = null,
    public val shadowColor: Color? = null,
    public val activityColor: Color? = null,
    public val pressedOverlayColor: Color? = null,
    public val foregroundColor: Color? = null,
    public val textSize: TextUnit? = null,
    public val textLineHeight: TextUnit? = null,
    public val textFontFamily: FontFamily? = null,
    public val borderRadius: Dp? = null,
    public val cornerRadii: AwesomeButtonCornerRadii? = null,
    public val borderWidth: Dp? = null,
    public val borderColor: Color? = null,
    public val raiseAmount: Dp? = null,
    public val contentGap: Dp? = null,
    public val animationDurationMillis: Int? = null,
    public val animationCurve: AwesomeButtonAnimationCurve? = null,
    public val disabledBackgroundColor: Color? = null,
    public val disabledDepthColor: Color? = null,
    public val disabledShadowColor: Color? = null,
    public val disabledForegroundColor: Color? = null,
    public val disabledBorderColor: Color? = null,
) {
    /** Returns this style with every non-null field from [other] applied. */
    public fun merge(other: AwesomeButtonStyle?): AwesomeButtonStyle {
        if (other == null) return this

        return AwesomeButtonStyle(
            backgroundColor = other.backgroundColor ?: backgroundColor,
            backgroundActive = other.backgroundActive ?: backgroundActive,
            backgroundPlaceholder = other.backgroundPlaceholder ?: backgroundPlaceholder,
            backgroundProgress = other.backgroundProgress ?: backgroundProgress,
            depthColor = other.depthColor ?: depthColor,
            shadowColor = other.shadowColor ?: shadowColor,
            activityColor = other.activityColor ?: activityColor,
            pressedOverlayColor = other.pressedOverlayColor ?: pressedOverlayColor,
            foregroundColor = other.foregroundColor ?: foregroundColor,
            textSize = normalizeOptionalTextUnit(other.textSize) ?: textSize,
            textLineHeight = normalizeOptionalTextUnit(other.textLineHeight) ?: textLineHeight,
            textFontFamily = other.textFontFamily ?: textFontFamily,
            borderRadius = normalizeOptionalDp(other.borderRadius) ?: borderRadius,
            cornerRadii = normalizeCornerRadii(other.cornerRadii) ?: cornerRadii,
            borderWidth = normalizeOptionalDp(other.borderWidth) ?: borderWidth,
            borderColor = other.borderColor ?: borderColor,
            raiseAmount = normalizeOptionalDp(other.raiseAmount) ?: raiseAmount,
            contentGap = normalizeOptionalDp(other.contentGap) ?: contentGap,
            animationDurationMillis =
                normalizeOptionalMillis(other.animationDurationMillis) ?: animationDurationMillis,
            animationCurve = other.animationCurve ?: animationCurve,
            disabledBackgroundColor = other.disabledBackgroundColor ?: disabledBackgroundColor,
            disabledDepthColor = other.disabledDepthColor ?: disabledDepthColor,
            disabledShadowColor = other.disabledShadowColor ?: disabledShadowColor,
            disabledForegroundColor = other.disabledForegroundColor ?: disabledForegroundColor,
            disabledBorderColor = other.disabledBorderColor ?: disabledBorderColor,
        )
    }
}

/**
 * Theme data resolved through [AwesomeButtonTheme].
 *
 * @property style base visual values inherited by buttons in the provider.
 */
public data class AwesomeButtonThemeData(
    public val style: AwesomeButtonStyle,
) {
    /** Returns theme data whose base style includes non-null values from [style]. */
    public fun merge(style: AwesomeButtonStyle?): AwesomeButtonThemeData =
        AwesomeButtonThemeData(this.style.merge(style))

    /** Stable fallback values used when neither a theme nor call-site override supplies a field. */
    public companion object {
        /** Complete fallback style, including the 140 ms general animation duration. */
        public val fallbackStyle: AwesomeButtonStyle =
            AwesomeButtonStyle(
                backgroundColor = Color(0xFF2563EB),
                depthColor = Color(0xFF1D4ED8),
                shadowColor = Color.Black.copy(alpha = 0.15f),
                backgroundPlaceholder = Color.Black.copy(alpha = 0.15f),
                backgroundProgress = Color.Black.copy(alpha = 0.15f),
                pressedOverlayColor = Color.Black.copy(alpha = 0.08f),
                foregroundColor = Color.White,
                activityColor = Color.White,
                textSize = 14.sp,
                textLineHeight = 20.sp,
                borderRadius = 18.dp,
                borderWidth = 0.dp,
                borderColor = Color.Transparent,
                raiseAmount = 6.dp,
                contentGap = 10.dp,
                animationDurationMillis = 140,
                animationCurve = AwesomeButtonAnimationCurve.EaseOutCubic,
                disabledBackgroundColor = Color(0xFFB8C6DB),
                disabledDepthColor = Color(0xFF98A9C2),
                disabledShadowColor = Color.Black.copy(alpha = 0.10f),
                disabledForegroundColor = Color(0xFFF8FAFC),
                disabledBorderColor = Color.Transparent,
            )

        /** Theme data wrapping [fallbackStyle]. */
        public val fallback: AwesomeButtonThemeData = AwesomeButtonThemeData(fallbackStyle)
    }
}

internal fun resolvedVisualStyle(style: AwesomeButtonStyle): AwesomeButtonStyle {
    val normalizedStyle = normalizeStyleInput(style)
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val borderRadius = normalizedStyle.borderRadius ?: fallback.borderRadius!!
    return AwesomeButtonStyle(
        backgroundColor = normalizedStyle.backgroundColor ?: fallback.backgroundColor,
        backgroundActive = normalizedStyle.backgroundActive ?: fallback.backgroundActive,
        backgroundPlaceholder = normalizedStyle.backgroundPlaceholder ?: fallback.backgroundPlaceholder,
        backgroundProgress = normalizedStyle.backgroundProgress ?: fallback.backgroundProgress,
        depthColor = normalizedStyle.depthColor ?: fallback.depthColor,
        shadowColor = normalizedStyle.shadowColor ?: fallback.shadowColor,
        activityColor = normalizedStyle.activityColor ?: fallback.activityColor,
        pressedOverlayColor = normalizedStyle.pressedOverlayColor ?: fallback.pressedOverlayColor,
        foregroundColor = normalizedStyle.foregroundColor ?: fallback.foregroundColor,
        textSize = normalizedStyle.textSize ?: fallback.textSize,
        textLineHeight = normalizedStyle.textLineHeight ?: fallback.textLineHeight,
        textFontFamily = normalizedStyle.textFontFamily ?: fallback.textFontFamily,
        borderRadius = borderRadius,
        cornerRadii = normalizedStyle.cornerRadii ?: AwesomeButtonCornerRadii.all(borderRadius),
        borderWidth = normalizedStyle.borderWidth ?: fallback.borderWidth,
        borderColor = normalizedStyle.borderColor ?: fallback.borderColor,
        raiseAmount = normalizedStyle.raiseAmount ?: fallback.raiseAmount,
        contentGap = normalizedStyle.contentGap ?: fallback.contentGap,
        animationDurationMillis = normalizedStyle.animationDurationMillis ?: fallback.animationDurationMillis,
        animationCurve = normalizedStyle.animationCurve ?: fallback.animationCurve,
        disabledBackgroundColor = normalizedStyle.disabledBackgroundColor,
        disabledDepthColor = normalizedStyle.disabledDepthColor,
        disabledShadowColor = normalizedStyle.disabledShadowColor,
        disabledForegroundColor = normalizedStyle.disabledForegroundColor,
        disabledBorderColor = normalizedStyle.disabledBorderColor,
    )
}

internal fun interpolateAwesomeButtonStyle(
    from: AwesomeButtonStyle,
    to: AwesomeButtonStyle,
    fraction: Float,
): AwesomeButtonStyle {
    val start = resolvedVisualStyle(from)
    val end = resolvedVisualStyle(to)
    val progress = fraction.coerceIn(0f, 1f)

    return AwesomeButtonStyle(
        backgroundColor = lerpNullableColor(start.backgroundColor, end.backgroundColor, progress),
        backgroundActive = lerpNullableColor(start.backgroundActive, end.backgroundActive, progress),
        backgroundPlaceholder = lerpNullableColor(start.backgroundPlaceholder, end.backgroundPlaceholder, progress),
        backgroundProgress = lerpNullableColor(start.backgroundProgress, end.backgroundProgress, progress),
        depthColor = lerpNullableColor(start.depthColor, end.depthColor, progress),
        shadowColor = lerpNullableColor(start.shadowColor, end.shadowColor, progress),
        activityColor = lerpNullableColor(start.activityColor, end.activityColor, progress),
        pressedOverlayColor = lerpNullableColor(start.pressedOverlayColor, end.pressedOverlayColor, progress),
        foregroundColor = lerpNullableColor(start.foregroundColor, end.foregroundColor, progress),
        textSize = lerpNullableTextUnit(start.textSize, end.textSize, progress),
        textLineHeight = lerpNullableTextUnit(start.textLineHeight, end.textLineHeight, progress),
        textFontFamily = if (progress < 1f) start.textFontFamily else end.textFontFamily,
        borderRadius = lerpNullableDp(start.borderRadius, end.borderRadius, progress),
        cornerRadii = lerpCornerRadii(start.cornerRadii, end.cornerRadii, progress),
        borderWidth = lerpNullableDp(start.borderWidth, end.borderWidth, progress),
        borderColor = lerpNullableColor(start.borderColor, end.borderColor, progress),
        raiseAmount = lerpNullableDp(start.raiseAmount, end.raiseAmount, progress),
        contentGap = lerpNullableDp(start.contentGap, end.contentGap, progress),
        animationDurationMillis = if (progress < 1f) start.animationDurationMillis else end.animationDurationMillis,
        animationCurve = if (progress < 1f) start.animationCurve else end.animationCurve,
        disabledBackgroundColor =
            lerpNullableColor(
                start.disabledBackgroundColor,
                end.disabledBackgroundColor,
                progress,
            ),
        disabledDepthColor = lerpNullableColor(start.disabledDepthColor, end.disabledDepthColor, progress),
        disabledShadowColor = lerpNullableColor(start.disabledShadowColor, end.disabledShadowColor, progress),
        disabledForegroundColor =
            lerpNullableColor(
                start.disabledForegroundColor,
                end.disabledForegroundColor,
                progress,
            ),
        disabledBorderColor = lerpNullableColor(start.disabledBorderColor, end.disabledBorderColor, progress),
    )
}

private fun lerpNullableColor(
    from: Color?,
    to: Color?,
    fraction: Float,
): Color? =
    when {
        from == null && to == null -> null
        from == null -> lerpColor(Color.Transparent, to!!, fraction)
        to == null -> lerpColor(from, Color.Transparent, fraction)
        else -> lerpColor(from, to, fraction)
    }

private fun lerpNullableDp(
    from: Dp?,
    to: Dp?,
    fraction: Float,
): Dp? =
    when {
        from == null && to == null -> null
        from == null -> lerpUnit(0.dp, to!!, fraction)
        to == null -> lerpUnit(from, 0.dp, fraction)
        else -> lerpUnit(from, to, fraction)
    }

private fun lerpNullableTextUnit(
    from: TextUnit?,
    to: TextUnit?,
    fraction: Float,
): TextUnit? =
    when {
        from == null && to == null -> null
        from == null -> to
        to == null -> from
        else -> lerpUnit(from, to, fraction)
    }

private fun lerpCornerRadii(
    from: AwesomeButtonCornerRadii?,
    to: AwesomeButtonCornerRadii?,
    fraction: Float,
): AwesomeButtonCornerRadii? {
    if (from == null && to == null) return null
    val start = from ?: AwesomeButtonCornerRadii.all(0.dp)
    val end = to ?: AwesomeButtonCornerRadii.all(0.dp)
    return AwesomeButtonCornerRadii(
        topStart = lerpUnit(start.topStart, end.topStart, fraction),
        topEnd = lerpUnit(start.topEnd, end.topEnd, fraction),
        bottomEnd = lerpUnit(start.bottomEnd, end.bottomEnd, fraction),
        bottomStart = lerpUnit(start.bottomStart, end.bottomStart, fraction),
    )
}
