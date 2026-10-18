package com.example.data.reminder

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

/**
 * Singleton manager for playing Azaan audio cleanly without overlapping or memory leaks.
 * Strictly respects audio focus, device ringer mode, Do Not Disturb policy, and volume settings.
 */
object AzaanAudioPlayer {
    private const val TAG = "AzaanAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var completionCallback: (() -> Unit)? = null

    @Synchronized
    fun playAzaan(context: Context, onCompletion: (() -> Unit)? = null) {
        stopAzaan()
        this.completionCallback = onCompletion

        val appContext = context.applicationContext
        audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        // 1. Check Device Ringer Mode
        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        val isSilentOrVibrate = ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE

        // 2. Check Do Not Disturb (DND) Interruption Filter
        val isDndMuted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager != null) {
            val filter = notificationManager.currentInterruptionFilter
            filter == NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            false
        }

        if (isSilentOrVibrate || isDndMuted) {
            com.example.util.AdhanDebugLogger.log(
                appContext,
                "[AUDIO SKIPPED] Suppressed due to Ringer Mode ($ringerMode) or DND Policy (isDndMuted=$isDndMuted)."
            )
            Log.i(TAG, "Audio playback suppressed due to Ringer Mode ($ringerMode) or DND Policy.")
            performSoftHaptic(appContext)
            val cb = completionCallback
            completionCallback = null
            cb?.invoke()
            return
        }

        // 3. Request Audio Focus
        val focusGranted = requestAudioFocus(audioManager)
        if (!focusGranted) {
            com.example.util.AdhanDebugLogger.log(
                appContext,
                "[AUDIO SKIPPED] Audio focus request denied by system."
            )
            Log.w(TAG, "Audio focus request denied. Suppressing audible Azaan.")
            performSoftHaptic(appContext)
            val cb = completionCallback
            completionCallback = null
            cb?.invoke()
            return
        }

        try {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            mediaPlayer = MediaPlayer.create(appContext, R.raw.azaan, attrs, 0)?.apply {
                setOnCompletionListener {
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO COMPLETED] Azaan audio completed naturally.")
                    Log.i(TAG, "Scheduled Azaan audio playback completed naturally.")
                    stopAzaan()
                    val cb = completionCallback
                    completionCallback = null
                    cb?.invoke()
                }
                setOnErrorListener { mp, _, _ ->
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] MediaPlayer error during playback.")
                    Log.e(TAG, "MediaPlayer error during Azaan playback.")
                    try { mp.reset(); mp.release() } catch (_: Exception) {}
                    if (mediaPlayer === mp) mediaPlayer = null
                    abandonAudioFocus()
                    val cb = completionCallback
                    completionCallback = null
                    cb?.invoke()
                    true
                }
                start()
            }
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO STARTED] Azaan audio playback started successfully.")
            Log.i(TAG, "Scheduled Azaan audio playback started successfully.")
        } catch (e: Exception) {
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] Failed to start Azaan audio: ${e.message}")
            Log.e(TAG, "Failed to play Azaan audio: ${e.message}", e)
            stopAzaan()
            val cb = completionCallback
            completionCallback = null
            cb?.invoke()
        }
    }

    @Synchronized
    fun stopAzaan() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping Azaan player: ${e.message}")
        } finally {
            mediaPlayer = null
            abandonAudioFocus()
            val cb = completionCallback
            completionCallback = null
            cb?.invoke()
        }
    }

    @Synchronized
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    private fun requestAudioFocus(am: AudioManager?): Boolean {
        if (am == null) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        Log.i(TAG, "Audio focus lost during playback. Stopping Azaan.")
                        stopAzaan()
                    }
                }
                .build()
            audioFocusRequest = focusRequest
            am.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        stopAzaan()
                    }
                },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        }
    }

    private fun performSoftHaptic(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vm?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(150)
                }
            }
        } catch (_: Exception) {}
    }
}
