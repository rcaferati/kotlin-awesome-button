package dev.caferati.awesomebutton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

internal fun normalizeOptionalDp(value: Dp?): Dp? {
    if (value == null || !value.isSpecified || !value.value.isFinite()) return null
    return value.value.coerceAtLeast(0f).dp
}

internal fun normalizeRequiredDp(
    value: Dp,
    fallback: Dp,
): Dp = normalizeOptionalDp(value) ?: fallback

internal fun normalizeOptionalTextUnit(value: TextUnit?): TextUnit? {
    if (value == null || !value.isSpecified || !value.value.isFinite()) return null
    val normalized = value.value.coerceAtLeast(0f)
    return when (value.type) {
        TextUnitType.Sp -> normalized.sp
        TextUnitType.Em -> normalized.em
        else -> null
    }
}

internal fun normalizeOptionalMillis(value: Int?): Int? = value?.coerceAtLeast(0)

internal fun resolvePressInDurationMillis(
    overrideMillis: Int?,
    styleMillis: Int?,
    fallbackMillis: Int = 140,
): Int =
    normalizeOptionalMillis(overrideMillis)
        ?: normalizeOptionalMillis(styleMillis)
        ?: fallbackMillis.coerceAtLeast(0)

internal fun normalizeOpacity(
    value: Float,
    fallback: Float = 1f,
): Float = (if (value.isFinite()) value else fallback).coerceIn(0f, 1f)

internal fun normalizeCornerRadii(value: AwesomeButtonCornerRadii?): AwesomeButtonCornerRadii? =
    value?.let {
        AwesomeButtonCornerRadii(
            topStart = normalizeRequiredDp(it.topStart, 0.dp),
            topEnd = normalizeRequiredDp(it.topEnd, 0.dp),
            bottomEnd = normalizeRequiredDp(it.bottomEnd, 0.dp),
            bottomStart = normalizeRequiredDp(it.bottomStart, 0.dp),
        )
    }

internal fun normalizeStyleInput(style: AwesomeButtonStyle): AwesomeButtonStyle =
    style.copy(
        textSize = normalizeOptionalTextUnit(style.textSize),
        textLineHeight = normalizeOptionalTextUnit(style.textLineHeight),
        borderRadius = normalizeOptionalDp(style.borderRadius),
        cornerRadii = normalizeCornerRadii(style.cornerRadii),
        borderWidth = normalizeOptionalDp(style.borderWidth),
        raiseAmount = normalizeOptionalDp(style.raiseAmount),
        contentGap = normalizeOptionalDp(style.contentGap),
        animationDurationMillis = normalizeOptionalMillis(style.animationDurationMillis),
    )
