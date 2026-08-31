package dev.caferati.awesomebutton

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AwesomeButtonOwnerTest {
    @Test
    fun interactionPortUsesOnlyLatestCommittedBindings() {
        val port = AwesomeButtonInteractionCommandPort()

        assertFalse(port.canBeginGesture())
        port.update(interactionBindings(canBegin = true, token = 4L))
        assertTrue(port.canBeginGesture())
        assertEquals(4L, port.beginGesture())

        port.update(interactionBindings(canBegin = false, token = 9L))
        assertFalse(port.canBeginGesture())
        assertEquals(9L, port.beginGesture())
    }

    @Test
    fun sizeOwnerReconcilesFixedAndStretchWithoutRetainingContent() =
        runBlocking {
            val style = AwesomeButtonThemeData.fallbackStyle
            val owner = AwesomeButtonSizeTextOwner(this, null, 52, "First", false)

            owner.reconcile(
                sizeInput(
                    widthMode = ButtonWidthMode.Fixed,
                    fixedWidthPx = 120,
                    text = "Second",
                    style = style,
                ),
            )
            repeat(8) { yield() }

            assertTrue(owner.hasExplicitResolvedWidth)
            assertEquals(120f, owner.resolvedWidthPx.value)
            assertEquals("Second", owner.displayedText)

            owner.reconcile(
                sizeInput(
                    widthMode = ButtonWidthMode.Stretch,
                    fixedWidthPx = null,
                    text = "Third",
                    style = style,
                ),
            )
            repeat(8) { yield() }

            assertFalse(owner.hasExplicitResolvedWidth)
            assertEquals("Third", owner.displayedText)
            owner.cancel()
        }

    @Test
    fun progressOwnerAcceptsCompletionOnceAndKeepsAcceptedSnapshots() =
        runBlocking {
            val events = mutableListOf<String>()
            var retainedNext: AwesomeButtonNext? = null
            var dependencies =
                progressDependencies(
                    onPress = { next ->
                        events += "press"
                        retainedNext = next
                    },
                    onProgressStart = { events += "start-a" },
                    onProgressEnd = { events += "end-a" },
                )
            val port = AwesomeButtonProgressCommandPort()
            port.update(
                object : AwesomeButtonProgressCommandBindings {
                    override fun isMounted(): Boolean = true

                    override fun currentDependencies(): AwesomeButtonProgressDependencies = dependencies

                    override suspend fun requestRelease(
                        physicalLifecycle: Boolean,
                        onPressedOutSnapshot: (() -> Unit)?,
                    ): Boolean {
                        events += "release"
                        return true
                    }
                },
            )
            val owner =
                AwesomeButtonProgressOwner(
                    scope = this,
                    awaitDeferredFrame = {},
                    isMainThread = { true },
                )

            owner.start(
                physicalLifecycle = false,
                commands = port,
                snapPressedState = {},
            )
            dependencies = dependencies.copy(onProgressStart = { events += "start-b" })
            repeat(8) { yield() }
            val next = retainedNext
            assertNotNull(next)

            dependencies = dependencies.copy(onProgressEnd = { events += "end-b" })
            next!!.complete { events += "completion" }
            next.complete { events += "duplicate" }
            repeat(8) { yield() }

            assertEquals(listOf("start-b", "press", "release", "completion", "end-b"), events)
            assertFalse(owner.busy)
            assertEquals(0f, owner.overlayOpacity.value)
            owner.cancel(unmount = true)
        }

    @Test
    fun reducedMotionReleaseReturnsItsCapturedCallbackAndSettlesImmediately() =
        runBlocking {
            val owner = AwesomeButtonReleaseOwner(this)
            val callback = {}
            owner.snapPressed(true)

            val settlement = owner.release(callback, reduceMotion = true)

            assertNotNull(settlement)
            assertTrue(settlement?.onPressedOut === callback)
            assertEquals(0f, owner.pressValue.value)
            assertNull(owner.activeGeneration)
            owner.cancel()
        }

    private fun interactionBindings(
        canBegin: Boolean,
        token: Long,
    ): AwesomeButtonInteractionCommandBindings =
        object : AwesomeButtonInteractionCommandBindings {
            override fun isMounted(): Boolean = true

            override fun canBeginGesture(): Boolean = canBegin

            override fun hasLongPressHandler(): Boolean = false

            override fun beginGesture(): Long = token

            override suspend fun preparePressIn(
                token: Long,
                gestureScope: kotlinx.coroutines.CoroutineScope,
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

    private fun sizeInput(
        widthMode: ButtonWidthMode,
        fixedWidthPx: Int?,
        text: String,
        style: AwesomeButtonStyle,
    ): AwesomeButtonSizeTextInput =
        AwesomeButtonSizeTextInput(
            widthMode = widthMode,
            fixedWidthPx = fixedWidthPx,
            targetHeightPx = 52,
            autoWidthTextEligible = false,
            targetAutoWidthPx = null,
            targetText = text,
            animateSize = true,
            textTransition = true,
            textTransitionSlotStaggerMillis = DEFAULT_TEXT_TRANSITION_SLOT_STAGGER_MILLIS,
            sizeSignature = SizeTransitionSignature(52, 16, 0, 0, style),
            releaseActive = false,
            reduceMotion = true,
        )

    private fun progressDependencies(
        onPress: AwesomeButtonPressCallback,
        onProgressStart: () -> Unit,
        onProgressEnd: () -> Unit,
    ): AwesomeButtonProgressDependencies =
        AwesomeButtonProgressDependencies(
            onPress = onPress,
            onProgressStart = onProgressStart,
            onProgressEnd = onProgressEnd,
            onPressedOut = null,
            effectiveDisabled = false,
            showProgressBar = false,
            progressLoadingTimeMillis = 0,
            reduceMotion = true,
        )
}
