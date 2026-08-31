package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit

/**
 * Theme-local values mapped into [AwesomeButtonStyle]. Null fields defer to the size or fallback
 * layer, and numeric values use the same non-negative normalization as the core button.
 *
 * @property activityColor progress-indicator color.
 * @property backgroundActive pressed face color.
 * @property backgroundColor idle face color.
 * @property backgroundDarker legacy name for the lower depth-layer color.
 * @property backgroundPlaceholder placeholder face color.
 * @property backgroundProgress progress-layer color.
 * @property backgroundShadow legacy name for the outer shadow color.
 * @property borderColor face border color.
 * @property borderRadius fallback radius for every corner.
 * @property borderBottomLeftRadius physical bottom-left corner override.
 * @property borderBottomRightRadius physical bottom-right corner override.
 * @property borderTopLeftRadius physical top-left corner override.
 * @property borderTopRightRadius physical top-right corner override.
 * @property borderWidth face border width in density-independent pixels.
 * @property height themed face height in density-independent pixels.
 * @property paddingBottom bottom content padding in density-independent pixels.
 * @property paddingHorizontal horizontal content padding in density-independent pixels.
 * @property paddingTop top content padding in density-independent pixels.
 * @property raiseLevel legacy name for visible depth height in density-independent pixels.
 * @property textColor built-in label foreground color.
 * @property textLineHeight built-in label line height.
 * @property textSize built-in label size.
 * @property width themed width in density-independent pixels.
 */
public data class ThemeButtonStyle(
    public val activityColor: Color? = null,
    public val backgroundActive: Color? = null,
    public val backgroundColor: Color? = null,
    public val backgroundDarker: Color? = null,
    public val backgroundPlaceholder: Color? = null,
    public val backgroundProgress: Color? = null,
    public val backgroundShadow: Color? = null,
    public val borderColor: Color? = null,
    public val borderRadius: Dp? = null,
    public val borderBottomLeftRadius: Dp? = null,
    public val borderBottomRightRadius: Dp? = null,
    public val borderTopLeftRadius: Dp? = null,
    public val borderTopRightRadius: Dp? = null,
    public val borderWidth: Dp? = null,
    public val height: Dp? = null,
    public val paddingBottom: Dp? = null,
    public val paddingHorizontal: Dp? = null,
    public val paddingTop: Dp? = null,
    public val raiseLevel: Dp? = null,
    public val textColor: Color? = null,
    public val textLineHeight: TextUnit? = null,
    public val textSize: TextUnit? = null,
    public val width: Dp? = null,
) {
    /** Returns this style with every non-null field from [other] applied. */
    public fun merge(other: ThemeButtonStyle?): ThemeButtonStyle {
        if (other == null) return this

        return ThemeButtonStyle(
            activityColor = other.activityColor ?: activityColor,
            backgroundActive = other.backgroundActive ?: backgroundActive,
            backgroundColor = other.backgroundColor ?: backgroundColor,
            backgroundDarker = other.backgroundDarker ?: backgroundDarker,
            backgroundPlaceholder = other.backgroundPlaceholder ?: backgroundPlaceholder,
            backgroundProgress = other.backgroundProgress ?: backgroundProgress,
            backgroundShadow = other.backgroundShadow ?: backgroundShadow,
            borderColor = other.borderColor ?: borderColor,
            borderRadius = other.borderRadius ?: borderRadius,
            borderBottomLeftRadius = other.borderBottomLeftRadius ?: borderBottomLeftRadius,
            borderBottomRightRadius = other.borderBottomRightRadius ?: borderBottomRightRadius,
            borderTopLeftRadius = other.borderTopLeftRadius ?: borderTopLeftRadius,
            borderTopRightRadius = other.borderTopRightRadius ?: borderTopRightRadius,
            borderWidth = other.borderWidth ?: borderWidth,
            height = other.height ?: height,
            paddingBottom = other.paddingBottom ?: paddingBottom,
            paddingHorizontal = other.paddingHorizontal ?: paddingHorizontal,
            paddingTop = other.paddingTop ?: paddingTop,
            raiseLevel = other.raiseLevel ?: raiseLevel,
            textColor = other.textColor ?: textColor,
            textLineHeight = other.textLineHeight ?: textLineHeight,
            textSize = other.textSize ?: textSize,
            width = other.width ?: width,
        )
    }
}

/**
 * Size preset values used by [ThemedButton].
 *
 * @property width preset width in density-independent pixels.
 * @property height preset face height in density-independent pixels.
 * @property textSize preset built-in label size.
 * @property paddingHorizontal preset horizontal padding in density-independent pixels.
 */
public data class ThemeSizeStyle(
    public val width: Dp? = null,
    public val height: Dp? = null,
    public val textSize: TextUnit? = null,
    public val paddingHorizontal: Dp? = null,
)

/**
 * Complete theme definition for [ThemedButton].
 *
 * @property title human-readable theme title.
 * @property background showcase background color.
 * @property color showcase foreground color.
 * @property buttons variant-to-style map.
 * @property size named size-preset map.
 */
public open class ThemeDefinition(
    public open val title: String,
    public open val background: Color,
    public open val color: Color,
    public open val buttons: Map<ButtonVariant, ThemeButtonStyle>,
    public open val size: Map<ButtonSize, ThemeSizeStyle>,
)

/**
 * Built-in theme definition decorated with registry navigation metadata.
 *
 * @property title human-readable theme title.
 * @property background showcase background color.
 * @property color showcase foreground color.
 * @property buttons variant-to-style map.
 * @property size named size-preset map.
 * @property name stable built-in theme name.
 * @property next whether another registered theme follows this one.
 * @property prev whether another registered theme precedes this one.
 */
public data class RegisteredThemeDefinition(
    public override val title: String,
    public override val background: Color,
    public override val color: Color,
    public override val buttons: Map<ButtonVariant, ThemeButtonStyle>,
    public override val size: Map<ButtonSize, ThemeSizeStyle>,
    public val name: ThemeName,
    public val next: Boolean,
    public val prev: Boolean,
) : ThemeDefinition(title, background, color, buttons, size)

internal fun ThemeButtonStyle.toAwesomeButtonStyle(
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): AwesomeButtonStyle {
    val baseRadius = normalizeOptionalDp(borderRadius)
    val normalizedTopLeft = normalizeOptionalDp(borderTopLeftRadius)
    val normalizedTopRight = normalizeOptionalDp(borderTopRightRadius)
    val normalizedBottomLeft = normalizeOptionalDp(borderBottomLeftRadius)
    val normalizedBottomRight = normalizeOptionalDp(borderBottomRightRadius)
    val hasPerCornerRadius =
        normalizedTopLeft != null ||
            normalizedTopRight != null ||
            normalizedBottomRight != null ||
            normalizedBottomLeft != null
    val cornerRadii =
        if (hasPerCornerRadius) {
            if (layoutDirection == LayoutDirection.Ltr) {
                AwesomeButtonCornerRadii(
                    topStart = normalizedTopLeft ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    topEnd = normalizedTopRight ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    bottomEnd =
                        normalizedBottomRight ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    bottomStart =
                        normalizedBottomLeft ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                )
            } else {
                AwesomeButtonCornerRadii(
                    topStart = normalizedTopRight ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    topEnd = normalizedTopLeft ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    bottomEnd =
                        normalizedBottomLeft ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                    bottomStart =
                        normalizedBottomRight ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                )
            }
        } else {
            null
        }

    return AwesomeButtonStyle(
        backgroundColor = backgroundColor,
        backgroundActive = backgroundActive,
        backgroundPlaceholder = backgroundPlaceholder,
        backgroundProgress = backgroundProgress,
        depthColor = backgroundDarker,
        shadowColor = backgroundShadow,
        activityColor = activityColor,
        foregroundColor = textColor,
        textSize = textSize,
        textLineHeight = textLineHeight,
        borderRadius = baseRadius,
        cornerRadii = cornerRadii,
        borderWidth = borderWidth,
        borderColor = borderColor,
        raiseAmount = raiseLevel,
    )
}
