package com.example.data.updater

import androidx.annotation.Keep

@Keep
data class ReleaseInfo(
    val versionName: String,
    val versionCode: Long,
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSize: Long,
    val sha256: String? = null
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpToDate(
        val installedVersionName: String,
        val installedVersionCode: Long,
        val lastCheckedTime: Long = System.currentTimeMillis()
    ) : UpdateState
    data class UpdateAvailable(
        val releaseInfo: ReleaseInfo,
        val installedVersionName: String,
        val installedVersionCode: Long
    ) : UpdateState
    data class Downloading(
        val releaseInfo: ReleaseInfo,
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState
    data class ReadyToInstall(
        val releaseInfo: ReleaseInfo,
        val apkFilePath: String
    ) : UpdateState
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false,
        val canRetry: Boolean = true
    ) : UpdateState
}
