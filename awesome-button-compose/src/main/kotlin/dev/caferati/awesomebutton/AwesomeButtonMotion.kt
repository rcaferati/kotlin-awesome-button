package dev.caferati.awesomebutton

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** Test seam for the system animator-duration policy. Production callers use the platform value. */
internal val LocalAwesomeButtonReduceMotionOverride = staticCompositionLocalOf<Boolean?> { null }

@Composable
internal fun rememberAwesomeButtonReduceMotion(): Boolean {
    val override = LocalAwesomeButtonReduceMotionOverride.current
    if (override != null) return override

    val context = LocalContext.current
    val resolver = context.contentResolver

    fun systemRequestsReducedMotion(): Boolean =
        runCatching {
            Settings.Global.getFloat(
                resolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)

    var reducedMotion by remember(resolver) { mutableStateOf(systemRequestsReducedMotion()) }
    DisposableEffect(resolver) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reducedMotion = systemRequestsReducedMotion()
                }
            }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reducedMotion
}
