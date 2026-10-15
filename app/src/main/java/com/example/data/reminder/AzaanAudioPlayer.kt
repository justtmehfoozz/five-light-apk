package com.example.data.reminder

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.R

/**
 * Singleton manager for playing Azaan audio cleanly without overlapping or memory leaks.
 */
object AzaanAudioPlayer {
    private const val TAG = "AzaanAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null

    @Synchronized
    fun playAzaan(context: Context) {
        stopAzaan()

        try {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.azaan, attrs, 0)?.apply {
                setOnCompletionListener {
                    stopAzaan()
                }
                setOnErrorListener { mp, _, _ ->
                    Log.e(TAG, "MediaPlayer error during Azaan playback")
                    try { mp.reset(); mp.release() } catch (_: Exception) {}
                    if (mediaPlayer === mp) mediaPlayer = null
                    true
                }
                start()
            }
            Log.i(TAG, "Scheduled Azaan audio playback started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play Azaan audio: ${e.message}", e)
            stopAzaan()
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
}
