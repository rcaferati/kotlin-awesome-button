package dev.caferati.awesomebutton

import android.os.Looper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class AwesomeButtonProgressDependencies(
    val onPress: AwesomeButtonPressCallback?,
    val onProgressStart: (() -> Unit)?,
    val onProgressEnd: (() -> Unit)?,
    val onPressedOut: (() -> Unit)?,
    val effectiveDisabled: Boolean,
    val showProgressBar: Boolean,
    val progressLoadingTimeMillis: Int,
    val reduceMotion: Boolean,
)

internal interface AwesomeButtonProgressCommandBindings {
    fun isMounted(): Boolean

    fun currentDependencies(): AwesomeButtonProgressDependencies

    suspend fun requestRelease(
        physicalLifecycle: Boolean,
        onPressedOutSnapshot: (() -> Unit)?,
    ): Boolean
}

/** Stable root-owned routing port used by a progress run without retaining consumer callbacks. */
internal class AwesomeButtonProgressCommandPort {
    private var bindings: AwesomeButtonProgressCommandBindings = InactiveProgressBindings

    fun update(next: AwesomeButtonProgressCommandBindings) {
        bindings = next
    }

    fun isMounted(): Boolean = bindings.isMounted()

    fun currentDependencies(): AwesomeButtonProgressDependencies = bindings.currentDependencies()

    suspend fun requestRelease(
        physicalLifecycle: Boolean,
        onPressedOutSnapshot: (() -> Unit)?,
    ): Boolean = bindings.requestRelease(physicalLifecycle, onPressedOutSnapshot)
}

/** Owns one-shot progress runs, their visual state, and accepted completion snapshots. */
internal class AwesomeButtonProgressOwner(
    private val scope: CoroutineScope,
    private val awaitDeferredFrame: suspend () -> Unit = { withFrameNanos { } },
    private val isMainThread: () -> Boolean = { Looper.myLooper() == Looper.getMainLooper() },
) {
    val contentTransition = Animatable(1f)
    val activityTransition = Animatable(0f)
    val overlayOpacity = Animatable(0f)
    val progressValue = Animatable(0f)

    var busy: Boolean by mutableStateOf(false)
        private set

    var nextConsumed: Boolean by mutableStateOf(false)
        private set

    var showProgressVisuals: Boolean by mutableStateOf(false)
        private set

    var hasPhysicalLifecycle: Boolean by mutableStateOf(true)
        private set

    private var runId = 0L
    private var rootJob: Job? = null
    private var completionCallbackSnapshot: (() -> Unit)? = null
    private var progressEndCallbackSnapshot: (() -> Unit)? = null
    private var activeCommands: AwesomeButtonProgressCommandPort? = null

    fun start(
        physicalLifecycle: Boolean,
        commands: AwesomeButtonProgressCommandPort,
        snapPressedState: suspend (Boolean) -> Unit,
    ) {
        val startDependencies = commands.currentDependencies()
        if (busy || startDependencies.onPress == null) return
        runId += 1
        val generation = runId
        cancelRoot(clearSnapshots = true)
        activeCommands = commands
        busy = true
        hasPhysicalLifecycle = physicalLifecycle
        nextConsumed = false
        showProgressVisuals = true
        rootJob =
            scope.launch {
                snapPressedState(physicalLifecycle)
                resetVisualState(unmount = false)
                if (startDependencies.reduceMotion) progressValue.snapTo(1f)
                overlayOpacity.snapTo(if (startDependencies.showProgressBar) 1f else 0f)
                commands.currentDependencies().onProgressStart?.let { callback ->
                    callback()
                    awaitDeferredFrame()
                }
                if (!owns(generation)) return@launch
                val committedDependencies = commands.currentDependencies()
                if (committedDependencies.effectiveDisabled || committedDependencies.onPress == null) {
                    rollback(generation, commands)
                    return@launch
                }

                coroutineScope {
                    launch {
                        if (startDependencies.reduceMotion) {
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
                    launch {
                        if (startDependencies.reduceMotion) {
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
                    launch {
                        if (!startDependencies.reduceMotion) {
                            progressValue.animateTo(
                                targetValue = 1f,
                                animationSpec =
                                    tween(
                                        durationMillis = startDependencies.progressLoadingTimeMillis,
                                        easing = LinearEasing,
                                    ),
                            )
                        }
                    }
                    launch {
                        awaitDeferredFrame()
                        if (!owns(generation)) return@launch
                        val dispatchDependencies = commands.currentDependencies()
                        val progressPress = dispatchDependencies.onPress
                        if (dispatchDependencies.effectiveDisabled || progressPress == null) {
                            rollback(generation, commands)
                            return@launch
                        }
                        progressPress(completionHandle(generation, commands))
                    }
                }
            }
    }

    fun rollback() {
        val commands = activeCommands ?: return
        rollback(runId, commands)
    }

    private fun rollback(
        generation: Long,
        commands: AwesomeButtonProgressCommandPort,
    ) {
        if (!owns(generation) || nextConsumed) return
        nextConsumed = true
        val progressEndSnapshot = commands.currentDependencies().onProgressEnd
        rootJob?.cancel()
        rootJob =
            scope.launch {
                val releaseDependencies = commands.currentDependencies()
                if (!commands.requestRelease(hasPhysicalLifecycle, releaseDependencies.onPressedOut)) return@launch
                if (!owns(generation)) return@launch
                showProgressVisuals = false
                busy = false
                resetVisualState(unmount = false)
                progressEndSnapshot?.invoke()
                if (isCurrent(generation)) clearCompletionState()
            }
    }

    suspend fun applyMotionPolicy(
        reduceMotion: Boolean,
        showProgressBar: Boolean,
        releaseActive: Boolean,
    ) {
        if (busy && !nextConsumed) {
            overlayOpacity.snapTo(if (showProgressBar) 1f else 0f)
            if (reduceMotion) {
                progressValue.snapTo(1f)
                contentTransition.snapTo(0f)
                activityTransition.snapTo(1f)
            }
        }
        if (reduceMotion && busy && nextConsumed && !releaseActive) {
            launchCompletion(runId)
        }
    }

    fun cancel(unmount: Boolean) {
        runId += 1
        nextConsumed = true
        cancelRoot(clearSnapshots = true)
        activeCommands = null
        if (unmount) {
            busy = false
            showProgressVisuals = false
        }
    }

    private fun completionHandle(
        generation: Long,
        commands: AwesomeButtonProgressCommandPort,
    ): AwesomeButtonNext =
        AwesomeButtonNext { callback ->
            if (isMainThread()) {
                acceptCompletion(generation, callback, commands)
            } else {
                scope.launch {
                    acceptCompletion(generation, callback, commands)
                }
            }
        }

    private fun acceptCompletion(
        generation: Long,
        callback: (() -> Unit)?,
        commands: AwesomeButtonProgressCommandPort,
    ) {
        if (!owns(generation) || nextConsumed) return
        nextConsumed = true
        completionCallbackSnapshot = callback
        progressEndCallbackSnapshot = commands.currentDependencies().onProgressEnd
        launchCompletion(generation)
    }

    private fun launchCompletion(generation: Long) {
        if (!owns(generation)) return
        val commands = activeCommands ?: return
        val callbackSnapshot = completionCallbackSnapshot
        val progressEndSnapshot = progressEndCallbackSnapshot
        rootJob?.cancel()
        rootJob =
            scope.launch {
                awaitDeferredFrame()
                if (!owns(generation)) return@launch
                val reduceMotion = commands.currentDependencies().reduceMotion
                if (reduceMotion) {
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
                if (!owns(generation)) return@launch

                coroutineScope {
                    launch {
                        if (reduceMotion) {
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
                    launch {
                        if (reduceMotion) {
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
                    launch {
                        if (!reduceMotion) delay(PROGRESS_OVERLAY_FADE_DELAY_MILLIS.toLong())
                        if (!owns(generation)) return@launch
                        if (reduceMotion) {
                            overlayOpacity.snapTo(0f)
                        } else {
                            overlayOpacity.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    tween(
                                        durationMillis = PROGRESS_OVERLAY_FADE_DURATION_MILLIS,
                                        easing = progressCompletionEasing,
                                    ),
                            )
                        }
                    }
                }
                if (!owns(generation)) return@launch

                val releaseDependencies = commands.currentDependencies()
                if (!commands.requestRelease(hasPhysicalLifecycle, releaseDependencies.onPressedOut)) return@launch
                if (!owns(generation)) return@launch

                showProgressVisuals = false
                busy = false
                resetVisualState(unmount = false)
                completionCallbackSnapshot = null
                progressEndCallbackSnapshot = null
                callbackSnapshot?.let { callback ->
                    callback()
                    awaitDeferredFrame()
                }
                if (!isCurrent(generation)) return@launch
                progressEndSnapshot?.invoke()
                if (isCurrent(generation)) clearCompletionState()
            }
    }

    private fun owns(generation: Long): Boolean = runId == generation && busy && activeCommands?.isMounted() == true

    private fun isCurrent(generation: Long): Boolean = runId == generation && activeCommands?.isMounted() == true

    private suspend fun resetVisualState(unmount: Boolean) {
        contentTransition.snapTo(1f)
        activityTransition.snapTo(0f)
        overlayOpacity.snapTo(0f)
        progressValue.snapTo(0f)
        if (unmount) showProgressVisuals = false
    }

    private fun cancelRoot(clearSnapshots: Boolean) {
        rootJob?.cancel()
        rootJob = null
        if (clearSnapshots) {
            completionCallbackSnapshot = null
            progressEndCallbackSnapshot = null
        }
    }

    private fun clearCompletionState() {
        completionCallbackSnapshot = null
        progressEndCallbackSnapshot = null
        rootJob = null
    }
}

private object InactiveProgressBindings : AwesomeButtonProgressCommandBindings {
    override fun isMounted(): Boolean = false

    override fun currentDependencies(): AwesomeButtonProgressDependencies =
        AwesomeButtonProgressDependencies(
            onPress = null,
            onProgressStart = null,
            onProgressEnd = null,
            onPressedOut = null,
            effectiveDisabled = true,
            showProgressBar = false,
            progressLoadingTimeMillis = 0,
            reduceMotion = true,
        )

    override suspend fun requestRelease(
        physicalLifecycle: Boolean,
        onPressedOutSnapshot: (() -> Unit)?,
    ): Boolean = false
}
