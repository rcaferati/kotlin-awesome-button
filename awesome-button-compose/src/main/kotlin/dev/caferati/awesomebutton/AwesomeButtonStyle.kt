package dev.caferati.awesomebutton

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpUnit
import androidx.compose.ui.unit.sp

/** Per-corner radius configuration for the shell, face, and shadow layers. */
data class AwesomeButtonCornerRadii(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomEnd: Dp,
    val bottomStart: Dp,
) {
    companion object {
        fun all(radius: Dp) = AwesomeButtonCornerRadii(
            topStart = radius,
            topEnd = radius,
            bottomEnd = radius,
            bottomStart = radius,
        )
    }
}

enum class AwesomeButtonAnimationCurve {
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

/** Visual configuration for [AwesomeButton]. */
data class AwesomeButtonStyle(
    val backgroundColor: Color? = null,
    val backgroundActive: Color? = null,
    val backgroundPlaceholder: Color? = null,
    val backgroundProgress: Color? = null,
    val depthColor: Color? = null,
    val shadowColor: Color? = null,
    val activityColor: Color? = null,
    val pressedOverlayColor: Color? = null,
    val foregroundColor: Color? = null,
    val textSize: TextUnit? = null,
    val textLineHeight: TextUnit? = null,
    val textFontFamily: FontFamily? = null,
    val borderRadius: Dp? = null,
    val cornerRadii: AwesomeButtonCornerRadii? = null,
    val borderWidth: Dp? = null,
    val borderColor: Color? = null,
    val raiseAmount: Dp? = null,
    val contentGap: Dp? = null,
    val animationDurationMillis: Int? = null,
    val animationCurve: AwesomeButtonAnimationCurve? = null,
    val disabledBackgroundColor: Color? = null,
    val disabledDepthColor: Color? = null,
    val disabledShadowColor: Color? = null,
    val disabledForegroundColor: Color? = null,
    val disabledBorderColor: Color? = null,
) {
    fun merge(other: AwesomeButtonStyle?): AwesomeButtonStyle {
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
            textSize = other.textSize ?: textSize,
            textLineHeight = other.textLineHeight ?: textLineHeight,
            textFontFamily = other.textFontFamily ?: textFontFamily,
            borderRadius = other.borderRadius ?: borderRadius,
            cornerRadii = other.cornerRadii ?: cornerRadii,
            borderWidth = other.borderWidth ?: borderWidth,
            borderColor = other.borderColor ?: borderColor,
            raiseAmount = other.raiseAmount ?: raiseAmount,
            contentGap = other.contentGap ?: contentGap,
            animationDurationMillis = other.animationDurationMillis ?: animationDurationMillis,
            animationCurve = other.animationCurve ?: animationCurve,
            disabledBackgroundColor = other.disabledBackgroundColor ?: disabledBackgroundColor,
            disabledDepthColor = other.disabledDepthColor ?: disabledDepthColor,
            disabledShadowColor = other.disabledShadowColor ?: disabledShadowColor,
            disabledForegroundColor = other.disabledForegroundColor ?: disabledForegroundColor,
            disabledBorderColor = other.disabledBorderColor ?: disabledBorderColor,
        )
    }
}

/** Theme data resolved through [AwesomeButtonTheme]. */
data class AwesomeButtonThemeData(
    val style: AwesomeButtonStyle,
) {
    fun merge(style: AwesomeButtonStyle?) = AwesomeButtonThemeData(this.style.merge(style))

    companion object {
        val fallbackStyle = AwesomeButtonStyle(
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

        val fallback = AwesomeButtonThemeData(fallbackStyle)
    }
}

internal fun resolvedVisualStyle(style: AwesomeButtonStyle): AwesomeButtonStyle {
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val borderRadius = style.borderRadius ?: fallback.borderRadius!!
    return AwesomeButtonStyle(
        backgroundColor = style.backgroundColor ?: fallback.backgroundColor,
        backgroundActive = style.backgroundActive ?: fallback.backgroundActive,
        backgroundPlaceholder = style.backgroundPlaceholder ?: fallback.backgroundPlaceholder,
        backgroundProgress = style.backgroundProgress ?: fallback.backgroundProgress,
        depthColor = style.depthColor ?: fallback.depthColor,
        shadowColor = style.shadowColor ?: fallback.shadowColor,
        activityColor = style.activityColor ?: fallback.activityColor,
        pressedOverlayColor = style.pressedOverlayColor ?: fallback.pressedOverlayColor,
        foregroundColor = style.foregroundColor ?: fallback.foregroundColor,
        textSize = style.textSize ?: fallback.textSize,
        textLineHeight = style.textLineHeight ?: fallback.textLineHeight,
        textFontFamily = style.textFontFamily ?: fallback.textFontFamily,
        borderRadius = borderRadius,
        cornerRadii = style.cornerRadii ?: AwesomeButtonCornerRadii.all(borderRadius),
        borderWidth = style.borderWidth ?: fallback.borderWidth,
        borderColor = style.borderColor ?: fallback.borderColor,
        raiseAmount = style.raiseAmount ?: fallback.raiseAmount,
        contentGap = style.contentGap ?: fallback.contentGap,
        animationDurationMillis = style.animationDurationMillis ?: fallback.animationDurationMillis,
        animationCurve = style.animationCurve ?: fallback.animationCurve,
        disabledBackgroundColor = style.disabledBackgroundColor,
        disabledDepthColor = style.disabledDepthColor,
        disabledShadowColor = style.disabledShadowColor,
        disabledForegroundColor = style.disabledForegroundColor,
        disabledBorderColor = style.disabledBorderColor,
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
        disabledBackgroundColor = lerpNullableColor(start.disabledBackgroundColor, end.disabledBackgroundColor, progress),
        disabledDepthColor = lerpNullableColor(start.disabledDepthColor, end.disabledDepthColor, progress),
        disabledShadowColor = lerpNullableColor(start.disabledShadowColor, end.disabledShadowColor, progress),
        disabledForegroundColor = lerpNullableColor(start.disabledForegroundColor, end.disabledForegroundColor, progress),
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
