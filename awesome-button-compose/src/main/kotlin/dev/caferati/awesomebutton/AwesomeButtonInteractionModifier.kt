package dev.caferati.awesomebutton

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data class AwesomeButtonInteractionSemantics(
    val effectiveDisabled: Boolean,
    val busy: Boolean,
    val isPlaceholder: Boolean,
    val hasPressHandler: Boolean,
    val hasLongPressHandler: Boolean,
    val accessibilityLabel: String?,
    val accessibilityHint: String?,
    val accessibilityLongPressLabel: String?,
    val busyStateDescription: String,
    val defaultLongPressLabel: String,
)

internal interface AwesomeButtonInteractionCommandBindings {
    fun canBeginGesture(): Boolean

    fun hasLongPressHandler(): Boolean

    fun beginGesture(): Long?

    suspend fun preparePressIn(
        token: Long,
        gestureScope: CoroutineScope,
    )

    suspend fun dispatchLongPress(token: Long)

    fun isGestureActive(token: Long): Boolean

    fun finishGesture(
        token: Long,
        inside: Boolean,
    )

    fun onFocusLost()

    fun onActivationKeyDown()

    fun onActivationKeyUp()

    fun activateAtomically(): Boolean

    fun activateLongPressAtomically(): Boolean
}

/** Stable event port whose bindings are replaced only after a composition commits. */
internal class AwesomeButtonInteractionCommandPort {
    private var bindings: AwesomeButtonInteractionCommandBindings = InactiveInteractionBindings

    fun update(next: AwesomeButtonInteractionCommandBindings) {
        bindings = next
    }

    fun canBeginGesture(): Boolean = bindings.canBeginGesture()

    fun hasLongPressHandler(): Boolean = bindings.hasLongPressHandler()

    fun beginGesture(): Long? = bindings.beginGesture()

    suspend fun preparePressIn(
        token: Long,
        gestureScope: CoroutineScope,
    ) {
        bindings.preparePressIn(token, gestureScope)
    }

    suspend fun dispatchLongPress(token: Long) {
        bindings.dispatchLongPress(token)
    }

    fun isGestureActive(token: Long): Boolean = bindings.isGestureActive(token)

    fun finishGesture(
        token: Long,
        inside: Boolean,
    ) {
        bindings.finishGesture(token, inside)
    }

    fun onFocusLost() {
        bindings.onFocusLost()
    }

    fun onActivationKeyDown() {
        bindings.onActivationKeyDown()
    }

    fun onActivationKeyUp() {
        bindings.onActivationKeyUp()
    }

    fun activateAtomically(): Boolean = bindings.activateAtomically()

    fun activateLongPressAtomically(): Boolean = bindings.activateLongPressAtomically()
}

internal fun Modifier.awesomeButtonInteraction(
    commands: AwesomeButtonInteractionCommandPort,
    semantics: AwesomeButtonInteractionSemantics,
): Modifier =
    sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        .focusable(enabled = !semantics.effectiveDisabled && !semantics.busy)
        .onFocusChanged { focusState ->
            if (!focusState.isFocused) {
                commands.onFocusLost()
            }
        }.onKeyEvent { event ->
            val isActivationKey = event.key == Key.Enter || event.key == Key.Spacebar
            if (!isActivationKey) return@onKeyEvent false
            when (event.type) {
                KeyEventType.KeyDown -> commands.onActivationKeyDown()
                KeyEventType.KeyUp -> commands.onActivationKeyUp()
                else -> return@onKeyEvent false
            }
            true
        }.pointerInput(Unit) {
            coroutineScope {
                val pointerCoroutineScope = this
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (!commands.canBeginGesture()) return@awaitEachGesture

                    var pointerId: PointerId = down.id
                    var endedInside = false
                    val longPressArmed = commands.hasLongPressHandler()
                    val token = commands.beginGesture() ?: return@awaitEachGesture
                    pointerCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        commands.preparePressIn(token, pointerCoroutineScope)
                    }
                    val longPressJob =
                        if (longPressArmed) {
                            pointerCoroutineScope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                commands.dispatchLongPress(token)
                            }
                        } else {
                            null
                        }

                    try {
                        while (commands.isGestureActive(token)) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            val inside =
                                change.position.x >= 0f &&
                                    change.position.y >= 0f &&
                                    change.position.x <= size.width &&
                                    change.position.y <= size.height
                            if (!inside) break
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

                    commands.finishGesture(token, inside = endedInside)
                }
            }
        }.semantics(mergeDescendants = true) {
            role = Role.Button
            semantics.accessibilityLabel?.let { contentDescription = it }
            if (semantics.busy) {
                stateDescription = semantics.busyStateDescription
            }
            if (semantics.effectiveDisabled) {
                disabled()
            }
            if (semantics.isPlaceholder && semantics.accessibilityLabel == null) {
                hideFromAccessibility()
            }
            if (!semantics.effectiveDisabled && !semantics.busy && semantics.hasPressHandler) {
                onClick(label = semantics.accessibilityHint) {
                    commands.activateAtomically()
                }
            }
            if (!semantics.effectiveDisabled && !semantics.busy && semantics.hasLongPressHandler) {
                onLongClick(
                    label = semantics.accessibilityLongPressLabel ?: semantics.defaultLongPressLabel,
                ) {
                    commands.activateLongPressAtomically()
                }
            }
        }

private object InactiveInteractionBindings : AwesomeButtonInteractionCommandBindings {
    override fun canBeginGesture(): Boolean = false

    override fun hasLongPressHandler(): Boolean = false

    override fun beginGesture(): Long? = null

    override suspend fun preparePressIn(
        token: Long,
        gestureScope: CoroutineScope,
    ) = Unit

    override suspend fun dispatchLongPress(token: Long) = Unit

    override fun isGestureActive(token: Long): Boolean = false

    override fun finishGesture(
        token: Long,
        inside: Boolean,
    ) = Unit

    override fun onFocusLost() = Unit

    override fun onActivationKeyDown() = Unit

    override fun onActivationKeyUp() = Unit

    override fun activateAtomically(): Boolean = false

    override fun activateLongPressAtomically(): Boolean = false
}
