package dev.caferati.awesomebutton

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    sizeTargetStyle: AwesomeButtonStyle? = null,
    reduceMotionOverride: Boolean? = null,
) {
    val scope = rememberCoroutineScope()
    val systemReduceMotion = rememberAwesomeButtonReduceMotion()
    val reduceMotion = reduceMotionOverride ?: systemReduceMotion
    val busyStateDescription = stringResource(R.string.awesome_button_busy_state)
    val defaultLongPressLabel = stringResource(R.string.awesome_button_long_press_action)
    val density = LocalDensity.current
    val presentation =
        resolveAwesomeButtonPresentation(
            AwesomeButtonPresentationInput(
                themeStyle = AwesomeButtonTheme.current.style,
                style = style,
                styleIsResolvedFrame = styleIsResolvedFrame,
                sizeTargetStyle = sizeTargetStyle,
                childPresent = child != null,
                customContentPresent = content != null,
                beforePresent = before != null,
                afterPresent = after != null,
                disabled = disabled,
                width = width,
                height = height,
                paddingHorizontal = paddingHorizontal,
                paddingTop = paddingTop,
                paddingBottom = paddingBottom,
                stretch = stretch,
                activeOpacity = activeOpacity,
                debouncedPressTimeMillis = debouncedPressTimeMillis,
                progressLoadingTimeMillis = progressLoadingTimeMillis,
                pressInAnimationDurationMillis = pressInAnimationDurationMillis,
                reduceMotion = reduceMotion,
                busyStateDescription = busyStateDescription,
                defaultLongPressLabel = defaultLongPressLabel,
                density = density,
                fontScale = density.fontScale,
            ),
        )
    val targetStyle = presentation.targetStyle
    val fallback = presentation.fallbackStyle
    val normalizedWidth = presentation.normalizedWidth
    val normalizedHeight = presentation.normalizedHeight
    val normalizedActiveOpacity = presentation.activeOpacity
    val normalizedDebounceMillis = presentation.debounceMillis
    val normalizedProgressLoadingMillis = presentation.progressLoadingMillis
    val normalizedPressInDurationMillis = presentation.pressInDurationMillis
    val isPlaceholder = presentation.isPlaceholder
    val effectiveDisabled = presentation.effectiveDisabled

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
    val contentPaddingHorizontal = presentation.paddingHorizontal
    val contentPaddingTop = presentation.paddingTop
    val contentPaddingBottom = presentation.paddingBottom
    val contentGap = resolvedStyle.contentGap ?: fallback.contentGap!!
    val targetTextStyle = presentation.targetTextStyle
    val textMeasurer = rememberTextMeasurer()
    val accessibilityTextGrowth = presentation.accessibilityTextGrowth
    val hasCustomContent = presentation.hasCustomContent
    val widthMode = presentation.widthMode
    val targetHeightPx = presentation.targetHeightPx
    val targetPaddingHorizontalPx = presentation.targetPaddingHorizontalPx
    val targetPaddingTopPx = presentation.targetPaddingTopPx
    val targetPaddingBottomPx = presentation.targetPaddingBottomPx
    val targetBorderWidthPx = presentation.targetBorderWidthPx
    val targetSizeSignature = presentation.sizeSignature

    val releaseOwner = remember { AwesomeButtonReleaseOwner(scope) }
    val progressOwner = remember { AwesomeButtonProgressOwner(scope) }
    val progressCommands = remember { AwesomeButtonProgressCommandPort() }

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
    val sizeTextOwner =
        remember {
            AwesomeButtonSizeTextOwner(
                scope = scope,
                initialResolvedWidthPx = initialResolvedWidthPx,
                initialHeightPx = targetHeightPx,
                initialText = child,
                initialAutoWidthTextEligible = autoWidthTextEligible,
            )
        }
    val animatedHeight = with(density) { sizeTextOwner.resolvedHeightPx.value.toDp() }
    val raiseAmount = resolvedStyle.raiseAmount ?: fallback.raiseAmount!!

    val busy = progressOwner.busy
    val debounceOwner = remember { AwesomeButtonDebounceOwner() }
    var keyboardArmed by remember { mutableStateOf(false) }
    var keyboardGestureToken by remember { mutableStateOf<Long?>(null) }
    var keyboardPressInJob by remember { mutableStateOf<Job?>(null) }
    var terminalGeneration by remember { mutableLongStateOf(0L) }
    var activeTerminalGeneration by remember { mutableStateOf<Long?>(null) }
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
                busy = progressOwner.busy,
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

    val targetAutoWidthPx =
        if (autoWidthTextEligible) {
            measureAutoTextWidthPx(child!!)
        } else {
            null
        }
    val fixedWidthPx =
        if (widthMode == ButtonWidthMode.Fixed) {
            with(density) { normalizedWidth!!.roundToPx() }
        } else {
            null
        }
    val sizeTextInput =
        AwesomeButtonSizeTextInput(
            widthMode = widthMode,
            fixedWidthPx = fixedWidthPx,
            targetHeightPx = targetHeightPx,
            autoWidthTextEligible = autoWidthTextEligible,
            targetAutoWidthPx = targetAutoWidthPx,
            targetText = child,
            animateSize = animateSize,
            textTransition = textTransition,
            textTransitionSlotStaggerMillis = textTransitionSlotStaggerMillis,
            sizeSignature = targetSizeSignature,
            releaseActive = releaseOwner.activeGeneration != null,
            reduceMotion = reduceMotion,
        )
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
        reduceMotion,
    ) {
        sizeTextOwner.reconcile(sizeTextInput)
    }

    fun canStartGesture(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        !dependencies.effectiveDisabled && !dependencies.busy

    fun canDispatchTap(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        canStartGesture(dependencies) && dependencies.onPress != null

    fun canBeginGesture(dependencies: AwesomeButtonInteractionDependencies = interactionDependencies.value) =
        canStartGesture(dependencies) &&
            (activeTerminalGeneration == null || releaseOwner.activeGeneration != null)

    suspend fun dispatchReleaseSettlement(settlement: AwesomeButtonReleaseSettlement?): Boolean {
        settlement ?: return false
        if (!mounted) return false
        settlement.onPressedOut?.let { callback ->
            callback()
            withFrameNanos { }
        }
        if (!mounted) return false
        sizeTextOwner.settleDeferredAfterRelease()
        return true
    }

    DisposableEffect(Unit) {
        mounted = true
        onDispose {
            mounted = false
            gestureOwner.teardown()
            keyboardPressInJob?.cancel()
            keyboardPressInJob = null
            activeTerminalGeneration = null
            progressOwner.cancel(unmount = true)
            releaseOwner.cancel()
            sizeTextOwner.cancel()
        }
    }

    suspend fun preparePressIn(
        token: Long,
        gestureScope: CoroutineScope,
    ): Boolean {
        releaseOwner.invalidate()
        interactionDependencies.value.onPressIn?.let { callback ->
            callback()
            withFrameNanos { }
        }
        if (
            !mounted ||
            !gestureOwner.isActive(token) ||
            !canStartGesture()
        ) {
            return false
        }
        interactionDependencies.value.onPressedIn?.let { callback ->
            callback()
            withFrameNanos { }
        }
        if (
            !mounted ||
            !gestureOwner.isActive(token) ||
            !canStartGesture()
        ) {
            return false
        }
        val animationDependencies = interactionDependencies.value
        val styleAtAnimationStart = animationDependencies.style
        gestureScope.launch {
            releaseOwner.press(
                reduceMotion = animationDependencies.reduceMotion,
                durationMillis =
                    resolvePressInDurationMillis(
                        overrideMillis = animationDependencies.pressInAnimationDurationMillis,
                        styleMillis = styleAtAnimationStart.animationDurationMillis,
                        fallbackMillis = fallback.animationDurationMillis!!,
                    ),
                style = styleAtAnimationStart,
                fallback = fallback,
            )
        }
        return true
    }

    suspend fun releasePressedState(onPressedOutSnapshot: (() -> Unit)?): Boolean {
        val settlement =
            releaseOwner.release(
                onPressedOut = onPressedOutSnapshot,
                reduceMotion = interactionDependencies.value.reduceMotion,
            )
        return dispatchReleaseSettlement(settlement)
    }

    suspend fun snapReleasedState() {
        releaseOwner.snapReleased()
    }

    fun consumeDebounceWindow(): Boolean =
        debounceOwner.tryAccept(
            nowMillis = SystemClock.uptimeMillis(),
            durationMillis = interactionDependencies.value.debouncedPressTimeMillis,
        )

    fun startProgressFlow(physicalLifecycle: Boolean) {
        progressOwner.start(
            physicalLifecycle = physicalLifecycle,
            commands = progressCommands,
            snapPressedState = { isPhysical ->
                releaseOwner.snapPressed(isPhysical)
            },
        )
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
                interactionDependencies.value.onPressOut?.let { callback ->
                    callback()
                    withFrameNanos { }
                }
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
                    withFrameNanos { }
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
        val pressOutSnapshot = interactionDependencies.value.onPressOut
        val pressedOutSnapshot = interactionDependencies.value.onPressedOut
        scope.launch {
            try {
                withFrameNanos { }
                if (!mounted || activeTerminalGeneration != generation) return@launch
                pressOutSnapshot?.let { callback ->
                    callback()
                    withFrameNanos { }
                }
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

    SideEffect {
        progressCommands.update(
            object : AwesomeButtonProgressCommandBindings {
                override fun isMounted(): Boolean = mounted

                override fun currentDependencies(): AwesomeButtonProgressDependencies {
                    val dependencies = interactionDependencies.value
                    return AwesomeButtonProgressDependencies(
                        onPress = dependencies.onPress,
                        onProgressStart = dependencies.onProgressStart,
                        onProgressEnd = dependencies.onProgressEnd,
                        onPressedOut = dependencies.onPressedOut,
                        effectiveDisabled = dependencies.effectiveDisabled,
                        showProgressBar = dependencies.showProgressBar,
                        progressLoadingTimeMillis = dependencies.progressLoadingTimeMillis,
                        reduceMotion = dependencies.reduceMotion,
                    )
                }

                override suspend fun requestRelease(
                    physicalLifecycle: Boolean,
                    onPressedOutSnapshot: (() -> Unit)?,
                ): Boolean {
                    if (!mounted) return false
                    return if (physicalLifecycle) {
                        releasePressedState(onPressedOutSnapshot)
                    } else {
                        snapReleasedState()
                        mounted
                    }
                }
            },
        )
    }

    LaunchedEffect(effectiveDisabled, busy) {
        if (effectiveDisabled || busy) {
            keyboardArmed = false
            keyboardGestureToken = null
            keyboardPressInJob?.cancel()
            keyboardPressInJob = null
            val outcome = gestureOwner.cancelActive()
            if (outcome == AwesomeButtonGestureOutcome.Ignore) {
                if (busy && effectiveDisabled && !progressOwner.nextConsumed) {
                    progressOwner.rollback()
                } else if (
                    effectiveDisabled &&
                    !busy &&
                    activeTerminalGeneration == null &&
                    releaseOwner.activeGeneration == null
                ) {
                    snapReleasedState()
                }
            } else {
                dispatchGestureOutcome(outcome)
            }
        }
    }

    LaunchedEffect(reduceMotion, busy, showProgressBar) {
        progressOwner.applyMotionPolicy(
            reduceMotion = reduceMotion,
            showProgressBar = showProgressBar,
            releaseActive = releaseOwner.activeGeneration != null,
        )

        if (!reduceMotion) return@LaunchedEffect

        if (releaseOwner.activeGeneration != null) {
            dispatchReleaseSettlement(releaseOwner.settleImmediately())
        } else if (gestureOwner.hasActiveGesture || keyboardArmed || (busy && progressOwner.hasPhysicalLifecycle)) {
            releaseOwner.snapPressed(true)
        }
    }

    val activateAtomicallyCommand = { activateAtomically() }
    val activateLongPressAtomicallyCommand = { activateLongPressAtomically() }
    val interactionCommands = remember { AwesomeButtonInteractionCommandPort() }
    SideEffect {
        interactionCommands.update(
            object : AwesomeButtonInteractionCommandBindings {
                override fun isMounted(): Boolean = mounted

                override fun canBeginGesture(): Boolean = canBeginGesture()

                override fun hasLongPressHandler(): Boolean = interactionDependencies.value.onLongPress != null

                override fun beginGesture(): Long? =
                    gestureOwner.begin(
                        longPressEligible = interactionDependencies.value.onLongPress != null,
                    )

                override suspend fun preparePressIn(
                    token: Long,
                    gestureScope: CoroutineScope,
                ) {
                    if (preparePressIn(token, gestureScope)) {
                        dispatchGestureOutcome(gestureOwner.markPressInReady(token))
                    }
                }

                override suspend fun dispatchLongPress(token: Long) {
                    val dependencies = interactionDependencies.value
                    val handler = dependencies.onLongPress
                    if (
                        canStartGesture(dependencies) &&
                        handler != null &&
                        gestureOwner.tryDispatchLongPress(token = token, hasHandler = true)
                    ) {
                        handler()
                    }
                }

                override fun isGestureActive(token: Long): Boolean = gestureOwner.isActive(token)

                override fun finishGesture(
                    token: Long,
                    inside: Boolean,
                ) {
                    dispatchGestureOutcome(gestureOwner.finish(token, inside))
                }

                override fun onFocusLost() {
                    if (!keyboardArmed) return
                    keyboardArmed = false
                    val token = keyboardGestureToken
                    keyboardGestureToken = null
                    keyboardPressInJob?.cancel()
                    keyboardPressInJob = null
                    if (token != null) {
                        dispatchGestureOutcome(gestureOwner.finish(token = token, inside = false))
                    }
                }

                override fun onActivationKeyDown() {
                    if (keyboardArmed || !canBeginGesture()) return
                    val token =
                        gestureOwner.begin(
                            longPressEligible = interactionDependencies.value.onLongPress != null,
                        ) ?: return
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

                override fun onActivationKeyUp() {
                    if (!keyboardArmed) return
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

                override fun activateAtomically(): Boolean = activateAtomicallyCommand()

                override fun activateLongPressAtomically(): Boolean = activateLongPressAtomicallyCommand()
            },
        )
    }
    val interactionModifier =
        Modifier.awesomeButtonInteraction(
            commands = interactionCommands,
            semantics =
                AwesomeButtonInteractionSemantics(
                    effectiveDisabled = effectiveDisabled,
                    busy = busy,
                    isPlaceholder = isPlaceholder,
                    hasPressHandler = onPress != null,
                    hasLongPressHandler = onLongPress != null,
                    accessibilityLabel = accessibilityLabel,
                    accessibilityHint = accessibilityHint,
                    accessibilityLongPressLabel = accessibilityLongPressLabel,
                    busyStateDescription = presentation.busyStateDescription,
                    defaultLongPressLabel = presentation.defaultLongPressLabel,
                ),
        )

    val visualPressValue = clampedVisualPressProgress(releaseOwner.pressValue.value)
    val renderedContentOpacity =
        if (progress) {
            progressOwner.contentTransition.value.coerceIn(0f, 1f)
        } else {
            1f - ((1f - normalizedActiveOpacity) * visualPressValue)
        }

    AutoWidthButtonLayout(
        modifier =
            modifier
                .then(interactionModifier)
                .testTag("AwesomeButton"),
        resolvedWidthPx =
            if (sizeTextOwner.hasExplicitResolvedWidth) {
                sizeTextOwner.resolvedWidthPx.value
            } else {
                null
            },
        minimumFaceHeight = animatedHeight,
        raiseAmount = raiseAmount,
        pressValue = releaseOwner.pressValue.value,
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
                showProgressVisuals = progressOwner.showProgressVisuals,
                pressValue = releaseOwner.pressValue.value,
                progressOverlayAlpha = progressOwner.overlayOpacity.value,
                progressValue = progressOwner.progressValue.value,
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
                        scale = if (progress) progressOwner.contentTransition.value else 1f,
                        paddingHorizontal = contentPaddingHorizontal,
                        paddingTop = contentPaddingTop,
                        paddingBottom = contentPaddingBottom,
                        contentGap = contentGap,
                        child = sizeTextOwner.displayedText,
                        before = before,
                        after = after,
                        content = content,
                        contentClipAlignment = sizeTextOwner.contentClipAlignment,
                        hideMainContentSemantics = accessibilityLabel != null,
                        allowTextWrap = accessibilityTextGrowth,
                    )
                }
            },
        activity =
            if (progressOwner.showProgressVisuals) {
                {
                    ButtonActivityOverlay(
                        activityAlpha = progressOwner.activityTransition.value,
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
