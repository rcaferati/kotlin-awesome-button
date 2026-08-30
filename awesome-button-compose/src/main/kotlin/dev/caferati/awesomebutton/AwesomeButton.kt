package dev.caferati.awesomebutton

import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Renders an Awesome Button using native Compose layout and interaction ownership.
 *
 * Empty [child] and absent [content] render the non-interactive placeholder. Reduced Motion snaps
 * visual effects while retaining gesture thresholds, debounce, progress ownership, state order,
 * and completion callbacks.
 *
 * @param modifier modifier applied to the package-owned interaction surface.
 * @param child optional built-in string label; use [content] for arbitrary Compose content.
 * @param onPress activation callback; progress buttons receive a one-shot completion handle.
 * @param onLongPress optional long-press action using the platform gesture threshold.
 * @param disabled whether the button rejects and cancels interaction ownership.
 * @param width optional fixed face width in density-independent pixels; null uses intrinsic width.
 * @param height height of the interactive face before the resolved raise/depth layer is added. The
 * total shell height is `height + resolved raise amount`.
 * @param paddingHorizontal optional horizontal content padding in density-independent pixels.
 * @param paddingTop optional top content padding in density-independent pixels.
 * @param paddingBottom optional bottom content padding in density-independent pixels.
 * @param before leading content included in intrinsic-width measurement.
 * @param after trailing content included in intrinsic-width measurement.
 * @param extra face overlay excluded from intrinsic-width measurement.
 * @param stretch whether width fills the available horizontal constraints.
 * @param style visual overrides applied after theme values.
 * @param activeOpacity pressed opacity normalized to the inclusive range zero through one.
 * @param debouncedPressTimeMillis non-negative interval between accepted activations.
 * @param progress whether activation uses one-shot progress completion ownership.
 * @param showProgressBar whether busy state renders the progress layer.
 * @param progressLoadingTimeMillis non-negative progress fill duration in milliseconds.
 * @param animateSize whether supported content-driven size changes animate.
 * @param textTransition whether built-in string changes use the staggered text effect.
 * @param textTransitionSlotStaggerMillis non-negative text-slot stagger in milliseconds.
 * @param animatedPlaceholder whether the placeholder shimmer animates when motion is allowed.
 * @param pressInAnimationDurationMillis optional non-negative press-down duration override.
 * @param accessibilityLabel explicit accessible name; string labels are inferred when absent.
 * @param accessibilityHint optional localized assistive usage hint.
 * @param accessibilityLongPressLabel optional localized long-press action label.
 * @param onPressIn callback dispatched when a pointer arms this gesture.
 * @param onPressOut callback dispatched after a release or cancellation terminal is claimed.
 * @param onPressedIn callback dispatched synchronously after pressed state is committed.
 * @param onPressedOut callback captured when the release transition begins and dispatched on settle.
 * @param onProgressStart callback dispatched when accepted progress begins.
 * @param onProgressEnd callback captured when progress completion begins and dispatched on settle.
 * @param content optional arbitrary primary content included in intrinsic-width measurement.
 */
@Composable
public fun AwesomeButton(
    modifier: Modifier = Modifier,
    child: String? = null,
    onPress: AwesomeButtonPressCallback? = null,
    onLongPress: (() -> Unit)? = null,
    disabled: Boolean = false,
    width: Dp? = null,
    height: Dp = 52.dp,
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
    textTransition: Boolean = false,
    textTransitionSlotStaggerMillis: Int = DEFAULT_TEXT_TRANSITION_SLOT_STAGGER_MILLIS,
    animatedPlaceholder: Boolean = true,
    pressInAnimationDurationMillis: Int? = null,
    accessibilityLabel: String? = null,
    accessibilityHint: String? = null,
    accessibilityLongPressLabel: String? = null,
    onPressIn: (() -> Unit)? = null,
    onPressOut: (() -> Unit)? = null,
    onPressedIn: (() -> Unit)? = null,
    onPressedOut: (() -> Unit)? = null,
    onProgressStart: (() -> Unit)? = null,
    onProgressEnd: (() -> Unit)? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
): Unit =
    AwesomeButtonImpl(
        modifier = modifier,
        child = child,
        onPress = onPress,
        onLongPress = onLongPress,
        disabled = disabled,
        width = width,
        height = height,
        paddingHorizontal = paddingHorizontal,
        paddingTop = paddingTop,
        paddingBottom = paddingBottom,
        before = before,
        after = after,
        extra = extra,
        stretch = stretch,
        style = style,
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

@Composable
internal fun AwesomeButtonImpl(
    modifier: Modifier = Modifier,
    child: String? = null,
    onPress: AwesomeButtonPressCallback? = null,
    onLongPress: (() -> Unit)? = null,
    disabled: Boolean = false,
    width: Dp? = null,
    height: Dp = 52.dp,
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
    textTransition: Boolean = false,
    textTransitionSlotStaggerMillis: Int = DEFAULT_TEXT_TRANSITION_SLOT_STAGGER_MILLIS,
    animatedPlaceholder: Boolean = true,
    pressInAnimationDurationMillis: Int? = null,
    accessibilityLabel: String? = null,
    accessibilityHint: String? = null,
    accessibilityLongPressLabel: String? = null,
    onPressIn: (() -> Unit)? = null,
    onPressOut: (() -> Unit)? = null,
    onPressedIn: (() -> Unit)? = null,
    onPressedOut: (() -> Unit)? = null,
    onProgressStart: (() -> Unit)? = null,
    onProgressEnd: (() -> Unit)? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
    styleIsResolvedFrame: Boolean = false,
    reduceMotionOverride: Boolean? = null,
) {
    val targetStyle =
        if (styleIsResolvedFrame) {
            resolvedVisualStyle(style ?: AwesomeButtonThemeData.fallbackStyle)
        } else {
            resolvedVisualStyle(AwesomeButtonTheme.current.style.merge(style))
        }
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val normalizedWidth = normalizeOptionalDp(width)
    val normalizedHeight = normalizeRequiredDp(height, 52.dp)
    val normalizedPaddingHorizontal = normalizeOptionalDp(paddingHorizontal)
    val normalizedPaddingTop = normalizeOptionalDp(paddingTop)
    val normalizedPaddingBottom = normalizeOptionalDp(paddingBottom)
    val normalizedActiveOpacity = normalizeOpacity(activeOpacity)
    val normalizedDebounceMillis = debouncedPressTimeMillis.coerceAtLeast(0)
    val normalizedProgressLoadingMillis = progressLoadingTimeMillis.coerceAtLeast(0)
    val normalizedPressInDurationMillis = normalizeOptionalMillis(pressInAnimationDurationMillis)
    val isPlaceholder = child == null && content == null
    val effectiveDisabled = disabled || isPlaceholder
    val scope = rememberCoroutineScope()
    val systemReduceMotion = rememberAwesomeButtonReduceMotion()
    val reduceMotion = reduceMotionOverride ?: systemReduceMotion
    val busyStateDescription = stringResource(R.string.awesome_button_busy_state)
    val defaultLongPressLabel = stringResource(R.string.awesome_button_long_press_action)

    val styleTransitionProgress = remember { Animatable(1f) }
    var styleTransitionSource by remember { mutableStateOf(targetStyle) }
    var styleTransitionTarget by remember { mutableStateOf(targetStyle) }
    var lastStyleTransitionDisabled by remember { mutableStateOf(effectiveDisabled) }
    val resolvedStyle =
        interpolateAwesomeButtonStyle(
            styleTransitionSource,
            styleTransitionTarget,
            styleTransitionProgress.value,
        )
    val contentPaddingHorizontal = normalizedPaddingHorizontal ?: 16.dp
    val contentPaddingTop = normalizedPaddingTop ?: 0.dp
    val contentPaddingBottom = normalizedPaddingBottom ?: 0.dp
    val contentGap = resolvedStyle.contentGap ?: fallback.contentGap!!
    val targetTextStyle =
        TextStyle(
            color = Color.Unspecified,
            fontSize = targetStyle.textSize ?: fallback.textSize!!,
            lineHeight = targetStyle.textLineHeight ?: fallback.textLineHeight!!,
            fontFamily = targetStyle.textFontFamily,
            fontWeight = FontWeight.Bold,
        )
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val accessibilityTextGrowth = density.fontScale > 1f
    val hasCustomContent = content != null || before != null || after != null
    val widthMode =
        when {
            stretch -> ButtonWidthMode.Stretch
            normalizedWidth != null -> ButtonWidthMode.Fixed
            else -> ButtonWidthMode.Auto
        }
    val targetHeightPx = with(density) { normalizedHeight.roundToPx() }
    val targetPaddingHorizontalPx = with(density) { contentPaddingHorizontal.roundToPx() }
    val targetPaddingTopPx = with(density) { contentPaddingTop.roundToPx() }
    val targetPaddingBottomPx = with(density) { contentPaddingBottom.roundToPx() }
    val targetBorderWidthPx = with(density) { (targetStyle.borderWidth ?: fallback.borderWidth!!).roundToPx() }
    val targetSizeSignature =
        SizeTransitionSignature(
            heightPx = targetHeightPx,
            paddingHorizontalPx = targetPaddingHorizontalPx,
            paddingTopPx = targetPaddingTopPx,
            paddingBottomPx = targetPaddingBottomPx,
            style = targetStyle,
        )

    val pressValue = remember { Animatable(0f) }
    val contentTransition = remember { Animatable(1f) }
    val activityTransition = remember { Animatable(0f) }
    val progressOverlayOpacity = remember { Animatable(0f) }
    val progressValue = remember { Animatable(0f) }

    fun measureAutoTextWidthPx(text: String): Int {
        val measuredText =
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = targetTextStyle,
                maxLines = 1,
                softWrap = false,
            )
        return measuredText.size.width + (targetPaddingHorizontalPx * 2) + (targetBorderWidthPx * 2)
    }

    val autoWidthTextEligible =
        widthMode == ButtonWidthMode.Auto &&
            !child.isNullOrEmpty() &&
            !hasCustomContent
    val initialResolvedWidthPx =
        when {
            widthMode == ButtonWidthMode.Fixed -> with(density) { normalizedWidth!!.roundToPx() }
            autoWidthTextEligible -> measureAutoTextWidthPx(child!!)
            else -> null
        }
    val resolvedWidthPx = remember { Animatable((initialResolvedWidthPx ?: 0).toFloat()) }
    val resolvedHeightPx = remember { Animatable(targetHeightPx.toFloat()) }
    val animatedHeight = with(density) { resolvedHeightPx.value.toDp() }
    val raiseAmount = resolvedStyle.raiseAmount ?: fallback.raiseAmount!!

    var busy by remember { mutableStateOf(false) }
    var nextConsumed by remember { mutableStateOf(false) }
    var showProgressVisuals by remember { mutableStateOf(false) }
    var progressTravelJob by remember { mutableStateOf<Job?>(null) }
    var progressContentJob by remember { mutableStateOf<Job?>(null) }
    var progressActivityJob by remember { mutableStateOf<Job?>(null) }
    var progressOverlayJob by remember { mutableStateOf<Job?>(null) }
    var progressDeferredPressJob by remember { mutableStateOf<Job?>(null) }
    var progressCompletionJob by remember { mutableStateOf<Job?>(null) }
    var progressRunId by remember { mutableLongStateOf(0L) }
    var progressHasPhysicalLifecycle by remember { mutableStateOf(true) }
    var progressCompletionCallbackSnapshot by remember { mutableStateOf<(() -> Unit)?>(null) }
    var progressEndCallbackSnapshot by remember { mutableStateOf<(() -> Unit)?>(null) }
    val debounceOwner = remember { AwesomeButtonDebounceOwner() }
    var keyboardArmed by remember { mutableStateOf(false) }
    var keyboardGestureToken by remember { mutableStateOf<Long?>(null) }
    var keyboardPressInJob by remember { mutableStateOf<Job?>(null) }
    var displayedText by remember { mutableStateOf(child) }
    var currentTextTarget by remember { mutableStateOf(child) }
    var contentClipAlignment by remember { mutableStateOf(ContentClipAlignment.Center) }
    var hasExplicitResolvedWidth by remember { mutableStateOf(initialResolvedWidthPx != null) }
    var renderedWidthMode by remember { mutableStateOf<ButtonWidthMode?>(null) }
    var renderedAutoWidthTextEligible by remember { mutableStateOf(autoWidthTextEligible) }
    var renderedTextTransition by remember { mutableStateOf(textTransition) }
    var renderedSizeSignature by remember { mutableStateOf<SizeTransitionSignature?>(null) }
    var releaseGeneration by remember { mutableLongStateOf(0L) }
    var activeReleaseGeneration by remember { mutableStateOf<Long?>(null) }
    var releaseAnimationJob by remember { mutableStateOf<Job?>(null) }
    var releaseSettleJob by remember { mutableStateOf<Job?>(null) }
    var releasePressedOutSnapshot by remember { mutableStateOf<(() -> Unit)?>(null) }
    var terminalGeneration by remember { mutableLongStateOf(0L) }
    var activeTerminalGeneration by remember { mutableStateOf<Long?>(null) }
    var deferredAutoWidthUpdatePending by remember { mutableStateOf(false) }
    var deferredAutoWidthDrainToken by remember { mutableLongStateOf(0L) }
    var mounted by remember { mutableStateOf(true) }
    val gestureOwner = remember { AwesomeButtonGestureOwner() }
    val interactionDependencies =
        rememberUpdatedState(
            AwesomeButtonInteractionDependencies(
                onPress = onPress,
                onLongPress = onLongPress,
                onPressIn = onPressIn,
                onPressOut = onPressOut,
                onPressedIn = onPressedIn,
                onPressedOut = onPressedOut,
                onProgressStart = onProgressStart,
                onProgressEnd = onProgressEnd,
                effectiveDisabled = effectiveDisabled,
                busy = busy,
                progress = progress,
                showProgressBar = showProgressBar,
                progressLoadingTimeMillis = normalizedProgressLoadingMillis,
                debouncedPressTimeMillis = normalizedDebounceMillis,
                pressInAnimationDurationMillis = normalizedPressInDurationMillis,
                style = targetStyle,
                reduceMotion = reduceMotion,
            ),
        )

    SideEffect {
        gestureOwner.observeLongPressAvailability(onLongPress != null)
    }

    LaunchedEffect(targetStyle, effectiveDisabled, reduceMotion) {
        val currentStyle =
            interpolateAwesomeButtonStyle(
                styleTransitionSource,
                styleTransitionTarget,
                styleTransitionProgress.value,
            )
        val shouldAnimate =
            !styleIsResolvedFrame &&
                lastStyleTransitionDisabled == effectiveDisabled &&
                currentStyle != targetStyle
        lastStyleTransitionDisabled = effectiveDisabled

        if (!shouldAnimate || reduceMotion) {
            styleTransitionSource = targetStyle
            styleTransitionTarget = targetStyle
            styleTransitionProgress.snapTo(1f)
            return@LaunchedEffect
        }

        styleTransitionSource = currentStyle
        styleTransitionTarget = targetStyle
        styleTransitionProgress.snapTo(0f)
        styleTransitionProgress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = targetStyle.animationDurationMillis ?: fallback.animationDurationMillis!!,
                    easing = (targetStyle.animationCurve ?: fallback.animationCurve!!).toEasing(),
                ),
        )
    }

    LaunchedEffect(
        child,
        textTransition,
        textTransitionSlotStaggerMillis,
        widthMode,
        normalizedWidth,
        hasCustomContent,
        animateSize,
        targetTextStyle,
        contentPaddingHorizontal,
        contentPaddingTop,
        contentPaddingBottom,
        normalizedHeight,
        targetBorderWidthPx,
        targetSizeSignature,
        deferredAutoWidthDrainToken,
        reduceMotion,
    ) {
        val normalizedSlotStaggerMillis = normalizeTextTransitionSlotStaggerMillis(textTransitionSlotStaggerMillis)
        val shouldAnimateSize = animateSize && !reduceMotion
        val shouldAnimateText = textTransition && !reduceMotion
        val targetText = child
        val previousWidthMode = renderedWidthMode
        val widthBridge = shouldSnapWidthBridge(previousWidthMode, widthMode)
        val currentWidthPx =
            if (!widthBridge && hasExplicitResolvedWidth) {
                resolvedWidthPx.value.roundToInt()
            } else {
                null
            }
        val targetAutoWidthPx =
            if (autoWidthTextEligible) {
                measureAutoTextWidthPx(targetText)
            } else {
                null
            }

        if (shouldDeferReleaseAutoWidthTransition(
                isReleaseActive = activeReleaseGeneration != null,
                previousWidthMode = previousWidthMode,
                nextWidthMode = widthMode,
                currentAutoWidthTextEligible = renderedAutoWidthTextEligible,
                nextAutoWidthTextEligible = autoWidthTextEligible,
                currentTextTransition = renderedTextTransition,
                nextTextTransition = shouldAnimateText,
                nextAnimateSize = shouldAnimateSize,
                sizeSignatureUnchanged = renderedSizeSignature == targetSizeSignature,
                currentText = currentTextTarget,
                nextText = targetText,
                currentWidthPx = currentWidthPx,
                targetWidthPx = targetAutoWidthPx,
            )
        ) {
            deferredAutoWidthUpdatePending = true
            return@LaunchedEffect
        }

        deferredAutoWidthUpdatePending = false
        renderedWidthMode = widthMode
        renderedAutoWidthTextEligible = autoWidthTextEligible
        renderedTextTransition = shouldAnimateText
        renderedSizeSignature = targetSizeSignature

        suspend fun snapWidth(targetWidthPx: Int?) {
            if (targetWidthPx == null) {
                hasExplicitResolvedWidth = false
                return
            }

            hasExplicitResolvedWidth = true
            resolvedWidthPx.snapTo(targetWidthPx.toFloat())
        }

        suspend fun animateWidthTo(
            targetWidthPx: Int,
            durationMillis: Int = SIZE_ANIMATION_DURATION_MILLIS,
        ) {
            hasExplicitResolvedWidth = true
            if (durationMillis <= 0 || abs(resolvedWidthPx.value - targetWidthPx) < 0.5f) {
                resolvedWidthPx.snapTo(targetWidthPx.toFloat())
                return
            }

            resolvedWidthPx.animateTo(
                targetValue = targetWidthPx.toFloat(),
                animationSpec =
                    tween(
                        durationMillis = durationMillis,
                        easing = sizeAnimationEasing,
                    ),
            )
        }

        suspend fun updateHeight() {
            if (widthBridge || !shouldAnimateSize) {
                resolvedHeightPx.snapTo(targetHeightPx.toFloat())
                return
            }

            if (abs(resolvedHeightPx.value - targetHeightPx) >= 0.5f) {
                resolvedHeightPx.animateTo(
                    targetValue = targetHeightPx.toFloat(),
                    animationSpec =
                        tween(
                            durationMillis = SIZE_ANIMATION_DURATION_MILLIS,
                            easing = sizeAnimationEasing,
                        ),
                )
            }
        }

        val heightJob = launch { updateHeight() }

        suspend fun assignText(nextText: String?) {
            currentTextTarget = nextText
            displayedText = nextText
            contentClipAlignment = ContentClipAlignment.Center
        }

        suspend fun runTextTransitionToTarget(
            fromText: String,
            nextText: String,
        ) {
            runFrameTextTransition(
                fromText = fromText,
                targetText = nextText,
                slotStaggerMillis = normalizedSlotStaggerMillis,
                onUpdate = { displayedText = it },
                onComplete = { displayedText = nextText },
            )
        }

        suspend fun syncTextTransitionState() {
            contentClipAlignment = ContentClipAlignment.Center
            when (
                val updatePlan =
                    resolveButtonTextUpdatePlan(
                        textTransitionEnabled = shouldAnimateText,
                        nextText = targetText,
                        currentTarget = currentTextTarget,
                        displayedText = displayedText,
                    )
            ) {
                is ButtonTextUpdatePlan.Assign -> assignText(updatePlan.text)
                ButtonTextUpdatePlan.Keep -> Unit
                is ButtonTextUpdatePlan.Transition -> {
                    currentTextTarget = updatePlan.targetText
                    runTextTransitionToTarget(updatePlan.sourceText, updatePlan.targetText)
                }
            }
        }

        when (widthMode) {
            ButtonWidthMode.Stretch -> {
                snapWidth(null)
                syncTextTransitionState()
            }
            ButtonWidthMode.Fixed -> {
                val fixedWidthPx = with(density) { normalizedWidth!!.roundToPx() }
                if (widthBridge || !shouldAnimateSize || !hasExplicitResolvedWidth) {
                    snapWidth(fixedWidthPx)
                } else {
                    launch { animateWidthTo(fixedWidthPx) }
                }
                syncTextTransitionState()
            }
            ButtonWidthMode.Auto -> {
                val plan =
                    resolveAutoWidthTextUpdatePlan(
                        isEligible = autoWidthTextEligible,
                        targetText = targetText,
                        currentWidthPx = currentWidthPx,
                        targetWidthPx = targetAutoWidthPx,
                        displayedText = displayedText,
                        animateSize = shouldAnimateSize,
                        textTransition = shouldAnimateText,
                        slotStaggerMillis = normalizedSlotStaggerMillis,
                    )

                when (plan) {
                    AutoWidthTextUpdatePlan.FallbackToTextSync -> {
                        snapWidth(null)
                        syncTextTransitionState()
                    }
                    is AutoWidthTextUpdatePlan.Initial -> {
                        currentTextTarget = plan.targetText
                        displayedText = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        snapWidth(plan.targetWidthPx)
                    }
                    is AutoWidthTextUpdatePlan.TextOnly -> {
                        currentTextTarget = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        if (plan.animateText) {
                            runTextTransitionToTarget(plan.sourceText, plan.targetText)
                        } else {
                            displayedText = plan.targetText
                        }
                    }
                    is AutoWidthTextUpdatePlan.GrowFirst -> {
                        currentTextTarget = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        if (plan.animateSize) {
                            val widthDuration =
                                if (plan.animateText) {
                                    plan.timing.widthDurationMillis
                                } else {
                                    SIZE_ANIMATION_DURATION_MILLIS
                                }
                            launch { animateWidthTo(plan.targetWidthPx, widthDuration) }
                        } else {
                            snapWidth(plan.targetWidthPx)
                        }

                        if (plan.animateText) {
                            delay(if (plan.animateSize) plan.timing.textDelayMillis.toLong() else 0L)
                            runTextTransitionToTarget(plan.sourceText, plan.targetText)
                        } else {
                            delay(if (plan.animateSize) SIZE_ANIMATION_DURATION_MILLIS.toLong() else 0L)
                            displayedText = plan.targetText
                        }
                    }
                    is AutoWidthTextUpdatePlan.ShrinkLast -> {
                        currentTextTarget = plan.targetText
                        if (plan.animateText) {
                            contentClipAlignment = ContentClipAlignment.Leading
                            val textJob =
                                launch {
                                    runTextTransitionToTarget(plan.sourceText, plan.targetText)
                                    contentClipAlignment = ContentClipAlignment.Center
                                }
                            val widthJob =
                                launch {
                                    delay(if (plan.animateSize) plan.timing.widthDelayMillis.toLong() else 0L)
                                    if (plan.animateSize) {
                                        animateWidthTo(plan.targetWidthPx, plan.timing.widthDurationMillis)
                                    } else {
                                        snapWidth(plan.targetWidthPx)
                                    }
                                }
                            textJob.join()
                            widthJob.join()
                        } else {
                            contentClipAlignment = ContentClipAlignment.Center
                            displayedText = plan.targetText
                            if (plan.animateSize) {
                                animateWidthTo(plan.targetWidthPx)
                            } else {
                                snapWidth(plan.targetWidthPx)
                            }
                        }
                    }
                }
            }
        }

        heightJob.join()
    }

    fun canStartGesture(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        !dependencies.effectiveDisabled && !dependencies.busy

    fun canDispatchTap(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        canStartGesture(dependencies) && dependencies.onPress != null

    fun canBeginGesture(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        canStartGesture(dependencies) &&
            (activeTerminalGeneration == null || activeReleaseGeneration != null)

    fun cancelProgressJobs() {
        progressTravelJob?.cancel()
        progressTravelJob = null
        progressContentJob?.cancel()
        progressContentJob = null
        progressActivityJob?.cancel()
        progressActivityJob = null
        progressOverlayJob?.cancel()
        progressOverlayJob = null
        progressDeferredPressJob?.cancel()
        progressDeferredPressJob = null
        progressCompletionJob?.cancel()
        progressCompletionJob = null
        progressCompletionCallbackSnapshot = null
        progressEndCallbackSnapshot = null
    }

    suspend fun resetProgressVisualState(unmount: Boolean) {
        contentTransition.snapTo(1f)
        activityTransition.snapTo(0f)
        progressOverlayOpacity.snapTo(0f)
        progressValue.snapTo(0f)
        if (unmount) {
            showProgressVisuals = false
        }
    }

    fun cancelReleaseTracking(cancelAnimation: Boolean = true) {
        releaseGeneration += 1
        activeReleaseGeneration = null
        releasePressedOutSnapshot = null
        releaseSettleJob?.cancel()
        releaseSettleJob = null
        if (cancelAnimation) {
            releaseAnimationJob?.cancel()
            releaseAnimationJob = null
        }
    }

    fun completeReleaseIfCurrent(generation: Long) {
        if (activeReleaseGeneration != generation) return
        val pressedOut = releasePressedOutSnapshot
        activeReleaseGeneration = null
        releasePressedOutSnapshot = null
        releaseSettleJob = null
        if (mounted) {
            pressedOut?.invoke()
        }
        if (!mounted) return
        if (deferredAutoWidthUpdatePending) {
            deferredAutoWidthUpdatePending = false
            deferredAutoWidthDrainToken += 1
        }
    }

    DisposableEffect(Unit) {
        mounted = true
        onDispose {
            mounted = false
            gestureOwner.teardown()
            keyboardPressInJob?.cancel()
            keyboardPressInJob = null
            activeTerminalGeneration = null
            progressRunId += 1
            nextConsumed = true
            cancelProgressJobs()
            cancelReleaseTracking()
        }
    }

    suspend fun preparePressIn(
        token: Long,
        gestureScope: CoroutineScope,
    ): Boolean {
        cancelReleaseTracking()
        interactionDependencies.value.onPressIn?.invoke()
        yield()
        if (
            !mounted ||
            !gestureOwner.isActive(token) ||
            !canStartGesture()
        ) {
            return false
        }
        interactionDependencies.value.onPressedIn?.invoke()
        yield()
        if (
            !mounted ||
            !gestureOwner.isActive(token) ||
            !canStartGesture()
        ) {
            return false
        }
        val styleAtAnimationStart = interactionDependencies.value.style
        gestureScope.launch {
            if (reduceMotion) {
                pressValue.snapTo(1f)
            } else {
                pressValue.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis =
                                resolvePressInDurationMillis(
                                    overrideMillis =
                                        interactionDependencies.value.pressInAnimationDurationMillis,
                                    styleMillis = styleAtAnimationStart.animationDurationMillis,
                                    fallbackMillis = fallback.animationDurationMillis!!,
                                ),
                            easing = (styleAtAnimationStart.animationCurve ?: fallback.animationCurve!!).toEasing(),
                        ),
                )
            }
        }
        return true
    }

    suspend fun releasePressedState(onPressedOutSnapshot: (() -> Unit)?) {
        if (reduceMotion) {
            cancelReleaseTracking()
            pressValue.snapTo(0f)
            if (mounted) onPressedOutSnapshot?.invoke()
            if (mounted && deferredAutoWidthUpdatePending) {
                deferredAutoWidthUpdatePending = false
                deferredAutoWidthDrainToken += 1
            }
            return
        }
        releaseGeneration += 1
        val generation = releaseGeneration
        activeReleaseGeneration = generation
        releasePressedOutSnapshot = onPressedOutSnapshot
        releaseSettleJob?.cancel()
        releaseAnimationJob?.cancel()
        releaseAnimationJob =
            scope.launch {
                pressValue.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        spring(
                            dampingRatio = releaseSpringDampingRatio,
                            stiffness = RELEASE_SPRING_STIFFNESS,
                        ),
                )
            }

        val settleJob =
            scope.launch {
                delay(RELEASE_SPRING_SETTLE_DURATION_MILLIS.toLong())
                completeReleaseIfCurrent(generation)
            }
        releaseSettleJob = settleJob
        settleJob.join()
        if (releaseSettleJob == settleJob) {
            releaseSettleJob = null
        }
    }

    suspend fun snapReleasedState() {
        cancelReleaseTracking()
        pressValue.snapTo(0f)
    }

    fun consumeDebounceWindow(): Boolean =
        debounceOwner.tryAccept(
            nowMillis = SystemClock.uptimeMillis(),
            durationMillis = interactionDependencies.value.debouncedPressTimeMillis,
        )

    fun launchProgressCompletion(runId: Long) {
        val callbackSnapshot = progressCompletionCallbackSnapshot
        val progressEndSnapshot = progressEndCallbackSnapshot
        progressCompletionJob?.cancel()
        progressCompletionJob =
            scope.launch {
                // A completion accepted from inside onPress owns the run immediately, but execution
                // begins only after that consumer callback and its framework event batch unwind.
                yield()
                if (!mounted || progressRunId != runId || !busy) return@launch
                progressTravelJob?.cancel()
                progressTravelJob = null
                if (interactionDependencies.value.reduceMotion) {
                    progressValue.snapTo(1f)
                } else if (progressValue.value < 1f) {
                    progressValue.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = PROGRESS_FILL_COMPLETION_DURATION_MILLIS,
                                easing = progressCompletionEasing,
                            ),
                    )
                }
                if (progressRunId != runId) return@launch

                progressContentJob?.cancel()
                progressActivityJob?.cancel()
                progressOverlayJob?.cancel()

                val restoreJob =
                    launch {
                        if (interactionDependencies.value.reduceMotion) {
                            contentTransition.snapTo(1f)
                        } else {
                            contentTransition.animateTo(
                                targetValue = 1f,
                                animationSpec =
                                    tween(
                                        durationMillis = PROGRESS_SWAP_DURATION_MILLIS,
                                        easing = progressSwapEasing,
                                    ),
                            )
                        }
                    }
                val spinnerJob =
                    launch {
                        if (interactionDependencies.value.reduceMotion) {
                            activityTransition.snapTo(0f)
                        } else {
                            activityTransition.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    tween(
                                        durationMillis = PROGRESS_SWAP_DURATION_MILLIS,
                                        easing = progressSwapEasing,
                                    ),
                            )
                        }
                    }
                val overlayJob =
                    launch {
                        if (!interactionDependencies.value.reduceMotion) {
                            delay(PROGRESS_OVERLAY_FADE_DELAY_MILLIS.toLong())
                        }
                        if (progressRunId != runId) return@launch
                        if (interactionDependencies.value.reduceMotion) {
                            progressOverlayOpacity.snapTo(0f)
                        } else {
                            progressOverlayOpacity.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    tween(
                                        durationMillis = PROGRESS_OVERLAY_FADE_DURATION_MILLIS,
                                        easing = progressCompletionEasing,
                                    ),
                            )
                        }
                    }
                restoreJob.join()
                spinnerJob.join()
                overlayJob.join()
                if (progressRunId != runId) return@launch

                if (progressHasPhysicalLifecycle) {
                    releasePressedState(interactionDependencies.value.onPressedOut)
                } else {
                    snapReleasedState()
                }
                if (progressRunId != runId) return@launch

                showProgressVisuals = false
                busy = false
                resetProgressVisualState(unmount = false)
                progressCompletionCallbackSnapshot = null
                progressEndCallbackSnapshot = null
                callbackSnapshot?.invoke()
                if (!mounted || progressRunId != runId) return@launch
                progressEndSnapshot?.invoke()
                progressCompletionCallbackSnapshot = null
                progressEndCallbackSnapshot = null
                progressCompletionJob = null
            }
    }

    fun completeProgress(
        callback: (() -> Unit)? = null,
        runId: Long = progressRunId,
    ) {
        if (progressRunId != runId || !busy || nextConsumed) return
        nextConsumed = true
        progressCompletionCallbackSnapshot = callback
        progressEndCallbackSnapshot = interactionDependencies.value.onProgressEnd
        launchProgressCompletion(runId)
    }

    fun rollbackProgress(runId: Long) {
        if (progressRunId != runId || !busy || nextConsumed) return
        nextConsumed = true
        val progressEndSnapshot = interactionDependencies.value.onProgressEnd
        progressCompletionJob?.cancel()
        progressCompletionJob =
            scope.launch {
                if (progressHasPhysicalLifecycle) {
                    releasePressedState(interactionDependencies.value.onPressedOut)
                } else {
                    snapReleasedState()
                }
                if (!mounted || progressRunId != runId) return@launch
                progressTravelJob?.cancel()
                progressTravelJob = null
                progressContentJob?.cancel()
                progressContentJob = null
                progressActivityJob?.cancel()
                progressActivityJob = null
                progressOverlayJob?.cancel()
                progressOverlayJob = null
                progressDeferredPressJob?.cancel()
                progressDeferredPressJob = null
                showProgressVisuals = false
                busy = false
                resetProgressVisualState(unmount = false)
                progressEndSnapshot?.invoke()
                progressCompletionCallbackSnapshot = null
                progressEndCallbackSnapshot = null
                progressCompletionJob = null
            }
    }

    suspend fun startProgressFlow(physicalLifecycle: Boolean) {
        val startDependencies = interactionDependencies.value
        if (startDependencies.busy || startDependencies.onPress == null) return
        progressRunId += 1
        val runId = progressRunId
        cancelProgressJobs()
        progressCompletionCallbackSnapshot = null
        progressEndCallbackSnapshot = null
        cancelReleaseTracking()
        busy = true
        progressHasPhysicalLifecycle = physicalLifecycle
        nextConsumed = false
        pressValue.snapTo(if (physicalLifecycle) 1f else 0f)
        showProgressVisuals = true
        resetProgressVisualState(unmount = false)
        if (reduceMotion) progressValue.snapTo(1f)
        progressOverlayOpacity.snapTo(if (startDependencies.showProgressBar) 1f else 0f)
        startDependencies.onProgressStart?.invoke()
        progressContentJob =
            scope.launch {
                if (reduceMotion) {
                    contentTransition.snapTo(0f)
                } else {
                    contentTransition.animateTo(
                        targetValue = 0f,
                        animationSpec =
                            tween(
                                durationMillis = PROGRESS_SWAP_DURATION_MILLIS,
                                easing = progressSwapEasing,
                            ),
                    )
                }
            }
        progressActivityJob =
            scope.launch {
                if (reduceMotion) {
                    activityTransition.snapTo(1f)
                } else {
                    activityTransition.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = PROGRESS_SWAP_DURATION_MILLIS,
                                easing = progressSwapEasing,
                            ),
                    )
                }
            }
        progressTravelJob =
            scope.launch {
                if (!reduceMotion) {
                    progressValue.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = startDependencies.progressLoadingTimeMillis.coerceAtLeast(0),
                                easing = LinearEasing,
                            ),
                    )
                }
            }
        progressDeferredPressJob =
            scope.launch {
                withFrameNanos { }
                if (progressRunId != runId || !busy) return@launch
                val dispatchDependencies = interactionDependencies.value
                val progressPress = dispatchDependencies.onPress
                if (dispatchDependencies.effectiveDisabled || progressPress == null) {
                    rollbackProgress(runId)
                    return@launch
                }
                progressPress(
                    AwesomeButtonNext { callback ->
                        // A synchronous completion on the UI thread claims its callback owners before
                        // the consumer can replace them. Retained handles invoked off-main remain
                        // serialized onto the component scope before touching Compose state.
                        if (Looper.myLooper() == Looper.getMainLooper()) {
                            completeProgress(callback, runId)
                        } else {
                            scope.launch {
                                completeProgress(callback, runId)
                            }
                        }
                    },
                )
                if (progressRunId == runId) {
                    progressDeferredPressJob = null
                }
            }
    }

    fun activateAtomically(): Boolean {
        val dependencies = interactionDependencies.value
        if (!canDispatchTap(dependencies) || !consumeDebounceWindow()) return false
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            val liveDependencies = interactionDependencies.value
            if (!canDispatchTap(liveDependencies)) return@launch
            if (liveDependencies.progress) {
                startProgressFlow(physicalLifecycle = false)
            } else {
                liveDependencies.onPress?.invoke(null)
            }
        }
        return true
    }

    fun activateLongPressAtomically(): Boolean {
        val dependencies = interactionDependencies.value
        val handler = dependencies.onLongPress
        if (!canStartGesture(dependencies) || handler == null) return false
        handler()
        return true
    }

    fun activatePressed(generation: Long) {
        scope.launch {
            try {
                val pressedOutBeforePressOut = interactionDependencies.value.onPressedOut
                interactionDependencies.value.onPressOut?.invoke()
                yield()
                if (!mounted || activeTerminalGeneration != generation) return@launch
                val dispatchDependencies = interactionDependencies.value
                if (!canDispatchTap(dispatchDependencies) || !consumeDebounceWindow()) {
                    releasePressedState(pressedOutBeforePressOut)
                    return@launch
                }
                if (dispatchDependencies.progress) {
                    startProgressFlow(physicalLifecycle = true)
                } else {
                    dispatchDependencies.onPress?.invoke(null)
                    yield()
                    if (!mounted || activeTerminalGeneration != generation) return@launch
                    releasePressedState(pressedOutBeforePressOut)
                }
            } finally {
                if (mounted && activeTerminalGeneration == generation) {
                    activeTerminalGeneration = null
                }
            }
        }
    }

    fun cancelPressed(generation: Long) {
        scope.launch {
            try {
                val pressedOutSnapshot = interactionDependencies.value.onPressedOut
                interactionDependencies.value.onPressOut?.invoke()
                yield()
                if (!mounted || activeTerminalGeneration != generation) return@launch
                releasePressedState(pressedOutSnapshot)
            } finally {
                if (mounted && activeTerminalGeneration == generation) {
                    activeTerminalGeneration = null
                }
            }
        }
    }

    fun dispatchGestureOutcome(outcome: AwesomeButtonGestureOutcome) {
        when (outcome) {
            AwesomeButtonGestureOutcome.Activate -> {
                terminalGeneration += 1
                activeTerminalGeneration = terminalGeneration
                activatePressed(terminalGeneration)
            }
            AwesomeButtonGestureOutcome.LongPressCleanup,
            AwesomeButtonGestureOutcome.Cancel,
            -> {
                terminalGeneration += 1
                activeTerminalGeneration = terminalGeneration
                cancelPressed(terminalGeneration)
            }
            AwesomeButtonGestureOutcome.Ignore -> Unit
        }
    }

    LaunchedEffect(effectiveDisabled, busy) {
        if (effectiveDisabled || busy) {
            keyboardArmed = false
            keyboardGestureToken = null
            keyboardPressInJob?.cancel()
            keyboardPressInJob = null
            val outcome = gestureOwner.cancelActive()
            if (outcome == AwesomeButtonGestureOutcome.Ignore) {
                if (busy && effectiveDisabled && !nextConsumed) {
                    rollbackProgress(progressRunId)
                } else if (
                    effectiveDisabled &&
                    !busy &&
                    activeTerminalGeneration == null &&
                    activeReleaseGeneration == null
                ) {
                    snapReleasedState()
                }
            } else {
                dispatchGestureOutcome(outcome)
            }
        }
    }

    LaunchedEffect(reduceMotion, busy, showProgressBar) {
        if (busy && !nextConsumed) {
            progressOverlayOpacity.snapTo(if (showProgressBar) 1f else 0f)
            if (reduceMotion) {
                progressTravelJob?.cancel()
                progressTravelJob = null
                progressContentJob?.cancel()
                progressContentJob = null
                progressActivityJob?.cancel()
                progressActivityJob = null
                progressOverlayJob?.cancel()
                progressOverlayJob = null
                progressValue.snapTo(1f)
                contentTransition.snapTo(0f)
                activityTransition.snapTo(1f)
            }
        }

        if (!reduceMotion) return@LaunchedEffect

        val release = activeReleaseGeneration
        if (release != null) {
            releaseAnimationJob?.cancel()
            releaseAnimationJob = null
            releaseSettleJob?.cancel()
            releaseSettleJob = null
            pressValue.snapTo(0f)
            completeReleaseIfCurrent(release)
        } else if (gestureOwner.hasActiveGesture || keyboardArmed || (busy && progressHasPhysicalLifecycle)) {
            pressValue.snapTo(1f)
        }

        if (busy && nextConsumed && release == null) {
            // Restart only the component-owned visual completion. The accepted consumer
            // callbacks remain snapshotted and are still dispatched exactly once.
            launchProgressCompletion(progressRunId)
        }
    }

    val interactionModifier =
        Modifier
            .focusable(enabled = !effectiveDisabled && !busy)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused && keyboardArmed) {
                    keyboardArmed = false
                    val token = keyboardGestureToken
                    keyboardGestureToken = null
                    keyboardPressInJob?.cancel()
                    keyboardPressInJob = null
                    if (token != null) {
                        dispatchGestureOutcome(gestureOwner.finish(token = token, inside = false))
                    }
                }
            }.onKeyEvent { event ->
                val isActivationKey = event.key == Key.Enter || event.key == Key.Spacebar
                if (!isActivationKey) return@onKeyEvent false
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        if (!keyboardArmed && canBeginGesture()) {
                            val token =
                                gestureOwner.begin(
                                    longPressEligible = interactionDependencies.value.onLongPress != null,
                                ) ?: return@onKeyEvent true
                            keyboardArmed = true
                            keyboardGestureToken = token
                            keyboardPressInJob =
                                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                                    if (preparePressIn(token, scope)) {
                                        val outcome = gestureOwner.markPressInReady(token)
                                        if (!keyboardArmed && keyboardGestureToken == token) {
                                            dispatchGestureOutcome(outcome)
                                            keyboardGestureToken = null
                                            keyboardPressInJob = null
                                        }
                                    }
                                }
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        if (keyboardArmed) {
                            keyboardArmed = false
                            val token = keyboardGestureToken
                            if (token != null) {
                                val outcome = gestureOwner.finish(token = token, inside = true)
                                dispatchGestureOutcome(outcome)
                                if (outcome != AwesomeButtonGestureOutcome.Ignore) {
                                    keyboardGestureToken = null
                                    keyboardPressInJob = null
                                }
                            }
                        }
                        true
                    }
                    else -> false
                }
            }.pointerInput(Unit) {
                val pointerCoroutineScope = CoroutineScope(currentCoroutineContext())
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downDependencies = interactionDependencies.value
                    if (!canBeginGesture(downDependencies)) return@awaitEachGesture

                    var pointerId: PointerId = down.id
                    var endedInside = false
                    val token =
                        gestureOwner.begin(
                            longPressEligible = downDependencies.onLongPress != null,
                        ) ?: return@awaitEachGesture
                    pointerCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        if (preparePressIn(token, pointerCoroutineScope)) {
                            dispatchGestureOutcome(gestureOwner.markPressInReady(token))
                        }
                    }
                    val longPressJob =
                        if (downDependencies.onLongPress != null) {
                            pointerCoroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                val liveDependencies = interactionDependencies.value
                                val liveHandler = liveDependencies.onLongPress
                                if (
                                    canStartGesture(liveDependencies) &&
                                    liveHandler != null &&
                                    gestureOwner.tryDispatchLongPress(
                                        token = token,
                                        hasHandler = true,
                                    )
                                ) {
                                    liveHandler()
                                }
                            }
                        } else {
                            null
                        }

                    try {
                        while (gestureOwner.isActive(token)) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            val inside =
                                change.position.x >= 0f &&
                                    change.position.y >= 0f &&
                                    change.position.x <= size.width &&
                                    change.position.y <= size.height
                            if (!inside) {
                                break
                            }
                            if (!change.pressed) {
                                endedInside = true
                                break
                            }
                            change.consume()
                            pointerId = change.id
                        }
                    } finally {
                        longPressJob?.cancel()
                    }

                    dispatchGestureOutcome(gestureOwner.finish(token, inside = endedInside))
                }
            }.semantics(mergeDescendants = true) {
                role = Role.Button
                if (accessibilityLabel != null) {
                    contentDescription = accessibilityLabel
                }
                if (busy) {
                    stateDescription = busyStateDescription
                }
                if (effectiveDisabled) {
                    disabled()
                }
                if (isPlaceholder && accessibilityLabel == null) {
                    hideFromAccessibility()
                }
                if (!effectiveDisabled && !busy && onPress != null) {
                    onClick(label = accessibilityHint) {
                        activateAtomically()
                    }
                }
                if (!effectiveDisabled && !busy && onLongPress != null) {
                    onLongClick(
                        label = accessibilityLongPressLabel ?: defaultLongPressLabel,
                    ) {
                        activateLongPressAtomically()
                    }
                }
            }

    val visualPressValue = clampedVisualPressProgress(pressValue.value)
    val renderedContentOpacity =
        if (progress) {
            contentTransition.value.coerceIn(0f, 1f)
        } else {
            1f - ((1f - normalizedActiveOpacity) * visualPressValue)
        }

    AutoWidthButtonLayout(
        modifier =
            modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .then(interactionModifier)
                .testTag("AwesomeButton"),
        resolvedWidthPx = if (hasExplicitResolvedWidth) resolvedWidthPx.value else null,
        minimumFaceHeight = animatedHeight,
        raiseAmount = raiseAmount,
        pressValue = pressValue.value,
        stretch = stretch,
        contentClipShape = resolvedStyle.toShape(),
        chrome = { layoutGeometry ->
            ButtonLayers(
                geometry = layoutGeometry,
                resolvedStyle = resolvedStyle,
                disabled = disabled,
                isPlaceholder = isPlaceholder,
                progress = progress,
                showProgressBar = showProgressBar,
                busy = busy,
                showProgressVisuals = showProgressVisuals,
                pressValue = pressValue.value,
                progressOverlayAlpha = progressOverlayOpacity.value,
                progressValue = progressValue.value,
                paddingHorizontal = contentPaddingHorizontal,
                paddingTop = contentPaddingTop,
                paddingBottom = contentPaddingBottom,
                extra = extra,
                animatedPlaceholder = animatedPlaceholder,
                reduceMotion = reduceMotion,
            )
        },
        content =
            if (isPlaceholder) {
                null
            } else {
                {
                    ButtonContent(
                        modifier = Modifier,
                        resolvedStyle = resolvedStyle,
                        disabled = disabled,
                        alpha = renderedContentOpacity,
                        scale = if (progress) contentTransition.value else 1f,
                        paddingHorizontal = contentPaddingHorizontal,
                        paddingTop = contentPaddingTop,
                        paddingBottom = contentPaddingBottom,
                        contentGap = contentGap,
                        child = displayedText,
                        before = before,
                        after = after,
                        content = content,
                        contentClipAlignment = contentClipAlignment,
                        hideMainContentSemantics = accessibilityLabel != null,
                        allowTextWrap = accessibilityTextGrowth,
                    )
                }
            },
        activity =
            if (showProgressVisuals) {
                {
                    ButtonActivityOverlay(
                        activityAlpha = activityTransition.value,
                        color = resolvedStyle.activityColor ?: fallback.activityColor!!,
                        reduceMotion = reduceMotion,
                    )
                }
            } else {
                null
            },
    )
}

internal const val SIZE_ANIMATION_DURATION_MILLIS = 175
internal val sizeAnimationEasing = CubicBezierEasing(0.6f, 0.3f, 0.35f, 0.9f)

private data class SizeTransitionSignature(
    val heightPx: Int,
    val paddingHorizontalPx: Int,
    val paddingTopPx: Int,
    val paddingBottomPx: Int,
    val style: AwesomeButtonStyle,
)

private data class AwesomeButtonInteractionDependencies(
    val onPress: AwesomeButtonPressCallback?,
    val onLongPress: (() -> Unit)?,
    val onPressIn: (() -> Unit)?,
    val onPressOut: (() -> Unit)?,
    val onPressedIn: (() -> Unit)?,
    val onPressedOut: (() -> Unit)?,
    val onProgressStart: (() -> Unit)?,
    val onProgressEnd: (() -> Unit)?,
    val effectiveDisabled: Boolean,
    val busy: Boolean,
    val progress: Boolean,
    val showProgressBar: Boolean,
    val progressLoadingTimeMillis: Int,
    val debouncedPressTimeMillis: Long,
    val pressInAnimationDurationMillis: Int?,
    val style: AwesomeButtonStyle,
    val reduceMotion: Boolean,
)
