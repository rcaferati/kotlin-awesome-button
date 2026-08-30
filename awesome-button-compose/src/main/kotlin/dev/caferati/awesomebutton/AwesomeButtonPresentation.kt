package dev.caferati.awesomebutton

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Immutable, callback-free presentation targets resolved for one committed composition. */
internal data class AwesomeButtonPresentation(
    val targetStyle: AwesomeButtonStyle,
    val fallbackStyle: AwesomeButtonStyle,
    val normalizedWidth: Dp?,
    val normalizedHeight: Dp,
    val paddingHorizontal: Dp,
    val paddingTop: Dp,
    val paddingBottom: Dp,
    val activeOpacity: Float,
    val debounceMillis: Long,
    val progressLoadingMillis: Int,
    val pressInDurationMillis: Int?,
    val isPlaceholder: Boolean,
    val effectiveDisabled: Boolean,
    val reduceMotion: Boolean,
    val busyStateDescription: String,
    val defaultLongPressLabel: String,
    val accessibilityTextGrowth: Boolean,
    val hasCustomContent: Boolean,
    val widthMode: ButtonWidthMode,
    val targetHeightPx: Int,
    val targetPaddingHorizontalPx: Int,
    val targetPaddingTopPx: Int,
    val targetPaddingBottomPx: Int,
    val targetBorderWidthPx: Int,
    val targetTextStyle: TextStyle,
    val sizeSignature: SizeTransitionSignature,
)

internal data class AwesomeButtonPresentationInput(
    val themeStyle: AwesomeButtonStyle,
    val style: AwesomeButtonStyle?,
    val styleIsResolvedFrame: Boolean,
    val childPresent: Boolean,
    val customContentPresent: Boolean,
    val beforePresent: Boolean,
    val afterPresent: Boolean,
    val disabled: Boolean,
    val width: Dp?,
    val height: Dp,
    val paddingHorizontal: Dp?,
    val paddingTop: Dp?,
    val paddingBottom: Dp?,
    val stretch: Boolean,
    val activeOpacity: Float,
    val debouncedPressTimeMillis: Long,
    val progressLoadingTimeMillis: Int,
    val pressInAnimationDurationMillis: Int?,
    val reduceMotion: Boolean,
    val busyStateDescription: String,
    val defaultLongPressLabel: String,
    val density: Density,
    val fontScale: Float,
)

internal data class SizeTransitionSignature(
    val heightPx: Int,
    val paddingHorizontalPx: Int,
    val paddingTopPx: Int,
    val paddingBottomPx: Int,
    val style: AwesomeButtonStyle,
)

internal fun resolveAwesomeButtonPresentation(input: AwesomeButtonPresentationInput): AwesomeButtonPresentation {
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val targetStyle =
        if (input.styleIsResolvedFrame) {
            resolvedVisualStyle(input.style ?: fallback)
        } else {
            resolvedVisualStyle(input.themeStyle.merge(input.style))
        }
    val normalizedWidth = normalizeOptionalDp(input.width)
    val normalizedHeight = normalizeRequiredDp(input.height, 52.dp)
    val paddingHorizontal = normalizeOptionalDp(input.paddingHorizontal) ?: 16.dp
    val paddingTop = normalizeOptionalDp(input.paddingTop) ?: 0.dp
    val paddingBottom = normalizeOptionalDp(input.paddingBottom) ?: 0.dp
    val isPlaceholder = !input.childPresent && !input.customContentPresent
    val hasCustomContent =
        input.customContentPresent ||
            input.beforePresent ||
            input.afterPresent
    val widthMode =
        when {
            input.stretch -> ButtonWidthMode.Stretch
            normalizedWidth != null -> ButtonWidthMode.Fixed
            else -> ButtonWidthMode.Auto
        }
    val targetHeightPx = with(input.density) { normalizedHeight.roundToPx() }
    val targetPaddingHorizontalPx = with(input.density) { paddingHorizontal.roundToPx() }
    val targetPaddingTopPx = with(input.density) { paddingTop.roundToPx() }
    val targetPaddingBottomPx = with(input.density) { paddingBottom.roundToPx() }
    val targetBorderWidthPx =
        with(input.density) {
            (targetStyle.borderWidth ?: fallback.borderWidth!!).roundToPx()
        }
    val targetTextStyle =
        TextStyle(
            fontSize = targetStyle.textSize ?: fallback.textSize!!,
            lineHeight = targetStyle.textLineHeight ?: fallback.textLineHeight!!,
            fontFamily = targetStyle.textFontFamily,
            fontWeight = FontWeight.Bold,
        )

    return AwesomeButtonPresentation(
        targetStyle = targetStyle,
        fallbackStyle = fallback,
        normalizedWidth = normalizedWidth,
        normalizedHeight = normalizedHeight,
        paddingHorizontal = paddingHorizontal,
        paddingTop = paddingTop,
        paddingBottom = paddingBottom,
        activeOpacity = normalizeOpacity(input.activeOpacity),
        debounceMillis = input.debouncedPressTimeMillis.coerceAtLeast(0),
        progressLoadingMillis = input.progressLoadingTimeMillis.coerceAtLeast(0),
        pressInDurationMillis = normalizeOptionalMillis(input.pressInAnimationDurationMillis),
        isPlaceholder = isPlaceholder,
        effectiveDisabled = input.disabled || isPlaceholder,
        reduceMotion = input.reduceMotion,
        busyStateDescription = input.busyStateDescription,
        defaultLongPressLabel = input.defaultLongPressLabel,
        accessibilityTextGrowth = input.fontScale > 1f,
        hasCustomContent = hasCustomContent,
        widthMode = widthMode,
        targetHeightPx = targetHeightPx,
        targetPaddingHorizontalPx = targetPaddingHorizontalPx,
        targetPaddingTopPx = targetPaddingTopPx,
        targetPaddingBottomPx = targetPaddingBottomPx,
        targetBorderWidthPx = targetBorderWidthPx,
        targetTextStyle = targetTextStyle,
        sizeSignature =
            SizeTransitionSignature(
                heightPx = targetHeightPx,
                paddingHorizontalPx = targetPaddingHorizontalPx,
                paddingTopPx = targetPaddingTopPx,
                paddingBottomPx = targetPaddingBottomPx,
                style = targetStyle,
            ),
    )
}
