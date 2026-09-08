package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.data.model.TasbeehSound
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Centralized, application-lifecycle manager for zero-latency Tasbeeh audio playback.
 * Preloads and caches sound samples using a single SoundPool instance.
 */
class TasbeehAudioPlayer private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val tag = "TasbeehAudioPlayer"

    private val soundPool: SoundPool
    private val soundIdMap = ConcurrentHashMap<TasbeehSound, Int>()
    private val loadedSoundIds = CopyOnWriteArraySet<Int>()

    @Volatile
    private var pendingSound: TasbeehSound? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            Log.d(tag, "Sample load completed: sampleId=$sampleId, status=$status")
            if (status == 0) {
                loadedSoundIds.add(sampleId)
                val currentPending = pendingSound
                if (currentPending != null && soundIdMap[currentPending] == sampleId) {
                    pendingSound = null
                    playSample(sampleId, 1.0f)
                }
            }
        }

        preloadSounds()
    }

    private fun preloadSounds() {
        TasbeehSound.entries.forEach { sound ->
            sound.resId?.let { resId ->
                try {
                    val sampleId = soundPool.load(appContext, resId, 1)
                    if (sampleId > 0) {
                        soundIdMap[sound] = sampleId
                        Log.d(tag, "Loaded sound ${sound.name} -> sampleId=$sampleId (resId=$resId)")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to load sound ${sound.name}", e)
                }
            }
        }
    }

    /**
     * Plays the given TasbeehSound.
     * If the sound is [TasbeehSound.OFF], no sound is played.
     */
    fun playSound(sound: TasbeehSound, volume: Float = 1.0f) {
        if (sound == TasbeehSound.OFF) {
            pendingSound = null
            return
        }

        val sampleId = soundIdMap[sound]
        if (sampleId != null && sampleId > 0) {
            if (loadedSoundIds.contains(sampleId)) {
                playSample(sampleId, volume)
            } else {
                Log.d(tag, "Sound ${sound.name} not yet loaded, marking as pending")
                pendingSound = sound
            }
        } else {
            Log.w(tag, "No sampleId found for sound ${sound.name}")
        }
    }

    /**
     * Plays preview for sound selection.
     */
    fun playPreview(sound: TasbeehSound, volume: Float = 1.0f) {
        try {
            soundPool.autoPause()
        } catch (_: Exception) {}
        playSound(sound, volume)
    }

    private fun playSample(sampleId: Int, volume: Float) {
        try {
            val clampedVol = volume.coerceIn(0f, 1.0f)
            val streamId = soundPool.play(sampleId, clampedVol, clampedVol, 1, 0, 1.0f)
            Log.d(tag, "playSample: sampleId=$sampleId, vol=$clampedVol -> streamId=$streamId")
        } catch (e: Exception) {
            Log.e(tag, "Error playing sample $sampleId", e)
        }
    }

    companion object {
        @Volatile
        private var instance: TasbeehAudioPlayer? = null

        fun getInstance(context: Context): TasbeehAudioPlayer {
            return instance ?: synchronized(this) {
                instance ?: TasbeehAudioPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}
