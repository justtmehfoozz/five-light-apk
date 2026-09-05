package com.example.data.backup

import android.content.Context
import android.util.Log
import com.example.data.auth.AuthRepository
import com.example.data.repository.AppRepository
import com.example.data.sync.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {

    private suspend fun <T> runWithDriveRetry(
        context: Context,
        googleAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        block: suspend (accessToken: String) -> Result<T>
    ): Result<T> {
        var tokenResult = GoogleDriveService.getAccessToken(context, googleAccount)
        if (tokenResult.isFailure) {
            return Result.failure(Exception("Failed to obtain Drive OAuth token: " + tokenResult.exceptionOrNull()?.message))
        }
        var accessToken = tokenResult.getOrThrow()

        var result = block(accessToken)
        
        val exception = result.exceptionOrNull()
        if (exception != null && (exception.message?.contains("HTTP 403") == true || exception.message?.contains("HTTP 401") == true)) {
            Log.w(TAG, "Drive request failed with HTTP 403/401, invalidating token and retrying...", exception)
            GoogleDriveService.invalidateToken(context, accessToken)
            
            tokenResult = GoogleDriveService.getAccessToken(context, googleAccount)
            if (tokenResult.isSuccess) {
                accessToken = tokenResult.getOrThrow()
                result = block(accessToken)
            }
        }
        
        val finalException = result.exceptionOrNull()
        if (finalException != null && finalException.message?.contains("HTTP 403") == true) {
            return Result.failure(Exception("Google Drive access was denied (HTTP 403). Please ensure you have granted FiveLight permission to access Google Drive during sign-in, or try disconnecting and reconnecting your Google account."))
        }
        
        return result
    }


    private const val TAG = "BackupManager"
    private const val PREFS_NAME = "fivelight_drive_backup_meta"
    private const val KEY_LAST_BACKUP = "last_drive_backup_time"
    private const val BACKUP_FILE_NAME = "fivelight_backup_encrypted.bin"
    private const val SCHEMA_VERSION = 1

    fun getLastBackupTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_BACKUP, 0L)
    }

    private fun setLastBackupTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_BACKUP, timestamp).apply()
    }

    /**
     * Performs a complete encrypted backup of all syncable user data and uploads to Google Drive appDataFolder.
     */
    suspend fun performBackup(
        context: Context,
        repository: AppRepository,
        authRepository: AuthRepository
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val user = authRepository.currentUser.value
                ?: return@withContext Result.failure(Exception("Please sign in to perform a cloud backup."))
            val uid = user.uid
            val now = System.currentTimeMillis()

            // 1. Ensure Google Drive Authorization
            val googleAccount = GoogleDriveService.getAuthorizedAccount(context)
                ?: return@withContext Result.failure(Exception("Google Drive authorization required. Please connect your Google account."))

            // 2. Gather all MUST SYNC data into JSON
            val root = JSONObject()

            // Metadata
            val meta = JSONObject().apply {
                put("backupVersion", SCHEMA_VERSION)
                put("appVersion", "1.8")
                put("timestamp", now)
                put("accountUid", uid)
            }
            root.put("metadata", meta)

            // Prayer Logs
            val prayerLogs = repository.getAllRawPrayerLogsDirect()
            val logsArray = JSONArray()
            for (log in prayerLogs) {
                logsArray.put(JSONObject().apply {
                    put("date", log.date)
                    put("fajrCompleted", log.fajrCompleted)
                    put("dhuhrCompleted", log.dhuhrCompleted)
                    put("asrCompleted", log.asrCompleted)
                    put("maghribCompleted", log.maghribCompleted)
                    put("ishaCompleted", log.ishaCompleted)
                    put("fajrMissed", log.fajrMissed)
                    put("dhuhrMissed", log.dhuhrMissed)
                    put("asrMissed", log.asrMissed)
                    put("maghribMissed", log.maghribMissed)
                    put("ishaMissed", log.ishaMissed)
                    put("fajrNote", log.fajrNote ?: "")
                    put("dhuhrNote", log.dhuhrNote ?: "")
                    put("asrNote", log.asrNote ?: "")
                    put("maghribNote", log.maghribNote ?: "")
                    put("ishaNote", log.ishaNote ?: "")
                    put("fajrQadaAdded", log.fajrQadaAdded)
                    put("dhuhrQadaAdded", log.dhuhrQadaAdded)
                    put("asrQadaAdded", log.asrQadaAdded)
                    put("maghribQadaAdded", log.maghribQadaAdded)
                    put("ishaQadaAdded", log.ishaQadaAdded)
                    put("updatedAt", log.updatedAt)
                    put("isDeleted", log.isDeleted)
                })
            }
            root.put("prayerLogs", logsArray)

            // Qada
            val qadaObj = JSONObject()
            for (prayer in com.example.data.model.PrayerName.values()) {
                val pName = prayer.name.lowercase()
                qadaObj.put("${pName}Count", repository.getQadaCount(prayer))
                qadaObj.put("${pName}Timestamp", repository.getQadaTimestamp(prayer))
                qadaObj.put("${pName}EverAdded", repository.hasEverHadQada(prayer))
            }
            root.put("qada", qadaObj)

            // Bookmarks
            val bookmarks = repository.getAllRawBookmarksDirect()
            val bmArray = JSONArray()
            for (bm in bookmarks) {
                bmArray.put(JSONObject().apply {
                    put("surahNumber", bm.surahNumber)
                    put("verseNumber", bm.verseNumber)
                    put("surahNameEnglish", bm.surahNameEnglish)
                    put("surahNameArabic", bm.surahNameArabic)
                    put("verseTextArabic", bm.verseTextArabic)
                    put("verseTextTranslation", bm.verseTextTranslation)
                    put("timestamp", bm.timestamp)
                    put("updatedAt", bm.updatedAt)
                    put("isDeleted", bm.isDeleted)
                })
            }
            root.put("bookmarks", bmArray)

            // Quran Progress
            val lastRead = repository.lastReadPosition.value
            val progressObj = JSONObject().apply {
                put("dailyGoal", repository.getDailyQuranGoal())
                put("timestamp", repository.getQuranProgressTimestamp())
                if (lastRead != null) {
                    put("lastRead", JSONObject().apply {
                        put("surahNumber", lastRead.surahNumber)
                        put("surahNameEnglish", lastRead.surahNameEnglish)
                        put("surahNameArabic", lastRead.surahNameArabic)
                        put("verseNumber", lastRead.verseNumber)
                        put("verseIndex", lastRead.verseIndex)
                        put("timestamp", lastRead.timestamp)
                    })
                }
                val recentlyRead = repository.recentlyReadList.value
                val rrArray = JSONArray()
                for (rr in recentlyRead) {
                    rrArray.put(JSONObject().apply {
                        put("surahNumber", rr.surahNumber)
                        put("surahNameEnglish", rr.surahNameEnglish)
                        put("surahNameArabic", rr.surahNameArabic)
                        put("verseNumber", rr.verseNumber)
                        put("verseIndex", rr.verseIndex)
                        put("timestamp", rr.timestamp)
                    })
                }
                put("recentlyRead", rrArray)
            }
            root.put("quranProgress", progressObj)

            // Dhikr History
            val dhikrHistory = repository.getAllDhikrHistoryDirect()
            val dhikrArray = JSONArray()
            for (dh in dhikrHistory) {
                dhikrArray.put(JSONObject().apply {
                    put("syncId", dh.syncId)
                    put("dhikrName", dh.dhikrName)
                    put("arabicText", dh.arabicText)
                    put("countCompleted", dh.countCompleted)
                    put("target", dh.target)
                    put("timestamp", dh.timestamp)
                })
            }
            root.put("dhikrHistory", dhikrArray)

            // Tasbeeh State
            val customPresets = repository.customDhikrs.value
            val customPresetsArr = JSONArray()
            for (cp in customPresets) {
                customPresetsArr.put(JSONObject().apply {
                    put("id", cp.id)
                    put("nameEnglish", cp.nameEnglish)
                    put("nameArabic", cp.nameArabic)
                    put("translation", cp.translation)
                    put("defaultTarget", cp.defaultTarget)
                })
            }
            val allPresets = repository.DHIKR_PRESETS + customPresets
            val countsObj = JSONObject()
            val targetsObj = JSONObject()
            for (p in allPresets) {
                countsObj.put(p.id, repository.getDhikrCount(p.id))
                targetsObj.put(p.id, repository.getDhikrTarget(p.id))
            }
            val tasbeehObj = JSONObject().apply {
                put("customPresets", customPresetsArr)
                put("customTargets", JSONArray(repository.customTargets.value))
                put("counts", countsObj)
                put("targets", targetsObj)
                put("timestamp", repository.getTasbeehStateTimestamp())
            }
            root.put("tasbeehState", tasbeehObj)

            // Preferences
            val duaPrefs = context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE)
            val bookmarkedDuaIds = duaPrefs.getStringSet("bookmarked_dua_ids", emptySet()) ?: emptySet()
            val prefsObj = JSONObject().apply {
                put("calcMethod", repository.calcMethod.value.name)
                put("madhab", repository.madhab.value.name)
                put("appearanceMode", repository.appearanceMode.value.name)
                put("timeFormat", repository.timeFormat.value.name)
                put("hijriDateMethod", repository.hijriDateMethod.value.name)
                put("customHijriOffset", repository.customHijriOffset.value)
                put("tasbeehSound", repository.tasbeehSound.value.id)
                put("vibrationEnabled", repository.vibrationEnabled.value)
                put("selectedCity", repository.selectedCity.value?.cityName ?: "")
                put("bookmarkedDuaIds", JSONArray(bookmarkedDuaIds))
                put("timestamp", repository.getPreferencesTimestamp())
            }
            root.put("preferences", prefsObj)

            // 3. Compute SHA-256 checksum
            val jsonBytes = root.toString().toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance("SHA-256").digest(jsonBytes)
            val checksumHex = digest.joinToString("") { "%02x".format(it) }
            meta.put("checksum", checksumHex)

            // 4. Encrypt payload
            val encryptedBytes = encryptData(root.toString(), uid)

            // 5. Upload/update backup in Google Drive appDataFolder
            val uploadFlowResult = runWithDriveRetry(context, googleAccount) { token ->
                val searchResult = GoogleDriveService.findBackupFileId(token)
                if (searchResult.isFailure) return@runWithDriveRetry Result.failure(searchResult.exceptionOrNull()!!)
                val existingFileId = searchResult.getOrNull()

                GoogleDriveService.uploadBackupFile(token, encryptedBytes, existingFileId)
            }
            if (uploadFlowResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Drive upload failed: ${uploadFlowResult.exceptionOrNull()?.message}")
                )
            }

            // 6. Write to local cache file
            try {
                val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
                backupFile.writeBytes(encryptedBytes)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update local backup cache: ${e.message}")
            }

            // 7. ONLY AFTER SUCCESSFUL DRIVE UPLOAD, update timestamp
            setLastBackupTime(context, now)
            Log.d(TAG, "Encrypted backup successfully uploaded to Google Drive appDataFolder (${encryptedBytes.size} bytes)")
            Result.success(now)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads and restores user data directly from Google Drive appDataFolder.
     */
    suspend fun performRestore(
        context: Context,
        repository: AppRepository,
        authRepository: AuthRepository,
        syncManager: FirestoreSyncManager?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = authRepository.currentUser.value
                ?: return@withContext Result.failure(Exception("Please sign in to restore from Google Drive."))
            val uid = user.uid

            // 1. Ensure Google Drive Authorization
            val googleAccount = GoogleDriveService.getAuthorizedAccount(context)
                ?: return@withContext Result.failure(Exception("Google Drive authorization required. Please connect your Google account."))

            // 2. Search & Download backup from Drive with retry
            val downloadFlowResult = runWithDriveRetry(context, googleAccount) { token ->
                val searchResult = GoogleDriveService.findBackupFileId(token)
                if (searchResult.isFailure) return@runWithDriveRetry Result.failure(searchResult.exceptionOrNull()!!)
                val fileId = searchResult.getOrNull()
                    ?: return@runWithDriveRetry Result.failure(Exception("No FiveLight backup found in Google Drive appDataFolder."))

                GoogleDriveService.downloadBackupFile(token, fileId)
            }
            if (downloadFlowResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Restore failed: ${downloadFlowResult.exceptionOrNull()?.message}")
                )
            }
            val encryptedBytes = downloadFlowResult.getOrThrow()

            // 4. Decrypt payload
            val decryptedJsonStr = try {
                decryptData(encryptedBytes, uid)
            } catch (e: Exception) {
                return@withContext Result.failure(
                    Exception("Decryption failed. Ensure you are signed into the correct account ($uid).")
                )
            }

            val root = JSONObject(decryptedJsonStr)
            val meta = root.optJSONObject("metadata")
                ?: return@withContext Result.failure(Exception("Corrupted backup: Missing metadata."))

            val version = meta.optInt("backupVersion", 0)
            if (version <= 0 || version > SCHEMA_VERSION) {
                return@withContext Result.failure(Exception("Incompatible backup version ($version)."))
            }

            val accountUid = meta.optString("accountUid")
            if (accountUid.isNotEmpty() && accountUid != uid && uid != "guest") {
                return@withContext Result.failure(Exception("Backup belongs to a different user account UID ($accountUid)."))
            }

            // Restore Prayer Logs
            val prayerLogsArr = root.optJSONArray("prayerLogs")
            if (prayerLogsArr != null) {
                for (i in 0 until prayerLogsArr.length()) {
                    val obj = prayerLogsArr.getJSONObject(i)
                    val entity = com.example.data.db.PrayerLogEntity(
                        date = obj.getString("date"),
                        fajrCompleted = obj.getBoolean("fajrCompleted"),
                        dhuhrCompleted = obj.getBoolean("dhuhrCompleted"),
                        asrCompleted = obj.getBoolean("asrCompleted"),
                        maghribCompleted = obj.getBoolean("maghribCompleted"),
                        ishaCompleted = obj.getBoolean("ishaCompleted"),
                        fajrMissed = obj.optBoolean("fajrMissed", false),
                        dhuhrMissed = obj.optBoolean("dhuhrMissed", false),
                        asrMissed = obj.optBoolean("asrMissed", false),
                        maghribMissed = obj.optBoolean("maghribMissed", false),
                        ishaMissed = obj.optBoolean("ishaMissed", false),
                        fajrNote = obj.optString("fajrNote", null),
                        dhuhrNote = obj.optString("dhuhrNote", null),
                        asrNote = obj.optString("asrNote", null),
                        maghribNote = obj.optString("maghribNote", null),
                        ishaNote = obj.optString("ishaNote", null),
                        fajrQadaAdded = obj.optBoolean("fajrQadaAdded", false),
                        dhuhrQadaAdded = obj.optBoolean("dhuhrQadaAdded", false),
                        asrQadaAdded = obj.optBoolean("asrQadaAdded", false),
                        maghribQadaAdded = obj.optBoolean("maghribQadaAdded", false),
                        ishaQadaAdded = obj.optBoolean("ishaQadaAdded", false),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isDeleted = obj.optBoolean("isDeleted", false)
                    )
                    repository.applyPrayerLogFromRemote(entity)
                }
            }

            // Restore Qada
            val qadaObj = root.optJSONObject("qada")
            if (qadaObj != null) {
                for (prayer in com.example.data.model.PrayerName.values()) {
                    val pName = prayer.name.lowercase()
                    val count = qadaObj.optInt("${pName}Count", 0)
                    val ts = qadaObj.optLong("${pName}Timestamp", System.currentTimeMillis())
                    val ever = qadaObj.optBoolean("${pName}EverAdded", count > 0)
                    repository.applyQadaFromRemote(prayer, count, ever, ts)
                }
            }

            // Restore Bookmarks
            val bookmarksArr = root.optJSONArray("bookmarks")
            if (bookmarksArr != null) {
                for (i in 0 until bookmarksArr.length()) {
                    val obj = bookmarksArr.getJSONObject(i)
                    val entity = com.example.data.db.BookmarkEntity(
                        surahNumber = obj.getInt("surahNumber"),
                        verseNumber = obj.getInt("verseNumber"),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        verseTextArabic = obj.optString("verseTextArabic", ""),
                        verseTextTranslation = obj.optString("verseTextTranslation", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isDeleted = obj.optBoolean("isDeleted", false)
                    )
                    repository.applyBookmarkFromRemote(entity)
                }
            }

            // Restore Quran Progress
            val progressObj = root.optJSONObject("quranProgress")
            if (progressObj != null) {
                val goal = progressObj.optInt("dailyGoal", 0)
                val ts = progressObj.optLong("timestamp", System.currentTimeMillis())
                val lastReadObj = progressObj.optJSONObject("lastRead")
                val lastRead = if (lastReadObj != null) {
                    com.example.data.model.QuranLastRead(
                        surahNumber = lastReadObj.getInt("surahNumber"),
                        surahNameEnglish = lastReadObj.optString("surahNameEnglish", ""),
                        surahNameArabic = lastReadObj.optString("surahNameArabic", ""),
                        verseNumber = lastReadObj.getInt("verseNumber"),
                        verseIndex = lastReadObj.getInt("verseIndex"),
                        timestamp = lastReadObj.optLong("timestamp", System.currentTimeMillis())
                    )
                } else null

                val rrArr = progressObj.optJSONArray("recentlyRead")
                val rrList = mutableListOf<com.example.data.model.QuranLastRead>()
                if (rrArr != null) {
                    for (i in 0 until rrArr.length()) {
                        val obj = rrArr.getJSONObject(i)
                        rrList.add(com.example.data.model.QuranLastRead(
                            surahNumber = obj.getInt("surahNumber"),
                            surahNameEnglish = obj.optString("surahNameEnglish", ""),
                            surahNameArabic = obj.optString("surahNameArabic", ""),
                            verseNumber = obj.getInt("verseNumber"),
                            verseIndex = obj.getInt("verseIndex"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        ))
                    }
                }
                repository.applyQuranProgressFromRemote(lastRead, rrList, goal, ts)
            }

            // Restore Dhikr History
            val dhikrArr = root.optJSONArray("dhikrHistory")
            if (dhikrArr != null) {
                for (i in 0 until dhikrArr.length()) {
                    val obj = dhikrArr.getJSONObject(i)
                    val entity = com.example.data.db.DhikrHistoryEntity(
                        syncId = obj.optString("syncId", java.util.UUID.randomUUID().toString()),
                        dhikrName = obj.optString("dhikrName", "Dhikr"),
                        arabicText = obj.optString("arabicText", ""),
                        countCompleted = obj.optInt("countCompleted", 0),
                        target = obj.optInt("target", 33),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                    repository.applyDhikrHistoryFromRemote(entity)
                }
            }

            // Restore Tasbeeh State
            val tasbeehObj = root.optJSONObject("tasbeehState")
            if (tasbeehObj != null) {
                val ts = tasbeehObj.optLong("timestamp", System.currentTimeMillis())
                val cpArr = tasbeehObj.optJSONArray("customPresets")
                val customPresetsList = mutableListOf<com.example.data.model.DhikrPreset>()
                if (cpArr != null) {
                    for (i in 0 until cpArr.length()) {
                        val obj = cpArr.getJSONObject(i)
                        customPresetsList.add(com.example.data.model.DhikrPreset(
                            id = obj.getString("id"),
                            nameEnglish = obj.getString("nameEnglish"),
                            nameArabic = obj.getString("nameArabic"),
                            translation = obj.getString("translation"),
                            defaultTarget = obj.getInt("defaultTarget"),
                            isCustom = true
                        ))
                    }
                }
                val ctArr = tasbeehObj.optJSONArray("customTargets")
                val customTargetsList = mutableListOf<Int>()
                if (ctArr != null) {
                    for (i in 0 until ctArr.length()) customTargetsList.add(ctArr.getInt(i))
                }

                val countsObj = tasbeehObj.optJSONObject("counts")
                val countsMap = mutableMapOf<String, Int>()
                if (countsObj != null) {
                    val keys = countsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        countsMap[k] = countsObj.getInt(k)
                    }
                }

                val targetsObj = tasbeehObj.optJSONObject("targets")
                val targetsMap = mutableMapOf<String, Int>()
                if (targetsObj != null) {
                    val keys = targetsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        targetsMap[k] = targetsObj.getInt(k)
                    }
                }

                repository.applyTasbeehStateFromRemote(customPresetsList, customTargetsList, countsMap, targetsMap, ts)
            }

            // Restore Preferences
            val prefsObj = root.optJSONObject("preferences")
            if (prefsObj != null) {
                val map = mutableMapOf<String, Any?>()
                val keys = prefsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = prefsObj.get(k)
                }
                val ts = prefsObj.optLong("timestamp", System.currentTimeMillis())
                repository.applyPreferencesFromRemote(map, ts)

                // Bookmarked Dua IDs
                val duaArr = prefsObj.optJSONArray("bookmarkedDuaIds")
                if (duaArr != null) {
                    val set = mutableSetOf<String>()
                    for (i in 0 until duaArr.length()) set.add(duaArr.getString(i))
                    context.getSharedPreferences("dua_bookmarks_prefs", Context.MODE_PRIVATE)
                        .edit().putStringSet("bookmarked_dua_ids", set).apply()
                }

                // Selected City
                val cityName = prefsObj.optString("selectedCity", "")
                if (cityName.isNotBlank()) {
                    val city = repository.PREDEFINED_CITIES.find { it.cityName.equals(cityName, ignoreCase = true) }
                    if (city != null) {
                        repository.setCity(city)
                    }
                }
            }

            // Update local file cache
            try {
                val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
                backupFile.writeBytes(encryptedBytes)
            } catch (_: Exception) {}

            // Trigger Firestore reconciliation
            syncManager?.notifyPreferencesChanged()
            syncManager?.notifyTasbeehStateChanged()
            syncManager?.notifyQuranProgressChanged()

            Log.d(TAG, "Restored all user data successfully from Google Drive backup")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes local cache and Google Drive backup files during account deletion.
     */
    suspend fun deleteDriveBackup(context: Context, authRepository: AuthRepository): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val googleAccount = GoogleDriveService.getAuthorizedAccount(context)
            if (googleAccount != null) {
                runWithDriveRetry(context, googleAccount) { token ->
                    val searchResult = GoogleDriveService.findBackupFileId(token)
                    if (searchResult.isFailure) return@runWithDriveRetry Result.failure(searchResult.exceptionOrNull()!!)
                    val fileId = searchResult.getOrNull()
                    if (fileId != null) {
                        GoogleDriveService.deleteBackupFile(token, fileId)
                        Log.d(TAG, "Deleted FiveLight backup from Google Drive appDataFolder")
                    }
                    Result.success(Unit)
                }
            }

            val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
            if (backupFile.exists()) {
                backupFile.delete()
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            Log.d(TAG, "Local backup files and metadata cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup file: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // ENCRYPTION & KEY DERIVATION (AES-256-GCM + PBKDF2)
    // =========================================================================

    private fun deriveKey(uid: String, salt: ByteArray): SecretKeySpec {
        val passphrase = "fivelight_backup_key_$uid".toCharArray()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, 10000, 256)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    private fun encryptData(plainText: String, uid: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val iv = ByteArray(12)
        random.nextBytes(iv)

        val keySpec = deriveKey(uid, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val encryptedPayload = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Output format: [16 bytes salt][12 bytes iv][encrypted payload]
        return salt + iv + encryptedPayload
    }

    private fun decryptData(cipherBytes: ByteArray, uid: String): String {
        if (cipherBytes.size < 28) {
            throw IllegalArgumentException("Invalid encrypted payload size")
        }

        val salt = cipherBytes.copyOfRange(0, 16)
        val iv = cipherBytes.copyOfRange(16, 28)
        val encryptedPayload = cipherBytes.copyOfRange(28, cipherBytes.size)

        val keySpec = deriveKey(uid, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        val decryptedBytes = cipher.doFinal(encryptedPayload)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
