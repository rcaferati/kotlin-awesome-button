package dev.caferati.awesomebutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class AwesomeButtonReleaseSettlement(
    val generation: Long,
    val onPressedOut: (() -> Unit)?,
)

/** Owns the single press-depth value and every native-spring release generation. */
internal class AwesomeButtonReleaseOwner(
    private val scope: CoroutineScope,
) {
    val pressValue = Animatable(0f)

    var activeGeneration: Long? by mutableStateOf(null)
        private set

    private var generation = 0L
    private var rootJob: Job? = null
    private var settlement: CompletableDeferred<AwesomeButtonReleaseSettlement?>? = null
    private var onPressedOutSnapshot: (() -> Unit)? = null

    suspend fun press(
        reduceMotion: Boolean,
        durationMillis: Int,
        style: AwesomeButtonStyle,
        fallback: AwesomeButtonStyle,
    ) {
        if (reduceMotion) {
            pressValue.snapTo(1f)
        } else {
            pressValue.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = durationMillis,
                        easing = (style.animationCurve ?: fallback.animationCurve!!).toEasing(),
                    ),
            )
        }
    }

    suspend fun release(
        onPressedOut: (() -> Unit)?,
        reduceMotion: Boolean,
    ): AwesomeButtonReleaseSettlement? {
        invalidate()
        generation += 1
        val releaseGeneration = generation
        if (reduceMotion) {
            pressValue.snapTo(0f)
            return AwesomeButtonReleaseSettlement(releaseGeneration, onPressedOut)
        }

        activeGeneration = releaseGeneration
        onPressedOutSnapshot = onPressedOut
        val releaseSettlement = CompletableDeferred<AwesomeButtonReleaseSettlement?>()
        settlement = releaseSettlement
        rootJob =
            scope.launch {
                try {
                    coroutineScope {
                        launch {
                            pressValue.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    spring(
                                        dampingRatio = releaseSpringDampingRatio,
                                        stiffness = RELEASE_SPRING_STIFFNESS,
                                    ),
                            )
                        }
                        launch {
                            delay(RELEASE_SPRING_SETTLE_DURATION_MILLIS.toLong())
                            if (activeGeneration == releaseGeneration) {
                                val result =
                                    AwesomeButtonReleaseSettlement(
                                        releaseGeneration,
                                        onPressedOutSnapshot,
                                    )
                                activeGeneration = null
                                onPressedOutSnapshot = null
                                settlement = null
                                releaseSettlement.complete(result)
                            }
                        }
                    }
                } finally {
                    releaseSettlement.complete(null)
                }
            }
        return releaseSettlement.await()
    }

    suspend fun snapPressed(pressed: Boolean) {
        pressValue.snapTo(if (pressed) 1f else 0f)
    }

    suspend fun snapReleased() {
        invalidate()
        pressValue.snapTo(0f)
    }

    suspend fun settleImmediately(): AwesomeButtonReleaseSettlement? {
        val active = activeGeneration ?: return null
        val callback = onPressedOutSnapshot
        val pendingSettlement = settlement
        generation += 1
        activeGeneration = null
        onPressedOutSnapshot = null
        settlement = null
        rootJob?.cancel()
        rootJob = null
        pressValue.snapTo(0f)
        pendingSettlement?.complete(AwesomeButtonReleaseSettlement(active, callback))
        return null
    }

    fun invalidate() {
        generation += 1
        activeGeneration = null
        onPressedOutSnapshot = null
        settlement?.complete(null)
        settlement = null
        rootJob?.cancel()
        rootJob = null
    }

    fun cancel() {
        invalidate()
    }
}
