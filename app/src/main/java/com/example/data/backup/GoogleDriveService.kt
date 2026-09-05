package com.example.data.backup

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GoogleDriveService {
    private const val TAG = "GoogleDriveService"
    private const val BACKUP_FILENAME = "fivelight_backup_encrypted.bin"
    val DRIVE_APPDATA_SCOPE = Scope("https://www.googleapis.com/auth/drive.appdata")
    private const val OAUTH_SCOPE_STRING = "oauth2:https://www.googleapis.com/auth/drive.appdata"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getGoogleSignInClient(context: Context) = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(DRIVE_APPDATA_SCOPE)
            .build()
    )

    /**
     * Returns currently authorized Google account for Google Drive appDataFolder access.
     */
    fun getAuthorizedAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return if (GoogleSignIn.hasPermissions(account, DRIVE_APPDATA_SCOPE)) account else null
    }

    /**
     * Obtains an OAuth access token for the authorized Google account.
     */
    suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val androidAccount = account.account
                ?: return@withContext Result.failure(Exception("No valid Google account found."))
            val token = GoogleAuthUtil.getToken(context, androidAccount, OAUTH_SCOPE_STRING)
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve Drive OAuth token: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Clears cached OAuth token if invalid/expired.
     */
    suspend fun invalidateToken(context: Context, token: String) = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.clearToken(context, token)
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing token: ${e.message}")
        }
    }

    /**
     * Signs out the currently signed-in Google account for a fresh login.
     */
    suspend fun signOut(context: Context) = withContext(Dispatchers.IO) {
        try {
            val client = getGoogleSignInClient(context)
            com.google.android.gms.tasks.Tasks.await(client.signOut())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign out Google Sign-In client: ${e.message}", e)
        }
    }

    /**
     * Searches appDataFolder for existing FiveLight backup file.
     * Returns fileId if found, null otherwise.
     */
    suspend fun findBackupFileId(accessToken: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name%3D%27$BACKUP_FILENAME%27+and+trashed%3Dfalse&fields=files(id%2Cname%2CmodifiedTime)"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Drive search failed HTTP ${response.code}: $errBody"))
                }
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    val fileObj = files.getJSONObject(0)
                    val fileId = fileObj.getString("id")
                    Result.success(fileId)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding backup file in Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads or updates the encrypted backup payload in Drive appDataFolder.
     */
    suspend fun uploadBackupFile(accessToken: String, fileBytes: ByteArray, existingFileId: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (existingFileId != null) {
                // Update existing file
                val url = "https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=media"
                val mediaType = "application/octet-stream".toMediaType()
                val requestBody = fileBytes.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .patch(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        return@withContext Result.failure(Exception("Drive file update failed HTTP ${response.code}: $errBody"))
                    }
                    Result.success(existingFileId)
                }
            } else {
                // Create new file via multipart
                val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
                val metadataJson = JSONObject().apply {
                    put("name", BACKUP_FILENAME)
                    put("parents", listOf("appDataFolder"))
                }.toString()

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(
                        metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType())
                    )
                    .addPart(
                        fileBytes.toRequestBody("application/octet-stream".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(multipartBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        return@withContext Result.failure(Exception("Drive file upload failed HTTP ${response.code}: $errBody"))
                    }
                    val bodyStr = response.body?.string() ?: ""
                    val json = JSONObject(bodyStr)
                    val newFileId = json.getString("id")
                    Result.success(newFileId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading backup file to Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads encrypted backup file bytes from Drive appDataFolder.
     */
    suspend fun downloadBackupFile(accessToken: String, fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Drive download failed HTTP ${response.code}: $errBody"))
                }
                val bytes = response.body?.bytes()
                    ?: return@withContext Result.failure(Exception("Drive response body is empty."))
                Result.success(bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading backup file from Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes FiveLight backup file from Drive appDataFolder during account deletion.
     */
    suspend fun deleteBackupFile(accessToken: String, fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 404) {
                    val errBody = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Drive file delete failed HTTP ${response.code}: $errBody"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup file from Drive: ${e.message}", e)
            Result.failure(e)
        }
    }
}
