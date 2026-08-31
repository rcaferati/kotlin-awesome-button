package dev.caferati.awesomebutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Resolves a built-in or custom theme and delegates native interaction ownership to [AwesomeButton].
 *
 * Requested flat styling is preserved while disabled. Otherwise disabled styling overrides the
 * requested variant. Explicit geometry and [style] values precede variant, size, and fallback
 * values. Reduced Motion snaps the wrapper's theme transition.
 *
 * @param modifier modifier applied to the package-owned interaction surface.
 * @param child optional built-in string label.
 * @param config custom theme definition, taking precedence over [index] and [name].
 * @param index optional registered-theme index.
 * @param name optional registered-theme name.
 * @param type requested semantic or social variant; [ButtonVariant.Flat] remains flat while disabled.
 * @param size named size preset.
 * @param flat whether the flat visual variant is requested, including while disabled.
 * @param transparent whether resolved face/depth colors become transparent.
 * @param textTransition whether built-in string replacements use the staggered text effect.
 * @param textTransitionSlotStaggerMillis non-negative text-slot stagger in milliseconds.
 * @param animatedPlaceholder whether placeholder shimmer animates when motion is allowed.
 * @param pressInAnimationDurationMillis optional non-negative press-down duration override.
 * @param accessibilityLabel explicit accessible name.
 * @param accessibilityHint optional localized assistive usage hint.
 * @param accessibilityLongPressLabel optional localized long-press action label.
 * @param onPress activation callback, optionally receiving a one-shot progress handle.
 * @param onLongPress optional native long-press action.
 * @param disabled whether the button rejects and cancels interaction ownership.
 * @param width explicit width in density-independent pixels.
 * @param autoWidth whether intrinsic width is requested when [width] is absent.
 * @param height explicit face height in density-independent pixels.
 * @param paddingHorizontal optional horizontal content padding.
 * @param paddingTop optional top content padding.
 * @param paddingBottom optional bottom content padding.
 * @param before leading content included in intrinsic width.
 * @param after trailing content included in intrinsic width.
 * @param extra face overlay excluded from intrinsic width.
 * @param stretch whether width fills available horizontal constraints.
 * @param style explicit visual overrides applied after theme resolution.
 * @param activeOpacity pressed opacity normalized to zero through one.
 * @param debouncedPressTimeMillis non-negative interval between accepted activations.
 * @param progress whether activation uses progress completion ownership.
 * @param showProgressBar whether busy state renders the progress layer.
 * @param progressLoadingTimeMillis non-negative progress fill duration in milliseconds.
 * @param animateSize whether supported content-driven size changes animate.
 * @param onPressIn callback dispatched when a pointer arms this gesture.
 * @param onPressOut callback dispatched after a release or cancellation terminal is claimed.
 * @param onPressedIn callback dispatched after pressed state is committed.
 * @param onPressedOut callback captured when release starts and dispatched on settle.
 * @param onProgressStart callback dispatched when accepted progress begins.
 * @param onProgressEnd callback captured when progress completion begins and dispatched on settle.
 * @param content optional arbitrary primary content included in intrinsic width.
 */
@Composable
public fun ThemedButton(
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
    textTransitionSlotStaggerMillis: Int = DEFAULT_TEXT_TRANSITION_SLOT_STAGGER_MILLIS,
    animatedPlaceholder: Boolean = true,
    pressInAnimationDurationMillis: Int? = null,
    accessibilityLabel: String? = null,
    accessibilityHint: String? = null,
    accessibilityLongPressLabel: String? = null,
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
    val layoutDirection = LocalLayoutDirection.current
    val theme = resolveTheme(config = config, index = index, name = name)
    val buttonType = resolveButtonType(theme, disabled, flat, type)
    val buttonStyle =
        (theme.buttons[buttonType] ?: theme.buttons[ButtonVariant.Primary] ?: ThemeButtonStyle())
            .let { if (transparent) it.merge(transparentStyles) else it }
    val sizeStyle =
        theme.size[size] ?: theme.size[ButtonSize.Medium] ?: fallbackMediumSize
    val requestedStyle =
        resolveThemedRequestedStyle(
            buttonStyle = buttonStyle,
            sizeStyle = sizeStyle,
            explicitStyle = style,
            layoutDirection = layoutDirection,
        )
    val targetStyle = resolvedVisualStyle(AwesomeButtonTheme.current.style.merge(requestedStyle))
    val reduceMotion = rememberAwesomeButtonReduceMotion()
    val themeTransitionProgress = remember { Animatable(1f) }
    var themeTransitionSource by remember { mutableStateOf(targetStyle) }
    var themeTransitionTarget by remember { mutableStateOf(targetStyle) }
    val transitionContext =
        remember {
            ThemedTransitionContext(
                config = config,
                index = index,
                name = name,
                buttonType = buttonType,
                transparent = transparent,
            )
        }
    val themedStyle =
        interpolateAwesomeButtonStyle(
            themeTransitionSource,
            themeTransitionTarget,
            themeTransitionProgress.value,
        )

    LaunchedEffect(targetStyle, reduceMotion, theme, buttonType, transparent) {
        val currentStyle =
            interpolateAwesomeButtonStyle(
                themeTransitionSource,
                themeTransitionTarget,
                themeTransitionProgress.value,
            )
        val sameThemeSource =
            isSameThemedSource(
                previousConfig = transitionContext.config,
                previousIndex = transitionContext.index,
                previousName = transitionContext.name,
                config = config,
                index = index,
                name = name,
            )
        val sameTransparent = transitionContext.transparent == transparent
        val variantChanged = transitionContext.buttonType != buttonType
        transitionContext.config = config
        transitionContext.index = index
        transitionContext.name = name
        transitionContext.buttonType = buttonType
        transitionContext.transparent = transparent
        val durationMillis =
            resolveThemedStyleTransitionDurationMillis(
                sameThemeSource = sameThemeSource,
                sameTransparent = sameTransparent,
                variantChanged = variantChanged,
                reduceMotion = reduceMotion,
                styleDurationMillis = targetStyle.animationDurationMillis ?: 140,
            )
        if (currentStyle == targetStyle || durationMillis == 0) {
            themeTransitionSource = targetStyle
            themeTransitionTarget = targetStyle
            themeTransitionProgress.snapTo(1f)
            return@LaunchedEffect
        }
        themeTransitionSource = currentStyle
        themeTransitionTarget = targetStyle
        themeTransitionProgress.snapTo(0f)
        themeTransitionProgress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = durationMillis,
                    easing =
                        (
                            targetStyle.animationCurve
                                ?: AwesomeButtonThemeData.fallbackStyle.animationCurve!!
                        ).toEasing(),
                ),
        )
    }
    val resolvedWidth =
        resolveThemedWidth(
            stretch = stretch,
            explicitWidth = width,
            autoWidth = autoWidth,
            variantWidth = buttonStyle.width,
            sizeWidth = sizeStyle.width,
        )

    AwesomeButtonImpl(
        modifier = modifier,
        child = child,
        onPress = onPress,
        onLongPress = onLongPress,
        disabled = disabled,
        width = resolvedWidth,
        height = resolveThemedHeight(height, buttonStyle.height, sizeStyle.height),
        paddingHorizontal =
            normalizeOptionalDp(paddingHorizontal)
                ?: normalizeOptionalDp(buttonStyle.paddingHorizontal)
                ?: normalizeOptionalDp(sizeStyle.paddingHorizontal),
        paddingTop =
            normalizeOptionalDp(paddingTop)
                ?: normalizeOptionalDp(buttonStyle.paddingTop),
        paddingBottom =
            normalizeOptionalDp(paddingBottom)
                ?: normalizeOptionalDp(buttonStyle.paddingBottom),
        before = before,
        after = after,
        extra = extra,
        stretch = stretch,
        style = themedStyle,
        styleIsResolvedFrame = true,
        sizeTargetStyle = targetStyle,
        reduceMotionOverride = reduceMotion,
        activeOpacity = activeOpacity,
        debouncedPressTimeMillis = debouncedPressTimeMillis,
        progress = progress,
        showProgressBar = showProgressBar,
        progressLoadingTimeMillis = progressLoadingTimeMillis,
        animateSize = animateSize,
        textTransition = textTransition,
        textTransitionSlotStaggerMillis = textTransitionSlotStaggerMillis,
        animatedPlaceholder = animatedPlaceholder,
        pressInAnimationDurationMillis = pressInAnimationDurationMillis,
        accessibilityLabel = accessibilityLabel,
        accessibilityHint = accessibilityHint,
        accessibilityLongPressLabel = accessibilityLongPressLabel,
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

private val transparentStyles =
    ThemeButtonStyle(
        backgroundColor = Color.Transparent,
        backgroundDarker = Color.Transparent,
        backgroundPlaceholder = Color.Transparent,
        backgroundShadow = Color.Transparent,
        borderColor = Color.Transparent,
    )

internal const val THEMED_STYLE_TRANSITION_DURATION_MILLIS = 200

private class ThemedTransitionContext(
    var config: ThemeDefinition?,
    var index: Int?,
    var name: ThemeName?,
    var buttonType: ButtonVariant,
    var transparent: Boolean,
)

internal fun isSameThemedSource(
    previousConfig: ThemeDefinition?,
    previousIndex: Int?,
    previousName: ThemeName?,
    config: ThemeDefinition?,
    index: Int?,
    name: ThemeName?,
): Boolean =
    if (previousConfig != null || config != null) {
        previousConfig === config
    } else {
        previousIndex == index && previousName == name
    }

internal fun resolveThemedRequestedStyle(
    buttonStyle: ThemeButtonStyle,
    sizeStyle: ThemeSizeStyle,
    explicitStyle: AwesomeButtonStyle?,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
): AwesomeButtonStyle =
    AwesomeButtonStyle(textSize = normalizeOptionalTextUnit(sizeStyle.textSize))
        .merge(buttonStyle.toAwesomeButtonStyle(layoutDirection))
        .merge(explicitStyle)

internal fun resolveThemedStyleTransitionDurationMillis(
    sameThemeSource: Boolean,
    sameTransparent: Boolean,
    variantChanged: Boolean,
    reduceMotion: Boolean,
    styleDurationMillis: Int,
): Int =
    when {
        reduceMotion || !sameThemeSource || !sameTransparent -> 0
        variantChanged -> THEMED_STYLE_TRANSITION_DURATION_MILLIS
        else -> styleDurationMillis.coerceAtLeast(0)
    }

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

internal fun resolveButtonType(
    theme: ThemeDefinition,
    disabled: Boolean,
    flat: Boolean,
    type: ButtonVariant,
): ButtonVariant {
    val flatRequested = flat || type == ButtonVariant.Flat
    val requestedType =
        when {
            flatRequested -> ButtonVariant.Flat
            disabled -> ButtonVariant.Disabled
            else -> type
        }
    return if (theme.buttons.containsKey(requestedType)) requestedType else ButtonVariant.Primary
}

internal fun resolveThemedWidth(
    stretch: Boolean,
    explicitWidth: Dp?,
    autoWidth: Boolean,
    variantWidth: Dp?,
    sizeWidth: Dp?,
): Dp? {
    val normalizedExplicit = normalizeOptionalDp(explicitWidth)
    return when {
        stretch -> normalizedExplicit
        normalizedExplicit != null -> normalizedExplicit
        autoWidth -> null
        else -> normalizeOptionalDp(variantWidth) ?: normalizeOptionalDp(sizeWidth)
    }
}

internal fun resolveThemedHeight(
    explicitHeight: Dp?,
    variantHeight: Dp?,
    sizeHeight: Dp?,
): Dp =
    normalizeOptionalDp(explicitHeight)
        ?: normalizeOptionalDp(variantHeight)
        ?: normalizeOptionalDp(sizeHeight)
        ?: 52.dp
