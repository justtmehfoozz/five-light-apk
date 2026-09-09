package com.example.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Listener interface for physical volume key events intercepted by the application.
 */
interface VolumeKeyEventListener {
    /**
     * Called when a physical volume key down event occurs.
     * @param keyCode KeyEvent.KEYCODE_VOLUME_UP or KeyEvent.KEYCODE_VOLUME_DOWN
     * @return true if the event was consumed and should not trigger Android system volume adjustments.
     */
    fun onVolumeKeyDown(keyCode: Int): Boolean

    /**
     * Called when a physical volume key up event occurs.
     * @param keyCode KeyEvent.KEYCODE_VOLUME_UP or KeyEvent.KEYCODE_VOLUME_DOWN
     * @return true if the event was consumed.
     */
    fun onVolumeKeyUp(keyCode: Int): Boolean
}

/**
 * Provider interface implemented by the Activity to allow Composables to register/unregister
 * for physical volume key events.
 */
interface VolumeKeyDispatcher {
    fun setVolumeKeyEventListener(listener: VolumeKeyEventListener?)
}

/**
 * CompositionLocal providing the current VolumeKeyDispatcher to the UI tree.
 */
val LocalVolumeKeyDispatcher = staticCompositionLocalOf<VolumeKeyDispatcher?> { null }

/**
 * Traverses context wrappers to find the enclosing Activity.
 */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
