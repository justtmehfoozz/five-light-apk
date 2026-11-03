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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AdhanPlaybackState {
    IDLE,
    PLAYING,
    COMPLETED,
    STOPPED,
    SKIPPED
}

/**
 * Singleton manager for playing Azaan audio cleanly without overlapping or memory leaks.
 * Strictly respects audio focus, device ringer mode, Do Not Disturb policy, and volume settings.
 * Exposes a single observable playback state via StateFlow.
 */
object AzaanAudioPlayer {
    private const val TAG = "AzaanAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isAudioStoppedOrCompleted = false

    private val _playbackState = MutableStateFlow(AdhanPlaybackState.IDLE)
    val playbackState: StateFlow<AdhanPlaybackState> = _playbackState.asStateFlow()

    @Synchronized
    fun resetPlaybackState() {
        _playbackState.value = AdhanPlaybackState.IDLE
    }

    @Synchronized
    fun playAzaan(
        context: Context,
        prayerName: String = "UNKNOWN",
        dateStr: String = "UNKNOWN",
        reason: String = "service_request"
    ) {
        val stackTrace = Thread.currentThread().stackTrace
        val callerFrames = stackTrace.drop(2).take(4).joinToString(" -> ") { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
        com.example.util.AdhanDebugLogger.log(
            context,
            "[AUDIO START REQUESTED] Prayer=$prayerName, Date=$dateStr, Reason=$reason, Stack=$callerFrames"
        )

        stopAzaanInternal()
        isAudioStoppedOrCompleted = false
        _playbackState.value = AdhanPlaybackState.IDLE

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
            isAudioStoppedOrCompleted = true
            _playbackState.value = AdhanPlaybackState.SKIPPED
            com.example.util.AdhanDebugLogger.log(
                appContext,
                "[AUDIO SKIPPED reason = ringerMode ($ringerMode) / dnd value (isDndMuted=$isDndMuted)] Audio playback suppressed due to Ringer Mode or DND Policy."
            )
            Log.i(TAG, "Audio playback suppressed due to Ringer Mode ($ringerMode) or DND Policy.")
            performSoftHaptic(appContext)
            return
        }

        // 3. Request Audio Focus
        val focusGranted = requestAudioFocus(appContext, audioManager)
        if (!focusGranted) {
            isAudioStoppedOrCompleted = true
            _playbackState.value = AdhanPlaybackState.SKIPPED
            com.example.util.AdhanDebugLogger.log(
                appContext,
                "[AUDIO ERROR] Audio focus request denied by system."
            )
            Log.w(TAG, "Audio focus request denied. Suppressing audible Azaan.")
            performSoftHaptic(appContext)
            return
        }

        try {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            mediaPlayer = MediaPlayer.create(appContext, R.raw.azaan, attrs, 0)?.apply {
                setOnCompletionListener {
                    isAudioStoppedOrCompleted = true
                    _playbackState.value = AdhanPlaybackState.COMPLETED
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO COMPLETED] Azaan audio completed naturally.")
                    Log.i(TAG, "Scheduled Azaan audio playback completed naturally.")
                    stopAzaanInternal()
                }
                setOnErrorListener { mp, what, extra ->
                    isAudioStoppedOrCompleted = true
                    _playbackState.value = AdhanPlaybackState.STOPPED
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] MediaPlayer error during playback (what=$what, extra=$extra).")
                    Log.e(TAG, "MediaPlayer error during Azaan playback.")
                    try { mp.reset(); mp.release() } catch (_: Exception) {}
                    if (mediaPlayer === mp) mediaPlayer = null
                    abandonAudioFocus()
                    true
                }
                start()
            }
            if (mediaPlayer == null) {
                isAudioStoppedOrCompleted = true
                _playbackState.value = AdhanPlaybackState.SKIPPED
                com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] MediaPlayer creation failed (null or missing R.raw.azaan resource).")
                return
            }
            _playbackState.value = AdhanPlaybackState.PLAYING
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO STARTED] Azaan audio playback started successfully.")
            Log.i(TAG, "Scheduled Azaan audio playback started successfully.")
        } catch (e: Exception) {
            isAudioStoppedOrCompleted = true
            _playbackState.value = AdhanPlaybackState.STOPPED
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] Failed to start Azaan audio: ${e.message}")
            Log.e(TAG, "Failed to play Azaan audio: ${e.message}", e)
            stopAzaanInternal()
        }
    }

    @Synchronized
    fun stopAzaan() {
        isAudioStoppedOrCompleted = true
        _playbackState.value = AdhanPlaybackState.STOPPED
        stopAzaanInternal()
    }

    private fun stopAzaanInternal() {
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

    private fun requestAudioFocus(context: Context, am: AudioManager?): Boolean {
        if (am == null) return true
        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            com.example.util.AdhanDebugLogger.log(
                context,
                "[AUDIO FOCUS CHANGE] focusChange=$focusChange, isAudioStoppedOrCompleted=$isAudioStoppedOrCompleted"
            )
            Log.i(TAG, "Audio focus change received: $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Full or transient loss: stop playback
                    stopAzaan()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // DUCK: Lower volume to 30% instead of stopping
                    try { mediaPlayer?.setVolume(0.3f, 0.3f) } catch (_: Exception) {}
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // GAIN: Restore volume to 100% if playing
                    try { mediaPlayer?.setVolume(1.0f, 1.0f) } catch (_: Exception) {}
                }
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioFocusRequest = focusRequest
            am.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                focusChangeListener,
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
