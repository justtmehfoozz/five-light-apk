package com.example.data.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AppUpdateManager(private val context: Context) {

    private val appContext = context.applicationContext

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var activeDownloadCall: okhttp3.Call? = null

    val currentVersionName: String = BuildConfig.VERSION_NAME
    val currentVersionCode: Long = getInstalledVersionCode()

    private fun getInstalledVersionCode(): Long {
        return try {
            val pm = appContext.packageManager
            val pInfo = pm.getPackageInfo(appContext.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
        } catch (_: Exception) {
            BuildConfig.VERSION_CODE.toLong()
        }
    }

    /**
     * Checks the official GitHub repository for the latest release.
     * Prevents concurrent or duplicate requests.
     */
    suspend fun checkForUpdates(force: Boolean = false): UpdateState = withContext(Dispatchers.IO) {
        val currentState = _updateState.value
        if (!force && (currentState is UpdateState.Checking || currentState is UpdateState.Downloading)) {
            return@withContext currentState
        }

        _updateState.value = UpdateState.Checking

        val repoCandidates = listOf(
            "mehfoozzshaikhh/FiveLight",
            "mehfoozshaikh/FiveLight",
            "FiveLight/FiveLight"
        )

        var lastError: String? = null
        var foundRelease: ReleaseInfo? = null

        for (repo in repoCandidates) {
            val releaseUrl = "https://api.github.com/repos/$repo/releases/latest"
            val request = Request.Builder()
                .url(releaseUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "FiveLight-Android-App/$currentVersionName")
                .get()
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val bodyString = resp.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val parsed = parseReleaseJson(bodyString)
                            if (parsed != null) {
                                foundRelease = parsed
                                return@use
                            }
                        }
                    } else if (resp.code != 404) {
                        lastError = "GitHub API responded with status ${resp.code}"
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastError = "Network error: ${e.localizedMessage ?: "Unable to contact GitHub"}"
            }

            if (foundRelease != null) break
        }

        val release = foundRelease
        val err = lastError

        val finalState = if (release != null) {
            val remoteVersionCode = release.versionCode
            if (remoteVersionCode > currentVersionCode) {
                UpdateState.UpdateAvailable(
                    releaseInfo = release,
                    installedVersionName = currentVersionName,
                    installedVersionCode = currentVersionCode
                )
            } else {
                UpdateState.UpToDate(
                    installedVersionName = currentVersionName,
                    installedVersionCode = currentVersionCode
                )
            }
        } else {
            if (err != null && !err.contains("404")) {
                UpdateState.Error(message = err, isNetworkError = true)
            } else {
                // If no releases found or repo has no newer published release
                UpdateState.UpToDate(
                    installedVersionName = currentVersionName,
                    installedVersionCode = currentVersionCode
                )
            }
        }

        _updateState.value = finalState
        finalState
    }

    private fun parseReleaseJson(jsonString: String): ReleaseInfo? {
        return try {
            val json = JSONObject(jsonString)

            val isDraft = json.optBoolean("draft", false)
            val isPrerelease = json.optBoolean("prerelease", false)
            if (isDraft || isPrerelease) return null

            val tagName = json.optString("tag_name", "")
            val name = json.optString("name", tagName)
            val body = json.optString("body", "")
            val publishedAt = json.optString("published_at", "")

            val assetsArray = json.optJSONArray("assets") ?: return null
            var apkUrl: String? = null
            var apkName: String? = null
            var apkSize: Long = 0L

            for (i in 0 until assetsArray.length()) {
                val asset = assetsArray.getJSONObject(i)
                val assetName = asset.optString("name", "")
                val downloadUrl = asset.optString("browser_download_url", "")
                val size = asset.optLong("size", 0L)

                if (assetName.endsWith(".apk", ignoreCase = true) && downloadUrl.startsWith("https://")) {
                    apkUrl = downloadUrl
                    apkName = assetName
                    apkSize = size
                    break
                }
            }

            if (apkUrl.isNullOrBlank() || apkName.isNullOrBlank()) {
                return null
            }

            // Extract remote versionCode authoritatively
            var extractedVersionCode: Long = -1L

            // 1. Look for explicit versionCode in body or name
            val patterns = listOf(
                Pattern.compile("versionCode\\s*[:=]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("build\\s*[:=]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("code\\s*[:=]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("b(\\d+)", Pattern.CASE_INSENSITIVE)
            )

            for (pat in patterns) {
                val m = pat.matcher("$body\n$name\n$apkName")
                if (m.find()) {
                    extractedVersionCode = m.group(1)?.toLongOrNull() ?: -1L
                    if (extractedVersionCode > 0) break
                }
            }

            // 2. Extract semantic version from tag (e.g. v1.7, 1.7)
            val cleanTagName = tagName.removePrefix("v").removePrefix("V").trim()
            val versionName = if (cleanTagName.isNotBlank()) cleanTagName else name

            if (extractedVersionCode <= 0) {
                val semverPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?")
                val m = semverPattern.matcher(versionName)
                if (m.find()) {
                    val major = m.group(1)?.toIntOrNull() ?: 1
                    val minor = m.group(2)?.toIntOrNull() ?: 0
                    val patch = m.group(3)?.toIntOrNull() ?: 0
                    // Derive numerical comparison code (e.g. 1.7 -> 5 if 1.6 was 4)
                    extractedVersionCode = (major * 100 + minor * 10 + patch).toLong()
                }
            }

            // 3. Look for SHA256 checksum in body
            var sha256: String? = null
            val shaPattern = Pattern.compile("(?:sha-?256|checksum)\\s*[:=]?\\s*([a-fA-F0-9]{64})", Pattern.CASE_INSENSITIVE)
            val shaMatcher = shaPattern.matcher(body)
            if (shaMatcher.find()) {
                sha256 = shaMatcher.group(1)
            }

            ReleaseInfo(
                versionName = versionName,
                versionCode = if (extractedVersionCode > 0) extractedVersionCode else currentVersionCode + 1,
                tagName = tagName,
                name = name,
                body = body,
                publishedAt = publishedAt,
                apkDownloadUrl = apkUrl,
                apkFileName = apkName,
                apkSize = apkSize,
                sha256 = sha256
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Downloads the APK directly inside FiveLight via HTTPS.
     * Prevents duplicate simultaneous downloads.
     */
    suspend fun downloadUpdate(releaseInfo: ReleaseInfo): UpdateState = withContext(Dispatchers.IO) {
        val currentState = _updateState.value
        if (currentState is UpdateState.Downloading) {
            return@withContext currentState
        }

        val updateDir = File(appContext.cacheDir, "updates").apply {
            if (!exists()) mkdirs()
        }
        val targetApk = File(updateDir, "fivelight_v${releaseInfo.versionName}_${releaseInfo.versionCode}.apk")

        if (targetApk.exists()) {
            targetApk.delete()
        }

        _updateState.value = UpdateState.Downloading(
            releaseInfo = releaseInfo,
            progressPercent = 0,
            bytesDownloaded = 0,
            totalBytes = releaseInfo.apkSize
        )

        val request = Request.Builder()
            .url(releaseInfo.apkDownloadUrl)
            .header("User-Agent", "FiveLight-Android-App/$currentVersionName")
            .get()
            .build()

        val call = httpClient.newCall(request)
        activeDownloadCall = call

        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                targetApk.delete()
                val errorState = UpdateState.Error("Download failed with HTTP ${response.code}", isNetworkError = true)
                _updateState.value = errorState
                return@withContext errorState
            }

            val body = response.body
            if (body == null) {
                targetApk.delete()
                val errorState = UpdateState.Error("Download response was empty", isNetworkError = true)
                _updateState.value = errorState
                return@withContext errorState
            }

            val totalBytes = if (body.contentLength() > 0) body.contentLength() else releaseInfo.apkSize
            var bytesDownloaded = 0L
            var lastEmittedPercent = -1
            var lastEmittedTime = 0L

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetApk)

            val buffer = ByteArray(8192)
            var read: Int

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
                        val now = System.currentTimeMillis()

                        // Throttle progress updates to avoid flooding Compose state
                        if (percent != lastEmittedPercent && (now - lastEmittedTime > 150 || percent == 100)) {
                            lastEmittedPercent = percent
                            lastEmittedTime = now
                            _updateState.value = UpdateState.Downloading(
                                releaseInfo = releaseInfo,
                                progressPercent = percent,
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes
                            )
                        }
                    }
                    output.flush()
                }
            }

            // Validate downloaded APK
            val validationResult = validateApk(targetApk, releaseInfo)
            if (validationResult.isSuccess) {
                val readyState = UpdateState.ReadyToInstall(
                    releaseInfo = releaseInfo,
                    apkFilePath = targetApk.absolutePath
                )
                _updateState.value = readyState
                readyState
            } else {
                targetApk.delete()
                val errorState = UpdateState.Error(
                    message = validationResult.exceptionOrNull()?.message ?: "Downloaded APK is invalid",
                    canRetry = true
                )
                _updateState.value = errorState
                errorState
            }
        } catch (e: Exception) {
            targetApk.delete()
            if (e is CancellationException) throw e
            val errorState = UpdateState.Error(
                message = "Download interrupted: ${e.localizedMessage ?: "Network error"}",
                isNetworkError = true
            )
            _updateState.value = errorState
            errorState
        } finally {
            activeDownloadCall = null
        }
    }

    fun cancelDownload() {
        activeDownloadCall?.cancel()
        activeDownloadCall = null
        _updateState.value = UpdateState.Idle
    }

    /**
     * Validates the downloaded APK file:
     * - Existence and readability
     * - Application ID matching
     * - Version code comparison
     * - Signature compatibility
     * - Optional SHA-256 checksum verification
     */
    fun validateApk(apkFile: File, expectedRelease: ReleaseInfo): Result<Unit> {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            return Result.failure(IllegalStateException("Downloaded APK file is empty or missing."))
        }

        // SHA-256 Checksum Verification
        if (!expectedRelease.sha256.isNullOrBlank()) {
            val computedSha256 = calculateSha256(apkFile)
            if (!computedSha256.equals(expectedRelease.sha256.trim(), ignoreCase = true)) {
                return Result.failure(IllegalStateException("APK SHA-256 checksum verification failed."))
            }
        }

        val pm = appContext.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_ACTIVITIES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES or PackageManager.GET_ACTIVITIES
        }

        val archiveInfo: PackageInfo? = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
        if (archiveInfo == null) {
            return Result.failure(IllegalStateException("The downloaded file is not a valid Android APK."))
        }

        val archivePackageName = archiveInfo.packageName
        if (archivePackageName != appContext.packageName) {
            return Result.failure(
                IllegalStateException("Incompatible package ($archivePackageName). Expected ${appContext.packageName}.")
            )
        }

        val archiveVersionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archiveInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            archiveInfo.versionCode.toLong()
        }

        if (archiveVersionCode <= currentVersionCode) {
            return Result.failure(
                IllegalStateException("Downloaded APK version code ($archiveVersionCode) is not newer than installed ($currentVersionCode).")
            )
        }

        // Verify Android Signature Compatibility
        val signaturesMatch = verifySignatureCompatibility(pm, archiveInfo)
        if (!signaturesMatch) {
            return Result.failure(
                IllegalStateException("The downloaded update's signing certificate does not match the installed FiveLight app.")
            )
        }

        return Result.success(Unit)
    }

    private fun verifySignatureCompatibility(pm: PackageManager, archiveInfo: PackageInfo): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val installedInfo = pm.getPackageInfo(appContext.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val installedSigners = installedInfo.signingInfo?.apkContentsSigners
                val archiveSigners = archiveInfo.signingInfo?.apkContentsSigners

                if (installedSigners != null && archiveSigners != null && installedSigners.isNotEmpty() && archiveSigners.isNotEmpty()) {
                    val installedHashes = installedSigners.map { it.toByteArray().contentHashCode() }.toSet()
                    val archiveHashes = archiveSigners.map { it.toByteArray().contentHashCode() }.toSet()
                    installedHashes.intersect(archiveHashes).isNotEmpty()
                } else {
                    true
                }
            } else {
                @Suppress("DEPRECATION")
                val installedInfo = pm.getPackageInfo(appContext.packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                val installedSigs = installedInfo.signatures
                @Suppress("DEPRECATION")
                val archiveSigs = archiveInfo.signatures

                if (installedSigs != null && archiveSigs != null && installedSigs.isNotEmpty() && archiveSigs.isNotEmpty()) {
                    val installedHashes = installedSigs.map { it.toByteArray().contentHashCode() }.toSet()
                    val archiveHashes = archiveSigs.map { it.toByteArray().contentHashCode() }.toSet()
                    installedHashes.intersect(archiveHashes).isNotEmpty()
                } else {
                    true
                }
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if the app has permission to install unknown apps (Android 8.0+).
     */
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Returns an intent to navigate the user to Android's Install Unknown Apps settings page.
     */
    fun getInstallPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${appContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Launches the native Android package installer using FileProvider.
     */
    fun installUpdate(apkFilePath: String): Boolean {
        val apkFile = File(apkFilePath)
        if (!apkFile.exists()) {
            _updateState.value = UpdateState.Error("Update file not found on device.", canRetry = true)
            return false
        }

        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            true
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(
                "Unable to launch package installer: ${e.localizedMessage ?: "Unknown error"}",
                canRetry = true
            )
            false
        }
    }

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }
}
