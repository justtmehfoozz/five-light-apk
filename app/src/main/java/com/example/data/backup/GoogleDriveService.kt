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

    @Volatile
    var lastFoundFileMetadata: String? = null

    @Volatile
    var lastSearchFileId: String? = "NOT_RUN_YET"

    @Volatile
    var diagnosticTraceLog: String = ""

    data class CreateDiagnosticReport(
        val createStatus: Int,
        val createdFileId: String?,
        val createdName: String?,
        val createdParents: String?,
        val createdMimeType: String?,
        val getStatus: Int?,
        val getDetails: String?,
        val canEdit: Boolean?,
        val canDelete: Boolean?,
        val patchStatus: Int?,
        val patchDetails: String?,
        val googleError: String?,
        val clientIdentity: String?
    ) {
        fun formatReport(): String {
            val sb = StringBuilder()
            sb.append("--- CREATE TEST ---\n\n")
            sb.append("Metadata-only POST status: $createStatus\n")
            sb.append("Created file ID: ${createdFileId ?: "NONE"}\n")
            sb.append("Parents: ${createdParents ?: "NONE"}\n")
            sb.append("Mime type: ${createdMimeType ?: "NONE"}\n")
            if (getStatus != null) {
                sb.append("\nGET Verification (status $getStatus):\n")
                sb.append("capabilities.canEdit: ${canEdit ?: "UNKNOWN"}\n")
                sb.append("capabilities.canDelete: ${canDelete ?: "UNKNOWN"}\n")
                if (!getDetails.isNullOrBlank()) {
                    sb.append("File verification: $getDetails\n")
                }
            }

            sb.append("\n--- MEDIA UPDATE TEST ---\n\n")
            sb.append("PATCH status: ${patchStatus?.toString() ?: "NOT ATTEMPTED (Metadata-only create failed)"}\n")
            if (!patchDetails.isNullOrBlank()) {
                sb.append("PATCH details: $patchDetails\n")
            }

            sb.append("\n--- GOOGLE ERROR ---\n\n")
            sb.append(googleError ?: "NONE\n")

            if (!clientIdentity.isNullOrBlank()) {
                sb.append("\nOAuth Client/App Identity:\n$clientIdentity\n")
            }
            return sb.toString().trim()
        }
    }

    @Volatile
    var lastCreateDiagnosticReport: CreateDiagnosticReport? = null

    fun appendTrace(message: String) {
        val current = diagnosticTraceLog
        diagnosticTraceLog = if (current.isEmpty()) message else "$current\n$message"
        Log.d(TAG, "TRACE: $message")
    }

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

    fun parseGoogleError(httpCode: Int, bodyStr: String): String {
        try {
            if (bodyStr.trim().startsWith("{")) {
                val json = JSONObject(bodyStr)
                val errorObj = json.optJSONObject("error")
                if (errorObj != null) {
                    val code = errorObj.optInt("code", httpCode)
                    val message = errorObj.optString("message", "")
                    val errorsArr = errorObj.optJSONArray("errors")
                    var reason = ""
                    var domain = ""
                    if (errorsArr != null && errorsArr.length() > 0) {
                        val firstError = errorsArr.getJSONObject(0)
                        reason = firstError.optString("reason", "")
                        domain = firstError.optString("domain", "")
                    }
                    return "Google API Error (HTTP $httpCode, code: $code, message: \"$message\", reason: \"$reason\", domain: \"$domain\")"
                }
            }
        } catch (e: Exception) {
            // Fallback to raw string
        }
        return "HTTP $httpCode: $bodyStr"
    }

    fun parseGoogleErrorDetailed(httpCode: Int, bodyStr: String): String {
        return try {
            val json = JSONObject(bodyStr)
            val errorObj = json.optJSONObject("error")
            if (errorObj != null) {
                val code = errorObj.optInt("code", httpCode)
                val message = errorObj.optString("message", "")
                val errorsArr = errorObj.optJSONArray("errors")
                val sb = StringBuilder()
                sb.append("HTTP status: $httpCode\n")
                sb.append("error.code: $code\n")
                sb.append("error.message: \"$message\"\n")
                if (errorsArr != null && errorsArr.length() > 0) {
                    for (i in 0 until errorsArr.length()) {
                        val errItem = errorsArr.getJSONObject(i)
                        sb.append("error.errors[$i].reason: \"${errItem.optString("reason")}\"\n")
                        sb.append("error.errors[$i].domain: \"${errItem.optString("domain")}\"\n")
                        val itemMsg = errItem.optString("message")
                        if (itemMsg.isNotBlank() && itemMsg != message) {
                            sb.append("error.errors[$i].message: \"$itemMsg\"\n")
                        }
                    }
                }
                sb.toString().trim()
            } else {
                "HTTP status: $httpCode\nResponse: $bodyStr"
            }
        } catch (e: Exception) {
            "HTTP status: $httpCode\nResponse: $bodyStr"
        }
    }

    /**
     * Diagnostic experiment:
     * Attempts metadata-only file creation in appDataFolder (POST /drive/v3/files).
     * If successful, verifies via GET and performs media PATCH (/upload/drive/v3/files/{id}?uploadType=media).
     * If failed, captures the complete error response and client identity.
     */
    suspend fun runCreateDiagnosticTest(
        context: Context,
        accessToken: String,
        fileBytes: ByteArray
    ): CreateDiagnosticReport = withContext(Dispatchers.IO) {
        appendTrace("--- STARTING METADATA-ONLY FILE CREATION DIAGNOSTIC TEST ---")
        var postStatus = 0
        var createdFileId: String? = null
        var createdName: String? = null
        var createdParents: String? = null
        var createdMimeType: String? = null
        var getStatus: Int? = null
        var getDetails: String? = null
        var canEdit: Boolean? = null
        var canDelete: Boolean? = null
        var patchStatus: Int? = null
        var patchDetails: String? = null
        var googleError: String? = null
        var clientIdentity: String? = null

        try {
            // OAuth Client / App Identity check
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val email = account?.email ?: "Unknown"
            val accountId = account?.id ?: "Unknown"
            val packageName = context.packageName
            val defaultWebClientId = try {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) context.getString(resId) else "Not found in resources"
            } catch (e: Exception) {
                "Error retrieving: ${e.message}"
            }
            clientIdentity = "Account Email: $email\nAccount ID: $accountId\nPackage Name: $packageName\nDefault Web Client ID: $defaultWebClientId"

            // 1. Metadata-only file creation: POST https://www.googleapis.com/drive/v3/files
            val createUrl = "https://www.googleapis.com/drive/v3/files"
            val createJson = JSONObject().apply {
                put("name", BACKUP_FILENAME)
                put("parents", listOf("appDataFolder"))
                put("mimeType", "application/octet-stream")
            }.toString()

            val createReq = Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(createJson.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .build()

            httpClient.newCall(createReq).execute().use { response ->
                postStatus = response.code
                val bodyStr = response.body?.string() ?: ""
                appendTrace("Metadata-only POST /drive/v3/files status: $postStatus")

                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    createdFileId = json.optString("id", null)
                    createdName = json.optString("name", null)
                    createdParents = json.optJSONArray("parents")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }.joinToString(", ")
                    } ?: "None"
                    createdMimeType = json.optString("mimeType", null)
                    appendTrace("Metadata-only POST succeeded! File ID: $createdFileId, Parents: $createdParents, Name: $createdName, MimeType: $createdMimeType")
                } else {
                    googleError = parseGoogleErrorDetailed(postStatus, bodyStr)
                    appendTrace("Metadata-only POST failed! Status: $postStatus\n$googleError")
                }
            }

            // 2. If creation succeeded, perform GET verification and media PATCH
            if (createdFileId != null) {
                // Step 5: Verification GET
                appendTrace("Executing GET verification for file ID: $createdFileId")
                val getUrl = "https://www.googleapis.com/drive/v3/files/$createdFileId?fields=id,name,parents,mimeType,modifiedTime,trashed,capabilities,capabilities/canEdit,capabilities/canDelete"
                val getReq = Request.Builder()
                    .url(getUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

                httpClient.newCall(getReq).execute().use { getResp ->
                    getStatus = getResp.code
                    val getBody = getResp.body?.string() ?: ""
                    appendTrace("GET verification status: $getStatus")
                    if (getResp.isSuccessful) {
                        val getJson = JSONObject(getBody)
                        val capObj = getJson.optJSONObject("capabilities")
                        canEdit = capObj?.optBoolean("canEdit")
                        canDelete = capObj?.optBoolean("canDelete")
                        val pArr = getJson.optJSONArray("parents")?.let { arr ->
                            (0 until arr.length()).map { arr.getString(it) }.joinToString(", ")
                        } ?: "None"
                        getDetails = "id: ${getJson.optString("id")}\nname: ${getJson.optString("name")}\nparents: [$pArr]\nmimeType: ${getJson.optString("mimeType")}\nmodifiedTime: ${getJson.optString("modifiedTime")}\ntrashed: ${getJson.optBoolean("trashed")}"
                        appendTrace("GET verification: canEdit=$canEdit, canDelete=$canDelete, parents=[$pArr]")
                    } else {
                        getDetails = parseGoogleErrorDetailed(getResp.code, getBody)
                        appendTrace("GET verification failed: $getDetails")
                    }
                }

                // Step 3: Media PATCH
                appendTrace("Executing media PATCH for file ID: $createdFileId")
                val patchUrl = "https://www.googleapis.com/upload/drive/v3/files/$createdFileId?uploadType=media"
                val patchReq = Request.Builder()
                    .url(patchUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .patch(fileBytes.toRequestBody("application/octet-stream".toMediaType()))
                    .build()

                httpClient.newCall(patchReq).execute().use { patchResp ->
                    patchStatus = patchResp.code
                    val patchBody = patchResp.body?.string() ?: ""
                    appendTrace("Media PATCH status: $patchStatus")
                    if (patchResp.isSuccessful) {
                        patchDetails = "SUCCESS (HTTP $patchStatus). Media payload successfully uploaded to file ID: $createdFileId"
                        appendTrace("Media PATCH update succeeded!")
                    } else {
                        patchDetails = parseGoogleErrorDetailed(patchStatus!!, patchBody)
                        appendTrace("Media PATCH failed: $patchDetails")
                        if (googleError == null) {
                            googleError = patchDetails
                        }
                    }
                }
            }
        } catch (e: Exception) {
            appendTrace("Diagnostic test encountered exception: ${e.message}")
            if (googleError == null) {
                googleError = "Exception during diagnostic test: ${e.message}"
            }
        }

        val report = CreateDiagnosticReport(
            createStatus = postStatus,
            createdFileId = createdFileId,
            createdName = createdName,
            createdParents = createdParents,
            createdMimeType = createdMimeType,
            getStatus = getStatus,
            getDetails = getDetails,
            canEdit = canEdit,
            canDelete = canDelete,
            patchStatus = patchStatus,
            patchDetails = patchDetails,
            googleError = googleError,
            clientIdentity = clientIdentity
        )
        lastCreateDiagnosticReport = report
        report
    }

    /**
     * Searches appDataFolder for existing FiveLight backup file.
     * Returns fileId if found, null otherwise.
     */
    suspend fun findBackupFileId(accessToken: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            lastFoundFileMetadata = null
            lastSearchFileId = null
            appendTrace("--- START findBackupFileId ---")

            val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name%3D%27$BACKUP_FILENAME%27+and+trashed%3Dfalse&fields=files(id%2Cname%2Cparents%2CmimeType%2CmodifiedTime%2Ccapabilities%2Ctrashed)"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                appendTrace("findBackupFileId HTTP status: ${response.code}")
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    val errParsed = parseGoogleError(response.code, errBody)
                    lastFoundFileMetadata = "NOT AVAILABLE — metadata retrieval/cache failed\nReason: Search query (files.list) failed with $errParsed"
                    Log.d(TAG, "Stage D/A (Search): Search query failed: $lastFoundFileMetadata")
                    appendTrace("findBackupFileId failed: $errParsed")
                    return@withContext Result.failure(Exception(errParsed))
                }
                val bodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Stage A: Search files.list response body: $bodyStr")
                val json = JSONObject(bodyStr)
                val files = json.optJSONArray("files")
                val size = files?.length() ?: 0
                appendTrace("findBackupFileId files array size: $size")
                if (files != null && size > 0) {
                    val fileObj = files.getJSONObject(0)
                    val fileId = fileObj.getString("id")
                    lastSearchFileId = fileId
                    
                    val name = fileObj.optString("name", "N/A")
                    val parents = fileObj.optJSONArray("parents")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }.joinToString(", ")
                    } ?: "N/A"
                    val mimeType = fileObj.optString("mimeType", "N/A")
                    val modifiedTime = fileObj.optString("modifiedTime", "N/A")
                    val trashed = fileObj.optBoolean("trashed", false).toString()
                    val capabilities = fileObj.optJSONObject("capabilities")?.let { cap ->
                        val keys = cap.keys()
                        val list = mutableListOf<String>()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            list.add("$key: ${cap.opt(key)}")
                        }
                        list.joinToString("\n    ")
                    } ?: "N/A"
                    
                    lastFoundFileMetadata = """
                        id: "$fileId"
                        name: "$name"
                        parents: [$parents]
                        mimeType: "$mimeType"
                        modifiedTime: "$modifiedTime"
                        trashed: $trashed
                        capabilities:
                            $capabilities
                    """.trimIndent()
                    Log.d(TAG, "Stage B: lastFoundFileMetadata assigned:\n$lastFoundFileMetadata")
                    appendTrace("findBackupFileId returned: $fileId")
                    appendTrace("File Metadata: Name=$name, Parents=[$parents], MimeType=$mimeType, ModifiedTime=$modifiedTime, Trashed=$trashed")
                    Result.success(fileId)
                } else {
                    lastSearchFileId = null
                    lastFoundFileMetadata = "No existing backup file found in Drive appDataFolder."
                    appendTrace("Drive search returned 0 matching files.")
                    appendTrace("findBackupFileId returned: NULL")
                    Log.d(TAG, "findBackupFileId: No file found")
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            lastFoundFileMetadata = "NOT AVAILABLE — metadata retrieval/cache failed\nReason: Search query failed with exception: ${e.message}"
            appendTrace("findBackupFileId exception: ${e.message}")
            Log.e(TAG, "Error finding backup file in Drive: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads or updates the encrypted backup payload in Drive appDataFolder.
     */
    suspend fun uploadBackupFile(
        context: Context,
        accessToken: String,
        fileBytes: ByteArray,
        existingFileId: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            appendTrace("uploadBackupFile received existingFileId: ${existingFileId ?: "NULL"}")
            if (existingFileId != null) {
                appendTrace("UPLOAD PATH = UPDATE EXISTING FILE")
                appendTrace("UPDATE FILE ID = $existingFileId")
                appendTrace("HTTP Method: PATCH")
                appendTrace("Request URL Path: /upload/drive/v3/files/$existingFileId?uploadType=media")

                Log.d(TAG, "Stage C: Executing PATCH update for file ID: $existingFileId")
                val url = "https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=media"
                val mediaType = "application/octet-stream".toMediaType()
                val requestBody = fileBytes.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .patch(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    appendTrace("PATCH response status: ${response.code}")
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        if (response.code == 403) {
                            Log.d(TAG, "Stage D: Inside HTTP 403 branch for update upload")
                            appendTrace("PATCH failed with HTTP 403. Body: $errBody")
                        } else {
                            appendTrace("PATCH failed with HTTP ${response.code}. Body: $errBody")
                        }
                        val mainErr = parseGoogleError(response.code, errBody)
                        val metadataText = lastFoundFileMetadata ?: "NOT AVAILABLE — metadata retrieval/cache failed\nReason: lastFoundFileMetadata was null at update upload"
                        val exceptionToReturn = Exception("$mainErr\n\n--- EXISTING FILE METADATA ---\n$metadataText")
                        Log.d(TAG, "Stage E: Returning upload exception: ${exceptionToReturn.message}")
                        return@withContext Result.failure(exceptionToReturn)
                    }
                    appendTrace("PATCH update succeeded!")
                    Result.success(existingFileId)
                }
            } else {
                appendTrace("UPLOAD PATH = CREATE NEW FILE")

                // Diagnostic test: Metadata-only creation in appDataFolder
                val diagResult = runCreateDiagnosticTest(context, accessToken, fileBytes)

                if (diagResult.createStatus == 200 || diagResult.createStatus == 201) {
                    appendTrace("Diagnostic: Metadata-only creation SUCCEEDED with ID: ${diagResult.createdFileId}")
                    val exceptionToReturn = Exception(diagResult.formatReport())
                    return@withContext Result.failure(exceptionToReturn)
                } else {
                    appendTrace("Diagnostic: Metadata-only creation FAILED with HTTP ${diagResult.createStatus}")
                    // Execute existing multipart creation request as well to verify whether it also fails with identical error
                    appendTrace("Executing existing multipart/related POST to compare...")
                    val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
                    val metadataJson = JSONObject().apply {
                        put("name", BACKUP_FILENAME)
                        put("parents", listOf("appDataFolder"))
                    }.toString()

                    val multipartBody = MultipartBody.Builder()
                        .setType("multipart/related".toMediaType())
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
                        appendTrace("POST multipart response status: ${response.code}")
                        if (!response.isSuccessful) {
                            val errBody = response.body?.string() ?: ""
                            if (response.code == 403) {
                                Log.d(TAG, "Stage D: Inside HTTP 403 branch for create upload")
                                appendTrace("POST multipart failed with HTTP 403. Body: $errBody")
                            } else {
                                appendTrace("POST multipart failed with HTTP ${response.code}. Body: $errBody")
                            }
                            val mainErr = parseGoogleError(response.code, errBody)
                            val exceptionToReturn = Exception("${diagResult.formatReport()}\n\n--- MULTIPART POST ATTEMPT ---\n$mainErr")
                            Log.d(TAG, "Stage E (Create): Returning upload exception: ${exceptionToReturn.message}")
                            return@withContext Result.failure(exceptionToReturn)
                        }
                        val bodyStr = response.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        val newFileId = json.getString("id")
                        appendTrace("POST create succeeded! New file ID: $newFileId")
                        Result.success(newFileId)
                    }
                }
            }
        } catch (e: Exception) {
            appendTrace("uploadBackupFile exception: ${e.message}")
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
                    return@withContext Result.failure(Exception(parseGoogleError(response.code, errBody)))
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
                    return@withContext Result.failure(Exception(parseGoogleError(response.code, errBody)))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup file from Drive: ${e.message}", e)
            Result.failure(e)
        }
    }
}
