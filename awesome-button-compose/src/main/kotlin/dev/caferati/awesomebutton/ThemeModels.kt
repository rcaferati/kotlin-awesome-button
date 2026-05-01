package dev.caferati.awesomebutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/** Theme-local style values that map into [AwesomeButtonStyle]. */
data class ThemeButtonStyle(
    val activityColor: Color? = null,
    val backgroundActive: Color? = null,
    val backgroundColor: Color? = null,
    val backgroundDarker: Color? = null,
    val backgroundPlaceholder: Color? = null,
    val backgroundProgress: Color? = null,
    val backgroundShadow: Color? = null,
    val borderColor: Color? = null,
    val borderRadius: Dp? = null,
    val borderBottomLeftRadius: Dp? = null,
    val borderBottomRightRadius: Dp? = null,
    val borderTopLeftRadius: Dp? = null,
    val borderTopRightRadius: Dp? = null,
    val borderWidth: Dp? = null,
    val height: Dp? = null,
    val paddingBottom: Dp? = null,
    val paddingHorizontal: Dp? = null,
    val paddingTop: Dp? = null,
    val raiseLevel: Dp? = null,
    val textColor: Color? = null,
    val textLineHeight: TextUnit? = null,
    val textSize: TextUnit? = null,
    val width: Dp? = null,
) {
    fun merge(other: ThemeButtonStyle?): ThemeButtonStyle {
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

/** Size preset values used by [ThemedButton]. */
data class ThemeSizeStyle(
    val width: Dp? = null,
    val height: Dp? = null,
    val textSize: TextUnit? = null,
    val paddingHorizontal: Dp? = null,
)

/** Complete theme definition for [ThemedButton]. */
open class ThemeDefinition(
    open val title: String,
    open val background: Color,
    open val color: Color,
    open val buttons: Map<ButtonVariant, ThemeButtonStyle>,
    open val size: Map<ButtonSize, ThemeSizeStyle>,
)

/** Built-in theme definition decorated with registry navigation metadata. */
data class RegisteredThemeDefinition(
    override val title: String,
    override val background: Color,
    override val color: Color,
    override val buttons: Map<ButtonVariant, ThemeButtonStyle>,
    override val size: Map<ButtonSize, ThemeSizeStyle>,
    val name: ThemeName,
    val next: Boolean,
    val prev: Boolean,
) : ThemeDefinition(title, background, color, buttons, size)

internal fun ThemeButtonStyle.toAwesomeButtonStyle(): AwesomeButtonStyle {
    val baseRadius = borderRadius
    val hasPerCornerRadius =
        borderTopLeftRadius != null ||
            borderTopRightRadius != null ||
            borderBottomRightRadius != null ||
            borderBottomLeftRadius != null
    val cornerRadii =
        if (hasPerCornerRadius) {
            AwesomeButtonCornerRadii(
                topStart = borderTopLeftRadius ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                topEnd = borderTopRightRadius ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                bottomEnd = borderBottomRightRadius ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
                bottomStart = borderBottomLeftRadius ?: baseRadius ?: AwesomeButtonThemeData.fallbackStyle.borderRadius!!,
            )
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
