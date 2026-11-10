package com.example.data.reminder

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private var lastContext: Context? = null

    private val _playbackState = MutableStateFlow(AdhanPlaybackState.IDLE)
    val playbackState: StateFlow<AdhanPlaybackState> = _playbackState.asStateFlow()

    private fun setPlaybackState(context: Context?, newState: AdhanPlaybackState) {
        val oldState = _playbackState.value
        _playbackState.value = newState
        val ctx = context ?: lastContext
        if (ctx != null) {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            com.example.util.AdhanDebugLogger.log(
                ctx,
                "[PLAYBACK STATE CHANGE $timestamp] ${oldState.name} -> ${newState.name}"
            )
        }
    }

    @Synchronized
    fun resetPlaybackState(context: Context? = null) {
        setPlaybackState(context, AdhanPlaybackState.IDLE)
    }

    @Synchronized
    fun playAzaan(
        context: Context,
        prayerName: String = "UNKNOWN",
        dateStr: String = "UNKNOWN",
        reason: String = "service_request"
    ) {
        lastContext = context.applicationContext
        val stackTrace = Thread.currentThread().stackTrace
        val callerFrames = stackTrace.drop(2).take(4).joinToString(" -> ") { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
        com.example.util.AdhanDebugLogger.log(
            context,
            "[AUDIO START REQUESTED] Prayer=$prayerName, Date=$dateStr, Reason=$reason, Stack=$callerFrames"
        )

        stopAzaanInternal(context)
        isAudioStoppedOrCompleted = false
        setPlaybackState(context, AdhanPlaybackState.IDLE)

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
            setPlaybackState(appContext, AdhanPlaybackState.SKIPPED)
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
            setPlaybackState(appContext, AdhanPlaybackState.SKIPPED)
            com.example.util.AdhanDebugLogger.log(
                appContext,
                "[AUDIO ERROR] Audio focus request denied by system."
            )
            Log.w(TAG, "Audio focus request denied. Suppressing audible Azaan.")
            performSoftHaptic(appContext)
            return
        }

        // 4. Detailed Pre-Creation Diagnostics Log
        val alarmVol = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: -1
        val alarmMax = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: -1
        val musicVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1
        val musicMax = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: -1
        val filterVal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager != null) {
            notificationManager.currentInterruptionFilter
        } else -1
        val isServiceCtx = context is android.app.Service || appContext is android.app.Service
        val ctxClassName = context.javaClass.name

        com.example.util.AdhanDebugLogger.log(
            appContext,
            "[AUDIO DIAGNOSTICS] ringerMode=$ringerMode, dndFilter=$filterVal, ALARM_VOL=$alarmVol/$alarmMax, MUSIC_VOL=$musicVol/$musicMax, focusGranted=$focusGranted, isServiceCtx=$isServiceCtx ($ctxClassName)"
        )

        try {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            mediaPlayer = MediaPlayer.create(appContext, R.raw.azaan, attrs, 0)?.apply {
                val durMs = try { duration } catch (_: Exception) { -1 }
                com.example.util.AdhanDebugLogger.log(
                    appContext,
                    "[MEDIAPLAYER CREATED] mpNotNull=true, duration=$durMs ms"
                )

                setOnInfoListener { _, what, extra ->
                    com.example.util.AdhanDebugLogger.log(
                        appContext,
                        "[AUDIO INFO] MediaPlayer info code: what=$what, extra=$extra"
                    )
                    false
                }

                setOnCompletionListener {
                    isAudioStoppedOrCompleted = true
                    setPlaybackState(appContext, AdhanPlaybackState.COMPLETED)
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO COMPLETED] Azaan audio completed naturally.")
                    Log.i(TAG, "Scheduled Azaan audio playback completed naturally.")
                    stopAzaanInternal(appContext)
                }

                setOnErrorListener { mp, what, extra ->
                    isAudioStoppedOrCompleted = true
                    setPlaybackState(appContext, AdhanPlaybackState.STOPPED)
                    com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] MediaPlayer error code: what=$what, extra=$extra.")
                    Log.e(TAG, "MediaPlayer error during Azaan playback.")
                    try { mp.reset(); mp.release() } catch (_: Exception) {}
                    if (mediaPlayer === mp) mediaPlayer = null
                    abandonAudioFocus()
                    true
                }

                start()

                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.postDelayed({
                    val isPlaying1s = isPlaying()
                    val pos1s = try { mediaPlayer?.currentPosition ?: -1 } catch (_: Exception) { -1 }
                    com.example.util.AdhanDebugLogger.log(
                        appContext,
                        "[AUDIO 1S CHECK] 1 sec post-start: isPlaying=$isPlaying1s, currentPosition=$pos1s ms"
                    )
                }, 1000L)
            }

            if (mediaPlayer == null) {
                isAudioStoppedOrCompleted = true
                setPlaybackState(appContext, AdhanPlaybackState.SKIPPED)
                com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] MediaPlayer creation failed (null or missing R.raw.azaan resource).")
                return
            }

            setPlaybackState(appContext, AdhanPlaybackState.PLAYING)
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO STARTED] Azaan audio playback started successfully.")
            Log.i(TAG, "Scheduled Azaan audio playback started successfully.")
        } catch (e: Exception) {
            isAudioStoppedOrCompleted = true
            setPlaybackState(appContext, AdhanPlaybackState.STOPPED)
            com.example.util.AdhanDebugLogger.log(appContext, "[AUDIO ERROR] Failed to start Azaan audio: ${e.message}")
            Log.e(TAG, "Failed to play Azaan audio: ${e.message}", e)
            stopAzaanInternal(appContext)
        }
    }

    @Synchronized
    fun stopAzaan(context: Context? = null) {
        val stackTrace = Thread.currentThread().stackTrace
        val callerFrames = stackTrace.drop(2).take(4).joinToString(" -> ") { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
        val ctx = context ?: lastContext
        if (ctx != null) {
            com.example.util.AdhanDebugLogger.log(
                ctx,
                "[AUDIO STOP REQUESTED] Stack=$callerFrames"
            )
        }
        isAudioStoppedOrCompleted = true
        setPlaybackState(ctx, AdhanPlaybackState.STOPPED)
        stopAzaanInternal(ctx)
    }

    private fun stopAzaanInternal(context: Context? = null) {
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
                    stopAzaan(context)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    try { mediaPlayer?.setVolume(0.3f, 0.3f) } catch (_: Exception) {}
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
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
