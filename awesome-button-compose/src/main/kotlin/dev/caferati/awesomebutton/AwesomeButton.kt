package dev.caferati.awesomebutton

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AwesomeButton(
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
    textTransitionSlotStaggerMillis: Int = DefaultTextTransitionSlotStaggerMillis,
    animatedPlaceholder: Boolean = true,
    onPressIn: (() -> Unit)? = null,
    onPressOut: (() -> Unit)? = null,
    onPressedIn: (() -> Unit)? = null,
    onPressedOut: (() -> Unit)? = null,
    onProgressStart: (() -> Unit)? = null,
    onProgressEnd: (() -> Unit)? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val targetStyle = resolvedVisualStyle(AwesomeButtonTheme.current.style.merge(style))
    val fallback = AwesomeButtonThemeData.fallbackStyle
    val isPlaceholder = child == null && content == null
    val effectiveDisabled = disabled || isPlaceholder
    val scope = rememberCoroutineScope()

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
    val contentPaddingHorizontal = paddingHorizontal ?: 16.dp
    val contentPaddingTop = paddingTop ?: 0.dp
    val contentPaddingBottom = paddingBottom ?: 0.dp
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
    val hasCustomContent = content != null || before != null || after != null || extra != null
    val widthMode =
        when {
            stretch -> ButtonWidthMode.Stretch
            width != null -> ButtonWidthMode.Fixed
            else -> ButtonWidthMode.Auto
        }
    val targetHeightPx = with(density) { height.roundToPx() }
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
            widthMode == ButtonWidthMode.Fixed -> with(density) { width!!.roundToPx() }
            autoWidthTextEligible -> measureAutoTextWidthPx(child!!)
            else -> null
        }
    val resolvedWidthPx = remember { Animatable((initialResolvedWidthPx ?: 0).toFloat()) }
    val resolvedHeightPx = remember { Animatable(targetHeightPx.toFloat()) }
    val animatedHeight = with(density) { resolvedHeightPx.value.toDp() }
    val raiseAmount = resolvedStyle.raiseAmount ?: fallback.raiseAmount!!
    val geometry = AwesomeButtonGeometry(animatedHeight, raiseAmount)

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
    var lastAcceptedPressAt by remember { mutableLongStateOf(0L) }
    var keyboardArmed by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf(child) }
    var currentTextTarget by remember { mutableStateOf(child) }
    var measurementText by remember { mutableStateOf(child) }
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
    var deferredAutoWidthUpdatePending by remember { mutableStateOf(false) }
    var deferredAutoWidthDrainToken by remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetStyle, effectiveDisabled) {
        val currentStyle =
            interpolateAwesomeButtonStyle(
                styleTransitionSource,
                styleTransitionTarget,
                styleTransitionProgress.value,
            )
        val shouldAnimate =
            lastStyleTransitionDisabled == effectiveDisabled && currentStyle != targetStyle
        lastStyleTransitionDisabled = effectiveDisabled

        if (!shouldAnimate) {
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
            animationSpec = tween(
                durationMillis = StyleTransitionDurationMillis,
                easing = (targetStyle.animationCurve ?: fallback.animationCurve!!).toEasing(),
            ),
        )
    }

    LaunchedEffect(
        child,
        textTransition,
        textTransitionSlotStaggerMillis,
        widthMode,
        width,
        hasCustomContent,
        animateSize,
        targetTextStyle,
        contentPaddingHorizontal,
        contentPaddingTop,
        contentPaddingBottom,
        height,
        targetBorderWidthPx,
        targetSizeSignature,
        deferredAutoWidthDrainToken,
    ) {
        val normalizedSlotStaggerMillis = normalizeTextTransitionSlotStaggerMillis(textTransitionSlotStaggerMillis)
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
                nextTextTransition = textTransition,
                nextAnimateSize = animateSize,
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
        renderedTextTransition = textTransition
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
            durationMillis: Int = SizeAnimationDurationMillis,
        ) {
            hasExplicitResolvedWidth = true
            if (durationMillis <= 0 || abs(resolvedWidthPx.value - targetWidthPx) < 0.5f) {
                resolvedWidthPx.snapTo(targetWidthPx.toFloat())
                return
            }

            resolvedWidthPx.animateTo(
                targetValue = targetWidthPx.toFloat(),
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = SizeAnimationEasing,
                ),
            )
        }

        suspend fun updateHeight() {
            if (widthBridge || !animateSize) {
                resolvedHeightPx.snapTo(targetHeightPx.toFloat())
                return
            }

            if (abs(resolvedHeightPx.value - targetHeightPx) >= 0.5f) {
                resolvedHeightPx.animateTo(
                    targetValue = targetHeightPx.toFloat(),
                    animationSpec = tween(
                        durationMillis = SizeAnimationDurationMillis,
                        easing = SizeAnimationEasing,
                    ),
                )
            }
        }

        val heightJob = launch { updateHeight() }

        suspend fun assignText(nextText: String?) {
            currentTextTarget = nextText
            measurementText = nextText
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
            when (val updatePlan =
                resolveButtonTextUpdatePlan(
                    textTransitionEnabled = textTransition,
                    nextText = targetText,
                    currentTarget = currentTextTarget,
                    displayedText = displayedText,
                )
            ) {
                is ButtonTextUpdatePlan.Assign -> assignText(updatePlan.text)
                ButtonTextUpdatePlan.Keep -> {
                    if (widthMode != ButtonWidthMode.Auto || !autoWidthTextEligible) {
                        measurementText = targetText
                    }
                }
                is ButtonTextUpdatePlan.Transition -> {
                    currentTextTarget = updatePlan.targetText
                    measurementText = updatePlan.targetText
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
                val fixedWidthPx = with(density) { width!!.roundToPx() }
                if (widthBridge || !animateSize || !hasExplicitResolvedWidth) {
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
                        animateSize = animateSize,
                        textTransition = textTransition,
                        slotStaggerMillis = normalizedSlotStaggerMillis,
                    )

                when (plan) {
                    AutoWidthTextUpdatePlan.FallbackToTextSync -> {
                        snapWidth(null)
                        syncTextTransitionState()
                    }
                    is AutoWidthTextUpdatePlan.Initial -> {
                        currentTextTarget = plan.targetText
                        measurementText = plan.targetText
                        displayedText = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        snapWidth(plan.targetWidthPx)
                    }
                    is AutoWidthTextUpdatePlan.TextOnly -> {
                        currentTextTarget = plan.targetText
                        measurementText = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        if (plan.animateText) {
                            runTextTransitionToTarget(plan.sourceText, plan.targetText)
                        } else {
                            displayedText = plan.targetText
                        }
                    }
                    is AutoWidthTextUpdatePlan.GrowFirst -> {
                        currentTextTarget = plan.targetText
                        measurementText = plan.targetText
                        contentClipAlignment = ContentClipAlignment.Center
                        if (plan.animateSize) {
                            val widthDuration =
                                if (plan.animateText) {
                                    plan.timing.widthDurationMillis
                                } else {
                                    SizeAnimationDurationMillis
                                }
                            launch { animateWidthTo(plan.targetWidthPx, widthDuration) }
                        } else {
                            snapWidth(plan.targetWidthPx)
                        }

                        if (plan.animateText) {
                            delay(if (plan.animateSize) plan.timing.textDelayMillis.toLong() else 0L)
                            runTextTransitionToTarget(plan.sourceText, plan.targetText)
                        } else {
                            delay(if (plan.animateSize) SizeAnimationDurationMillis.toLong() else 0L)
                            displayedText = plan.targetText
                        }
                    }
                    is AutoWidthTextUpdatePlan.ShrinkLast -> {
                        currentTextTarget = plan.targetText
                        measurementText = plan.sourceText
                        if (plan.animateText) {
                            contentClipAlignment = ContentClipAlignment.Leading
                            val textJob = launch {
                                runTextTransitionToTarget(plan.sourceText, plan.targetText)
                                contentClipAlignment = ContentClipAlignment.Center
                            }
                            val widthJob = launch {
                                delay(if (plan.animateSize) plan.timing.widthDelayMillis.toLong() else 0L)
                                measurementText = plan.targetText
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
                            measurementText = plan.targetText
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

    fun canStartGesture() = !effectiveDisabled && !busy
    fun canDispatchTap() = !effectiveDisabled && !busy && onPress != null

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
        releaseSettleJob?.cancel()
        releaseSettleJob = null
        if (cancelAnimation) {
            releaseAnimationJob?.cancel()
            releaseAnimationJob = null
        }
    }

    fun completeReleaseIfCurrent(generation: Long) {
        if (activeReleaseGeneration != generation) return
        activeReleaseGeneration = null
        releaseSettleJob = null
        onPressedOut?.invoke()
        if (deferredAutoWidthUpdatePending) {
            deferredAutoWidthUpdatePending = false
            deferredAutoWidthDrainToken += 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            progressRunId += 1
            nextConsumed = true
            cancelProgressJobs()
            cancelReleaseTracking()
        }
    }

    suspend fun pressIn() {
        cancelReleaseTracking()
        onPressIn?.invoke()
        onPressedIn?.invoke()
        pressValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = targetStyle.animationDurationMillis ?: fallback.animationDurationMillis!!,
                easing = (targetStyle.animationCurve ?: fallback.animationCurve!!).toEasing(),
            ),
        )
    }

    suspend fun releasePressedState() {
        releaseGeneration += 1
        val generation = releaseGeneration
        activeReleaseGeneration = generation
        releaseSettleJob?.cancel()
        releaseAnimationJob?.cancel()
        releaseAnimationJob =
            scope.launch {
                pressValue.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = ReleaseSpringDampingRatio,
                        stiffness = ReleaseSpringStiffness,
                    ),
                )
            }

        val settleJob =
            scope.launch {
                delay(ReleaseSpringSettleDurationMillis.toLong())
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

    fun consumeDebounceWindow(): Boolean {
        if (debouncedPressTimeMillis <= 0) return true
        val now = SystemClock.uptimeMillis()
        if (now - lastAcceptedPressAt < debouncedPressTimeMillis) {
            return false
        }
        lastAcceptedPressAt = now
        return true
    }

    fun completeProgress(
        callback: (() -> Unit)? = null,
        runId: Long = progressRunId,
    ) {
        if (progressRunId != runId || !busy || nextConsumed) return
        nextConsumed = true
        progressCompletionJob?.cancel()
        progressCompletionJob = scope.launch {
            progressTravelJob?.cancel()
            progressTravelJob = null
            if (progressValue.value < 1f) {
                progressValue.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = ProgressFillCompletionDurationMillis,
                        easing = ProgressCompletionEasing,
                    ),
                )
            }
            if (progressRunId != runId) return@launch

            progressContentJob?.cancel()
            progressActivityJob?.cancel()
            progressOverlayJob?.cancel()

            val restoreJob = launch {
                contentTransition.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = ProgressSwapDurationMillis,
                        easing = ProgressSwapEasing,
                    ),
                )
            }
            val spinnerJob = launch {
                activityTransition.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = ProgressSwapDurationMillis,
                        easing = ProgressSwapEasing,
                    ),
                )
            }
            val overlayJob = launch {
                delay(ProgressOverlayFadeDelayMillis.toLong())
                if (progressRunId != runId) return@launch
                progressOverlayOpacity.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = ProgressOverlayFadeDurationMillis,
                        easing = ProgressCompletionEasing,
                    ),
                )
            }
            restoreJob.join()
            spinnerJob.join()
            overlayJob.join()
            if (progressRunId != runId) return@launch

            releasePressedState()
            if (progressRunId != runId) return@launch

            showProgressVisuals = false
            busy = false
            resetProgressVisualState(unmount = false)
            callback?.invoke()
            onProgressEnd?.invoke()
            progressCompletionJob = null
        }
    }

    suspend fun startProgressFlow() {
        if (busy || onPress == null) return
        progressRunId += 1
        val runId = progressRunId
        cancelProgressJobs()
        cancelReleaseTracking()
        busy = true
        nextConsumed = false
        pressValue.snapTo(1f)
        showProgressVisuals = true
        resetProgressVisualState(unmount = false)
        progressOverlayOpacity.snapTo(if (showProgressBar) 1f else 0f)
        onProgressStart?.invoke()
        progressContentJob = scope.launch {
            contentTransition.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = ProgressSwapDurationMillis,
                    easing = ProgressSwapEasing,
                ),
            )
        }
        progressActivityJob = scope.launch {
            activityTransition.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ProgressSwapDurationMillis,
                    easing = ProgressSwapEasing,
                ),
            )
        }
        progressTravelJob = scope.launch {
            progressValue.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = progressLoadingTimeMillis.coerceAtLeast(0),
                    easing = LinearEasing,
                ),
            )
        }
        progressDeferredPressJob = scope.launch {
            withFrameNanos { }
            if (progressRunId != runId || !busy) return@launch
            onPress?.invoke(AwesomeButtonNext { callback -> completeProgress(callback, runId) })
            if (progressRunId == runId) {
                progressDeferredPressJob = null
            }
        }
    }

    fun activatePressed() {
        scope.launch {
            onPressOut?.invoke()
            if (!canDispatchTap() || !consumeDebounceWindow()) {
                releasePressedState()
                return@launch
            }
            if (progress) {
                startProgressFlow()
            } else {
                onPress?.invoke(null)
                releasePressedState()
            }
        }
    }

    fun cancelPressed() {
        scope.launch {
            onPressOut?.invoke()
            releasePressedState()
        }
    }

    LaunchedEffect(effectiveDisabled) {
        if (effectiveDisabled && !busy) {
            snapReleasedState()
        }
    }

    val interactionModifier =
        Modifier
            .focusable(enabled = !effectiveDisabled && !busy)
            .onKeyEvent { event ->
                val isActivationKey = event.key == Key.Enter || event.key == Key.Spacebar
                if (!isActivationKey) return@onKeyEvent false
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        if (!keyboardArmed && canStartGesture()) {
                            keyboardArmed = true
                            scope.launch { pressIn() }
                        }
                        true
                    }
                    KeyEventType.KeyUp -> {
                        if (keyboardArmed) {
                            keyboardArmed = false
                            activatePressed()
                        }
                        true
                    }
                    else -> false
                }
            }
            .pointerInput(effectiveDisabled, busy, onPress, onLongPress, progress) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!canStartGesture()) return@awaitEachGesture

                    var pointerId: PointerId = down.id
                    var canceled = false
                    var longPressFired = false
                    val longPressJob =
                        onLongPress?.let { longPressHandler ->
                            scope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                if (!canceled) {
                                    longPressFired = true
                                    longPressHandler()
                                }
                            }
                        }

                    scope.launch { pressIn() }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        val inside =
                            change.position.x >= 0f &&
                                change.position.y >= 0f &&
                                change.position.x <= size.width &&
                                change.position.y <= size.height
                        if (!inside) {
                            canceled = true
                            break
                        }
                        if (!change.pressed) {
                            break
                        }
                        change.consume()
                        pointerId = change.id
                    }

                    longPressJob?.cancel()
                    if (canceled || longPressFired) {
                        cancelPressed()
                    } else {
                        activatePressed()
                    }
                }
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                stateDescription = if (busy) "Busy" else if (effectiveDisabled) "Disabled" else "Idle"
                if (effectiveDisabled) {
                    disabled()
                }
                onClick {
                    if (!canStartGesture()) return@onClick false
                    scope.launch {
                        pressIn()
                        activatePressed()
                    }
                    true
                }
            }

    AutoWidthButtonLayout(
        modifier = modifier.then(interactionModifier).testTag("AwesomeButton"),
        resolvedWidthPx = if (hasExplicitResolvedWidth) resolvedWidthPx.value else null,
        height = animatedHeight,
        totalHeight = geometry.totalHeight,
        stretch = stretch,
        paddingHorizontal = contentPaddingHorizontal,
        paddingTop = contentPaddingTop,
        paddingBottom = contentPaddingBottom,
        contentGap = contentGap,
        resolvedStyle = resolvedStyle,
        child = measurementText,
        before = before,
        after = after,
        content = content,
        contentClipAlignment = ContentClipAlignment.Center,
    ) {
        ButtonLayers(
            geometry = geometry,
            resolvedStyle = resolvedStyle,
            disabled = disabled,
            isPlaceholder = isPlaceholder,
            activeOpacity = activeOpacity,
            progress = progress,
            showProgressBar = showProgressBar,
            busy = busy,
            showProgressVisuals = showProgressVisuals,
            pressValue = pressValue.value,
            contentAlpha = contentTransition.value,
            activityAlpha = activityTransition.value,
            progressOverlayAlpha = progressOverlayOpacity.value,
            progressValue = progressValue.value,
            paddingHorizontal = contentPaddingHorizontal,
            paddingTop = contentPaddingTop,
            paddingBottom = contentPaddingBottom,
            contentGap = contentGap,
            child = displayedText,
            before = before,
            after = after,
            extra = extra,
            animatedPlaceholder = animatedPlaceholder,
            contentClipAlignment = contentClipAlignment,
            content = content,
        )
    }
}

private const val StyleTransitionDurationMillis = 200
internal const val SizeAnimationDurationMillis = 175
internal val SizeAnimationEasing = CubicBezierEasing(0.6f, 0.3f, 0.35f, 0.9f)

private data class SizeTransitionSignature(
    val heightPx: Int,
    val paddingHorizontalPx: Int,
    val paddingTopPx: Int,
    val paddingBottomPx: Int,
    val style: AwesomeButtonStyle,
)
