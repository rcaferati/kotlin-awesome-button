package dev.caferati.awesomebutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwesomeButtonCompositionLifecycleInstrumentedTest : AwesomeButtonInstrumentedTestBase() {
    @Test
    fun autoWidthMainContentOwnsOneLifecycleAndOneDisposal() {
        val mounted = mutableStateOf(true)
        val probe = CompositionProbe()
        composeRule.setContent {
            if (mounted.value) {
                AwesomeButton(
                    content = {
                        Probe(probe, "main")
                        Box(Modifier.width(96.dp).height(20.dp))
                    },
                )
            }
        }

        composeRule.runOnIdle {
            assertEquals(1, probe.starts("main"))
            assertEquals(1, probe.active("main"))
            assertEquals(0, probe.disposals("main"))
            mounted.value = false
        }
        composeRule.runOnIdle {
            assertEquals(0, probe.active("main"))
            assertEquals(1, probe.disposals("main"))
        }
    }

    @Test
    fun auxiliarySlotsOwnOneLifecycleAndExtraDoesNotChangeAutoWidth() {
        val includeExtra = mutableStateOf(false)
        val probe = CompositionProbe()
        composeRule.setContent {
            AwesomeButton(
                before = {
                    Probe(probe, "before")
                    Box(Modifier.width(12.dp).height(12.dp))
                },
                child = "Slots",
                after = {
                    Probe(probe, "after")
                    Box(Modifier.width(18.dp).height(12.dp))
                },
                extra =
                    if (includeExtra.value) {
                        {
                            Probe(probe, "extra")
                            Box(Modifier.fillMaxWidth().height(10.dp))
                        }
                    } else {
                        null
                    },
            )
        }

        composeRule.waitForIdle()
        val widthWithoutExtra = nodeWidth("AwesomeButton")
        composeRule.runOnIdle { includeExtra.value = true }
        composeRule.waitForIdle()
        val widthWithExtra = nodeWidth("AwesomeButton")

        composeRule.runOnIdle {
            assertEquals(1, probe.starts("before"))
            assertEquals(1, probe.starts("after"))
            assertEquals(1, probe.starts("extra"))
            assertEquals(1, probe.active("before"))
            assertEquals(1, probe.active("after"))
            assertEquals(1, probe.active("extra"))
            assertClose(widthWithoutExtra, widthWithExtra, tolerance = 1f)
        }
    }

    @Test
    fun contentReplacementDisposesEachRetiredOwnerExactlyOnce() {
        val contentID = mutableStateOf("A")
        val mounted = mutableStateOf(true)
        val probe = CompositionProbe()
        composeRule.setContent {
            if (mounted.value) {
                AwesomeButton(
                    content = {
                        Probe(probe, contentID.value)
                        Box(Modifier.width(80.dp).height(20.dp))
                    },
                )
            }
        }

        composeRule.runOnIdle { contentID.value = "B" }
        composeRule.runOnIdle { contentID.value = "C" }
        composeRule.runOnIdle {
            assertEquals(1, probe.starts("A"))
            assertEquals(1, probe.disposals("A"))
            assertEquals(1, probe.starts("B"))
            assertEquals(1, probe.disposals("B"))
            assertEquals(1, probe.starts("C"))
            assertEquals(1, probe.active("C"))
            mounted.value = false
        }
        composeRule.runOnIdle {
            assertEquals(1, probe.disposals("C"))
            assertEquals(0, probe.totalActive())
        }
    }

    @Test
    fun sizingModeChangesPreserveOneContentOwnerAndResolvedGeometry() {
        val mode = mutableStateOf(ButtonWidthMode.Auto)
        val probe = CompositionProbe()
        composeRule.setContent {
            Row(Modifier.width(300.dp)) {
                AwesomeButton(
                    width = if (mode.value == ButtonWidthMode.Fixed) 180.dp else null,
                    stretch = mode.value == ButtonWidthMode.Stretch,
                    content = {
                        Probe(probe, "sizing")
                        Box(Modifier.width(90.dp).height(20.dp))
                    },
                )
            }
        }

        composeRule.waitForIdle()
        val autoWidth = nodeWidth("AwesomeButton")
        assertTrue(autoWidth >= 90f)

        composeRule.runOnIdle { mode.value = ButtonWidthMode.Fixed }
        composeRule.waitForIdle()
        assertClose(180f, nodeWidth("AwesomeButton"), tolerance = 1f)

        composeRule.runOnIdle { mode.value = ButtonWidthMode.Stretch }
        composeRule.waitForIdle()
        assertClose(300f, nodeWidth("AwesomeButton"), tolerance = 1f)
        composeRule.runOnIdle {
            assertEquals(1, probe.starts("sizing"))
            assertEquals(1, probe.active("sizing"))
            assertEquals(0, probe.disposals("sizing"))
            assertTrue(probe.compositions("sizing") > 0)
            println("awesome-button auto-width compositions=${probe.compositions("sizing")}")
        }
    }

    @Test
    fun progressConfigurationChangesRecordContentCompositionsWithoutReplacingOwnership() {
        val progress = mutableStateOf(false)
        val probe = CompositionProbe()
        composeRule.setContent {
            AwesomeButton(
                progress = progress.value,
                content = { Probe(probe, "progress") },
            )
        }

        composeRule.runOnIdle { progress.value = true }
        composeRule.runOnIdle { progress.value = false }
        composeRule.runOnIdle {
            assertEquals(1, probe.starts("progress"))
            assertEquals(1, probe.active("progress"))
            assertEquals(0, probe.disposals("progress"))
            assertTrue(probe.compositions("progress") > 0)
            println("awesome-button progress compositions=${probe.compositions("progress")}")
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun Probe(
    probe: CompositionProbe,
    id: String,
) {
    SideEffect { probe.compose(id) }
    DisposableEffect(id) {
        probe.start(id)
        onDispose { probe.dispose(id) }
    }
}

private class CompositionProbe {
    private val starts = mutableMapOf<String, Int>()
    private val disposals = mutableMapOf<String, Int>()
    private val compositions = mutableMapOf<String, Int>()

    fun compose(id: String) {
        compositions[id] = compositions(id) + 1
    }

    fun start(id: String) {
        starts[id] = starts(id) + 1
    }

    fun dispose(id: String) {
        disposals[id] = disposals(id) + 1
    }

    fun starts(id: String): Int = starts[id] ?: 0

    fun disposals(id: String): Int = disposals[id] ?: 0

    fun compositions(id: String): Int = compositions[id] ?: 0

    fun active(id: String): Int = starts(id) - disposals(id)

    fun totalActive(): Int = starts.values.sum() - disposals.values.sum()
}
