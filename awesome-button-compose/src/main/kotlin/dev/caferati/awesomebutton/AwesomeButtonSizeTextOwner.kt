package dev.caferati.awesomebutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

internal data class AwesomeButtonSizeTextInput(
    val widthMode: ButtonWidthMode,
    val fixedWidthPx: Int?,
    val targetHeightPx: Int,
    val autoWidthTextEligible: Boolean,
    val targetAutoWidthPx: Int?,
    val targetText: String?,
    val animateSize: Boolean,
    val textTransition: Boolean,
    val textTransitionSlotStaggerMillis: Int,
    val sizeSignature: SizeTransitionSignature,
    val releaseActive: Boolean,
    val reduceMotion: Boolean,
)

/** Owns the complete fixed/automatic/stretch size and built-in text transition lifecycle. */
internal class AwesomeButtonSizeTextOwner(
    private val scope: CoroutineScope,
    initialResolvedWidthPx: Int?,
    initialHeightPx: Int,
    initialText: String?,
    initialAutoWidthTextEligible: Boolean,
) {
    val resolvedWidthPx = Animatable((initialResolvedWidthPx ?: 0).toFloat())
    val resolvedHeightPx = Animatable(initialHeightPx.toFloat())

    var displayedText: String? by mutableStateOf(initialText)
        private set

    var contentClipAlignment: ContentClipAlignment by mutableStateOf(ContentClipAlignment.Center)
        private set

    var hasExplicitResolvedWidth: Boolean by mutableStateOf(initialResolvedWidthPx != null)
        private set

    private var currentTextTarget = initialText
    private var renderedWidthMode: ButtonWidthMode? = null
    private var renderedAutoWidthTextEligible = initialAutoWidthTextEligible
    private var renderedTextTransition = false
    private var renderedSizeSignature: SizeTransitionSignature? = null
    private var generation = 0L
    private var rootJob: Job? = null
    private var deferredInput: AwesomeButtonSizeTextInput? = null

    fun reconcile(input: AwesomeButtonSizeTextInput) {
        generation += 1
        val operationGeneration = generation
        rootJob?.cancel()
        rootJob =
            scope.launch {
                coroutineScope {
                    reconcileCurrent(input)
                }
                if (generation == operationGeneration) {
                    rootJob = null
                }
            }
    }

    fun settleDeferredAfterRelease() {
        val input = deferredInput ?: return
        deferredInput = null
        reconcile(input.copy(releaseActive = false))
    }

    fun cancel() {
        generation += 1
        deferredInput = null
        rootJob?.cancel()
        rootJob = null
    }

    private suspend fun CoroutineScope.reconcileCurrent(input: AwesomeButtonSizeTextInput) {
        val normalizedSlotStaggerMillis =
            normalizeTextTransitionSlotStaggerMillis(input.textTransitionSlotStaggerMillis)
        val shouldAnimateSize = input.animateSize && !input.reduceMotion
        val shouldAnimateText = input.textTransition && !input.reduceMotion
        val previousWidthMode = renderedWidthMode
        val widthBridge = shouldSnapWidthBridge(previousWidthMode, input.widthMode)
        val currentWidthPx =
            if (!widthBridge && hasExplicitResolvedWidth) {
                resolvedWidthPx.value.roundToInt()
            } else {
                null
            }

        if (shouldDeferReleaseAutoWidthTransition(
                isReleaseActive = input.releaseActive,
                previousWidthMode = previousWidthMode,
                nextWidthMode = input.widthMode,
                currentAutoWidthTextEligible = renderedAutoWidthTextEligible,
                nextAutoWidthTextEligible = input.autoWidthTextEligible,
                currentTextTransition = renderedTextTransition,
                nextTextTransition = shouldAnimateText,
                nextAnimateSize = shouldAnimateSize,
                sizeSignatureUnchanged = renderedSizeSignature == input.sizeSignature,
                currentText = currentTextTarget,
                nextText = input.targetText,
                currentWidthPx = currentWidthPx,
                targetWidthPx = input.targetAutoWidthPx,
            )
        ) {
            deferredInput = input
            return
        }

        deferredInput = null
        renderedWidthMode = input.widthMode
        renderedAutoWidthTextEligible = input.autoWidthTextEligible
        renderedTextTransition = shouldAnimateText
        renderedSizeSignature = input.sizeSignature

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
                resolvedHeightPx.snapTo(input.targetHeightPx.toFloat())
                return
            }

            if (abs(resolvedHeightPx.value - input.targetHeightPx) >= 0.5f) {
                resolvedHeightPx.animateTo(
                    targetValue = input.targetHeightPx.toFloat(),
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
                        nextText = input.targetText,
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

        when (input.widthMode) {
            ButtonWidthMode.Stretch -> {
                snapWidth(null)
                syncTextTransitionState()
            }
            ButtonWidthMode.Fixed -> {
                val fixedWidthPx = requireNotNull(input.fixedWidthPx)
                if (widthBridge || !shouldAnimateSize || !hasExplicitResolvedWidth) {
                    snapWidth(fixedWidthPx)
                } else {
                    launch { animateWidthTo(fixedWidthPx) }
                }
                syncTextTransitionState()
            }
            ButtonWidthMode.Auto -> {
                when (
                    val plan =
                        resolveAutoWidthTextUpdatePlan(
                            isEligible = input.autoWidthTextEligible,
                            targetText = input.targetText,
                            currentWidthPx = currentWidthPx,
                            targetWidthPx = input.targetAutoWidthPx,
                            displayedText = displayedText,
                            animateSize = shouldAnimateSize,
                            textTransition = shouldAnimateText,
                            slotStaggerMillis = normalizedSlotStaggerMillis,
                        )
                ) {
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
}
