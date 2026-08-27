package com.example.data.audio

import android.content.Context
import com.example.data.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

sealed class SurahDownloadStatus {
    object NotDownloaded : SurahDownloadStatus()
    data class Downloading(val progress: Float, val downloadedVerses: Int, val totalVerses: Int) : SurahDownloadStatus()
    data class Downloaded(val totalSizeBytes: Long) : SurahDownloadStatus()
    data class Error(val message: String) : SurahDownloadStatus()
}

object QuranAudioRepository {

    const val DEFAULT_RECITER_ID = "ar.alafasy"

    fun getCanonicalVerseAudioUrl(
        surahNumber: Int,
        verseNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): String {
        if (surahNumber == 1) {
            val audioVerseIndex = if (verseNumber == 0) 1 else (verseNumber + 1).coerceAtMost(7)
            val paddedVerse = String.format(Locale.US, "%03d", audioVerseIndex)
            return "https://everyayah.com/data/Alafasy_128kbps/001${paddedVerse}.mp3"
        }
        if (verseNumber == 0) {
            return "https://everyayah.com/data/Alafasy_128kbps/001001.mp3"
        }
        val paddedSurah = String.format(Locale.US, "%03d", surahNumber)
        val paddedVerse = String.format(Locale.US, "%03d", verseNumber)
        return "https://everyayah.com/data/Alafasy_128kbps/${paddedSurah}${paddedVerse}.mp3"
    }

    fun getAudioDirectory(context: Context, reciterId: String = DEFAULT_RECITER_ID, surahNumber: Int): File {
        val baseDir = File(context.filesDir, "quran_audio/$reciterId/surah_$surahNumber")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir
    }

    fun getVerseAudioFile(
        context: Context,
        surahNumber: Int,
        verseNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): File {
        val dir = getAudioDirectory(context, reciterId, surahNumber)
        return File(dir, "verse_$verseNumber.mp3")
    }

    fun isVerseAudioCached(
        context: Context,
        surahNumber: Int,
        verseNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): Boolean {
        val file = getVerseAudioFile(context, surahNumber, verseNumber, reciterId)
        return file.exists() && file.length() > 0
    }

    fun getSurahCachedVerseCount(
        context: Context,
        surahNumber: Int,
        versesCount: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): Int {
        var count = 0
        for (v in 1..versesCount) {
            if (isVerseAudioCached(context, surahNumber, v, reciterId)) {
                count++
            }
        }
        return count
    }

    fun isSurahDownloaded(
        context: Context,
        surahNumber: Int,
        versesCount: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): Boolean {
        if (versesCount <= 0) return false
        for (v in 1..versesCount) {
            if (!isVerseAudioCached(context, surahNumber, v, reciterId)) {
                return false
            }
        }
        return true
    }

    fun getSurahDownloadedSizeBytes(
        context: Context,
        surahNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): Long {
        val dir = getAudioDirectory(context, reciterId, surahNumber)
        if (!dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".mp3")) {
                size += file.length()
            }
        }
        return size
    }

    fun getTotalAudioStorageBytes(context: Context): Long {
        val rootDir = File(context.filesDir, "quran_audio")
        if (!rootDir.exists()) return 0L
        return calculateDirectorySize(rootDir)
    }

    private fun calculateDirectorySize(dir: File): Long {
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return total
    }

    suspend fun getOrCacheVerseAudioFile(
        context: Context,
        surahNumber: Int,
        verseNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): File? = withContext(Dispatchers.IO) {
        val targetFile = getVerseAudioFile(context, surahNumber, verseNumber, reciterId)
        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext targetFile
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext null
        }

        val urlStr = getCanonicalVerseAudioUrl(surahNumber, verseNumber, reciterId)
        val tempFile = File(targetFile.parentFile, "verse_${verseNumber}_${System.currentTimeMillis()}.tmp")

        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 12000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val inputStream: InputStream = connection.inputStream
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            if (tempFile.length() > 0) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                val renamed = tempFile.renameTo(targetFile)
                if (renamed && targetFile.exists() && targetFile.length() > 0) {
                    return@withContext targetFile
                }
            }
            tempFile.delete()
            return@withContext null
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            return@withContext null
        }
    }

    suspend fun downloadSurah(
        context: Context,
        surahNumber: Int,
        versesCount: Int,
        reciterId: String = DEFAULT_RECITER_ID,
        onProgress: (downloadedVerses: Int, totalVerses: Int, progressFraction: Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext false
        }

        var downloadedCount = getSurahCachedVerseCount(context, surahNumber, versesCount, reciterId)
        onProgress(downloadedCount, versesCount, downloadedCount.toFloat() / versesCount.coerceAtLeast(1).toFloat())

        if (downloadedCount == versesCount) {
            return@withContext true
        }

        for (v in 1..versesCount) {
            if (isVerseAudioCached(context, surahNumber, v, reciterId)) {
                continue
            }

            val file = getOrCacheVerseAudioFile(context, surahNumber, v, reciterId)
            if (file == null || !file.exists() || file.length() == 0L) {
                return@withContext false
            }

            downloadedCount++
            val fraction = (downloadedCount.toFloat() / versesCount.toFloat()).coerceIn(0f, 1f)
            onProgress(downloadedCount, versesCount, fraction)
        }

        return@withContext isSurahDownloaded(context, surahNumber, versesCount, reciterId)
    }

    suspend fun deleteDownloadedSurah(
        context: Context,
        surahNumber: Int,
        reciterId: String = DEFAULT_RECITER_ID
    ): Boolean = withContext(Dispatchers.IO) {
        val dir = getAudioDirectory(context, reciterId, surahNumber)
        if (dir.exists()) {
            dir.deleteRecursively()
        } else {
            true
        }
    }

    suspend fun deleteAllDownloadedAudio(context: Context): Boolean = withContext(Dispatchers.IO) {
        val rootDir = File(context.filesDir, "quran_audio")
        if (rootDir.exists()) {
            rootDir.deleteRecursively()
        } else {
            true
        }
    }
}
