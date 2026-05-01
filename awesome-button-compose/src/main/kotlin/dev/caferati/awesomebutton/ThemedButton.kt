package dev.caferati.awesomebutton

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ThemedButton(
    modifier: Modifier = Modifier,
    child: String? = null,
    config: ThemeDefinition? = null,
    index: Int? = null,
    name: ThemeName? = null,
    type: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    flat: Boolean = false,
    transparent: Boolean = false,
    textTransition: Boolean = false,
    textTransitionSlotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
    animatedPlaceholder: Boolean = true,
    onPress: AwesomeButtonPressCallback? = null,
    onLongPress: (() -> Unit)? = null,
    disabled: Boolean = false,
    width: Dp? = null,
    autoWidth: Boolean = false,
    height: Dp? = null,
    paddingHorizontal: Dp? = null,
    paddingTop: Dp? = null,
    paddingBottom: Dp? = null,
    before: (@Composable RowScope.() -> Unit)? = null,
    after: (@Composable RowScope.() -> Unit)? = null,
    extra: (@Composable BoxScope.() -> Unit)? = null,
    stretch: Boolean = false,
    style: AwesomeButtonStyle? = null,
    activeOpacity: Float = 1f,
    debouncedPressTimeMillis: Long = 0,
    progress: Boolean = false,
    showProgressBar: Boolean = true,
    progressLoadingTimeMillis: Int = 3000,
    animateSize: Boolean = true,
    onPressIn: (() -> Unit)? = null,
    onPressOut: (() -> Unit)? = null,
    onPressedIn: (() -> Unit)? = null,
    onPressedOut: (() -> Unit)? = null,
    onProgressStart: (() -> Unit)? = null,
    onProgressEnd: (() -> Unit)? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val theme = resolveTheme(config = config, index = index, name = name)
    val buttonType = resolveButtonType(theme, disabled, flat, type)
    val buttonStyle =
        (theme.buttons[buttonType] ?: theme.buttons[ButtonVariant.Primary] ?: ThemeButtonStyle())
            .let { if (transparent) it.merge(transparentStyles) else it }
    val sizeStyle =
        theme.size[size] ?: theme.size[ButtonSize.Medium] ?: fallbackMediumSize
    val themedStyle = buttonStyle
        .toAwesomeButtonStyle()
        .merge(AwesomeButtonStyle(textSize = sizeStyle.textSize))
        .merge(style)
    val resolvedWidth =
        when {
            stretch -> width
            autoWidth && width == null -> null
            else -> width ?: buttonStyle.width ?: sizeStyle.width
        }

    AwesomeButton(
        modifier = modifier,
        child = child,
        onPress = onPress,
        onLongPress = onLongPress,
        disabled = disabled,
        width = resolvedWidth,
        height = height ?: buttonStyle.height ?: sizeStyle.height ?: 60.dp,
        paddingHorizontal = paddingHorizontal ?: sizeStyle.paddingHorizontal ?: buttonStyle.paddingHorizontal,
        paddingTop = paddingTop ?: buttonStyle.paddingTop,
        paddingBottom = paddingBottom ?: buttonStyle.paddingBottom,
        before = before,
        after = after,
        extra = extra,
        stretch = stretch,
        style = themedStyle,
        activeOpacity = activeOpacity,
        debouncedPressTimeMillis = debouncedPressTimeMillis,
        progress = progress,
        showProgressBar = showProgressBar,
        progressLoadingTimeMillis = progressLoadingTimeMillis,
        animateSize = animateSize,
        textTransition = textTransition,
        textTransitionSlotStaggerMillis = textTransitionSlotStaggerMillis,
        animatedPlaceholder = animatedPlaceholder,
        onPressIn = onPressIn,
        onPressOut = onPressOut,
        onPressedIn = onPressedIn,
        onPressedOut = onPressedOut,
        onProgressStart = onProgressStart,
        onProgressEnd = onProgressEnd,
        content = content,
    )
}

private val fallbackMediumSize = ThemeSizeStyle(width = 200.dp, height = 60.dp)

private val transparentStyles = ThemeButtonStyle(
    backgroundColor = Color.Transparent,
    backgroundDarker = Color.Transparent,
    backgroundPlaceholder = Color.Transparent,
    backgroundShadow = Color.Transparent,
    borderColor = Color.Transparent,
)

private fun resolveTheme(
    config: ThemeDefinition?,
    index: Int?,
    name: ThemeName?,
): ThemeDefinition =
    when {
        config != null -> config
        name != null -> getTheme(name)
        else -> getTheme(index ?: 0)
    }

private fun resolveButtonType(
    theme: ThemeDefinition,
    disabled: Boolean,
    flat: Boolean,
    type: ButtonVariant,
): ButtonVariant {
    val requestedType =
        when {
            disabled -> ButtonVariant.Disabled
            flat -> ButtonVariant.Flat
            else -> type
        }
    return if (theme.buttons.containsKey(requestedType)) requestedType else ButtonVariant.Primary
}
