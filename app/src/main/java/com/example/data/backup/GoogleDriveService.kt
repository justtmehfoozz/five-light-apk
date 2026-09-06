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
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GoogleDriveService {
    private const val TAG = "GoogleDriveService"
    private const val BACKUP_FILENAME = "fivelight_backup_encrypted.bin"
    const val FIVELIGHT_WEB_CLIENT_ID = "985622795249-q417s7jbb58g4t304g5a76lq56689d1b.apps.googleusercontent.com"
    val DRIVE_APPDATA_SCOPE = Scope("https://www.googleapis.com/auth/drive.appdata")
    private const val OAUTH_SCOPE_STRING = "oauth2:https://www.googleapis.com/auth/drive.appdata"

    @Volatile
    var lastFoundFileMetadata: String? = null

    @Volatile
    var lastSearchFileId: String? = "NOT_RUN_YET"

    @Volatile
    var diagnosticTraceLog: String = ""

    data class DriveBackupInfo(
        val fileId: String,
        val name: String,
        val modifiedTime: String,
        val sizeBytes: Long,
        val accountEmail: String
    )

    data class CreateDiagnosticReport(
        // 1. Google Account Identity
        val googleSignInEmail: String?,
        val googleSignInId: String?,
        val androidAccountName: String?,
        val androidAccountType: String?,
        val identityMatch: Boolean,
        val googleSignInGrantedScopes: String?,
        val driveAppDataGrantedOnAccount: Boolean,
        val googleAuthUtilRequestedScope: String,
        val tokenAcquisitionSuccess: Boolean,

        // 2. OAuth Client Configuration
        val configuredProjectId: String,
        val configuredProjectNumber: String,
        val configuredPackageName: String,
        val configuredWebClientId: String?,
        val projectIdentityMatch: Boolean,

        // 3. Token Effective Authorization (tokeninfo)
        val tokenInfoStatus: Int?,
        val tokenInfoIssuedTo: String?,
        val tokenInfoAudience: String?,
        val tokenInfoScope: String?,
        val tokenInfoEmail: String?,
        val tokenInfoUserId: String?,
        val tokenInfoExpiresIn: Int?,
        val tokenInfoError: String?,
        val tokenHasAppDataScope: Boolean?,
        val tokenAudienceMatchesWebClientId: Boolean?,

        // 4. Safe Drive API About Diagnostic
        val aboutStatus: Int?,
        val aboutUserEmail: String?,
        val aboutPermissionId: String?,
        val aboutError: String?,

        // 5. Safe Drive API appDataFolder Diagnostic
        val appDataFolderStatus: Int?,
        val appDataFolderId: String?,
        val appDataFolderCanAddChildren: Boolean?,
        val appDataFolderCanListChildren: Boolean?,
        val appDataFolderError: String?,

        // 6. Metadata-only Create Test Result
        val createStatus: Int,
        val createdFileId: String?,
        val createdName: String?,
        val createdParents: String?,
        val createdMimeType: String?,
        val googleError: String?,

        // 7. Media Update Test (if create succeeded)
        val patchStatus: Int?,
        val patchDetails: String?
    ) {
        fun formatReport(): String {
            val sb = StringBuilder()
            sb.append("=== FIVELIGHT DRIVE AUTHORIZATION & CREATE DIAGNOSTIC ===\n\n")

            sb.append("--- 1. GOOGLE ACCOUNT IDENTITY ---\n")
            sb.append("GoogleSignIn Account Email: ${googleSignInEmail ?: "NONE"}\n")
            sb.append("GoogleSignIn Account ID: ${googleSignInId ?: "NONE"}\n")
            sb.append("Android Account Name: ${androidAccountName ?: "NONE"}\n")
            sb.append("Android Account Type: ${androidAccountType ?: "NONE"}\n")
            sb.append("GoogleSignIn Account Identity Match: ${if (identityMatch) "YES" else "NO"}\n")
            sb.append("Android Account Identity Match: ${if (identityMatch) "YES" else "NO"}\n\n")

            sb.append("--- 2. SCOPE VERIFICATION AT TWO LEVELS ---\n")
            sb.append("A. GoogleSignInAccount.grantedScopes: [${googleSignInGrantedScopes ?: "NONE"}]\n")
            sb.append("   drive.appdata granted on GoogleSignIn account: ${if (driveAppDataGrantedOnAccount) "YES" else "NO"}\n")
            sb.append("B. GoogleAuthUtil Scope String Requested: $googleAuthUtilRequestedScope\n")
            sb.append("   Token Acquisition Result: ${if (tokenAcquisitionSuccess) "SUCCESS (Valid token acquired)" else "FAILED"}\n\n")

            sb.append("--- 3. OAUTH CLIENT CONFIGURATION ---\n")
            sb.append("Configured Project ID: $configuredProjectId\n")
            sb.append("Configured Project Number: $configuredProjectNumber\n")
            sb.append("Configured Package Name: $configuredPackageName\n")
            sb.append("Default Web Client ID: ${configuredWebClientId ?: "NOT FOUND"}\n")
            sb.append("OAuth Client / Project Identity Match: ${if (projectIdentityMatch) "YES" else "NO"}\n\n")

            sb.append("--- 4. TOKEN EFFECTIVE AUTHORIZATION (Google tokeninfo) ---\n")
            if (tokenInfoStatus != null) {
                sb.append("tokeninfo HTTP Status: $tokenInfoStatus\n")
                if (tokenInfoStatus in 200..299) {
                    sb.append("Token Issued To: ${tokenInfoIssuedTo ?: "NONE"}\n")
                    sb.append("Token Audience: ${tokenInfoAudience ?: "NONE"}\n")
                    sb.append("Token Associated Email: ${tokenInfoEmail ?: "NONE"}\n")
                    sb.append("Token User ID: ${tokenInfoUserId ?: "NONE"}\n")
                    sb.append("Token Expires In: ${tokenInfoExpiresIn ?: 0}s\n")
                    sb.append("Token Effective Scopes: ${tokenInfoScope ?: "NONE"}\n")
                    sb.append("Token Contains drive.appdata: ${if (tokenHasAppDataScope == true) "YES" else "NO"}\n")
                    sb.append("Token Audience/IssuedTo Matches FiveLight Web Client ID ($FIVELIGHT_WEB_CLIENT_ID): ${if (tokenAudienceMatchesWebClientId == true) "YES" else "NO"}\n")
                } else {
                    sb.append("tokeninfo Error: ${tokenInfoError ?: "UNKNOWN"}\n")
                }
            } else {
                sb.append("tokeninfo: NOT RUN\n")
            }
            sb.append("\n")

            sb.append("--- 5. SAFE DRIVE API DIAGNOSTIC (about.get) ---\n")
            if (aboutStatus != null) {
                sb.append("about.get HTTP Status: $aboutStatus\n")
                if (aboutStatus in 200..299) {
                    sb.append("Drive User Email: ${aboutUserEmail ?: "NONE"}\n")
                    sb.append("Drive Permission ID: ${aboutPermissionId ?: "NONE"}\n")
                } else {
                    sb.append("about.get Error: ${aboutError ?: "UNKNOWN"}\n")
                }
            } else {
                sb.append("about.get: NOT RUN\n")
            }
            sb.append("\n")

            sb.append("--- 6. SAFE DRIVE API DIAGNOSTIC (files.get appDataFolder) ---\n")
            if (appDataFolderStatus != null) {
                sb.append("appDataFolder HTTP Status: $appDataFolderStatus\n")
                if (appDataFolderStatus in 200..299) {
                    sb.append("appDataFolder ID: ${appDataFolderId ?: "NONE"}\n")
                    sb.append("capabilities.canAddChildren: ${appDataFolderCanAddChildren ?: "UNKNOWN"}\n")
                    sb.append("capabilities.canListChildren: ${appDataFolderCanListChildren ?: "UNKNOWN"}\n")
                } else {
                    sb.append("appDataFolder Error: ${appDataFolderError ?: "UNKNOWN"}\n")
                }
            } else {
                sb.append("appDataFolder: NOT RUN\n")
            }
            sb.append("\n")

            sb.append("--- 7. METADATA-ONLY FILES.CREATE TEST ---\n")
            sb.append("POST https://www.googleapis.com/drive/v3/files\n")
            sb.append("Status: $createStatus\n")
            sb.append("Created File ID: ${createdFileId ?: "NONE"}\n")
            sb.append("Parents: ${createdParents ?: "NONE"}\n")
            sb.append("MimeType: ${createdMimeType ?: "NONE"}\n")
            if (googleError != null) {
                sb.append("\nGoogle Error:\n$googleError\n")
            }
            sb.append("\n")

            if (patchStatus != null) {
                sb.append("--- 8. MEDIA UPDATE TEST ---\n")
                sb.append("PATCH Status: $patchStatus\n")
                sb.append("Details: ${patchDetails ?: "NONE"}\n")
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
            val email = account.email ?: "NO_EMAIL"
            val accountId = account.id ?: "NO_ID"
            val grantedScopes = account.grantedScopes.map { it.scopeUri }.joinToString(", ")
            val hasAppData = account.grantedScopes.any { it.scopeUri.equals("https://www.googleapis.com/auth/drive.appdata", ignoreCase = true) }
            val androidAccount = account.account
            val androidAccountName = androidAccount?.name ?: "NULL"
            val androidAccountType = androidAccount?.type ?: "NULL"
            val identityMatch = (!email.isBlank() && email == androidAccountName && androidAccountType == "com.google")

            appendTrace("--- TOKEN ACQUISITION TRACE ---")
            appendTrace("GoogleSignInAccount email: $email, id: $accountId")
            appendTrace("GoogleSignInAccount grantedScopes: [$grantedScopes]")
            appendTrace("GoogleSignInAccount has drive.appdata: $hasAppData")
            appendTrace("Android Account name: $androidAccountName, type: $androidAccountType")
            appendTrace("Identity match (GoogleSignIn == AndroidAccount): ${if (identityMatch) "YES" else "NO"}")
            appendTrace("Scope requested from GoogleAuthUtil: $OAUTH_SCOPE_STRING")

            if (androidAccount == null) {
                appendTrace("Token acquisition FAILED: androidAccount is null")
                return@withContext Result.failure(Exception("No valid Google account found."))
            }

            val token = GoogleAuthUtil.getToken(context, androidAccount, OAUTH_SCOPE_STRING)
            val acquired = !token.isNullOrBlank()
            appendTrace("Token acquisition result: ${if (acquired) "SUCCESS (valid non-empty token obtained)" else "FAILURE (empty token)"}")
            // NEVER log the token itself
            Result.success(token)
        } catch (e: Exception) {
            appendTrace("Token acquisition EXCEPTION: ${e.message}")
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
     * Diagnostic experiment & Authorization Verification:
     * 1. Inspects Google account identity & GoogleAuthUtil token acquisition context.
     * 2. Inspects OAuth Client configuration (Project ID, project number, web client ID).
     * 3. Performs safe, harmless tokeninfo inspection to verify token scopes, issued_to client, and account email.
     * 4. Performs harmless Drive API about.get and files.get(appDataFolder) to check effective capabilities.
     * 5. Attempts metadata-only file creation in appDataFolder (POST /drive/v3/files).
     * 6. If creation succeeds, tests media PATCH.
     * NEVER logs or exposes the access token itself.
     */
    suspend fun runCreateDiagnosticTest(
        context: Context,
        accessToken: String,
        fileBytes: ByteArray,
        googleAccount: GoogleSignInAccount? = null
    ): CreateDiagnosticReport = withContext(Dispatchers.IO) {
        appendTrace("--- STARTING DRIVE AUTHORIZATION & CREATE DIAGNOSTIC TEST ---")

        // 1. Google Account Identity
        val account = googleAccount ?: GoogleSignIn.getLastSignedInAccount(context)
        val googleSignInEmail = account?.email
        val googleSignInId = account?.id
        val androidAccount = account?.account
        val androidAccountName = androidAccount?.name
        val androidAccountType = androidAccount?.type
        val identityMatch = (!googleSignInEmail.isNullOrBlank() && googleSignInEmail == androidAccountName && androidAccountType == "com.google")
        val googleSignInGrantedScopes = account?.grantedScopes?.map { it.scopeUri }?.joinToString(", ")
        val driveAppDataGrantedOnAccount = account?.grantedScopes?.any { it.scopeUri.equals("https://www.googleapis.com/auth/drive.appdata", ignoreCase = true) } == true
        val googleAuthUtilRequestedScope = OAUTH_SCOPE_STRING
        val tokenAcquisitionSuccess = accessToken.isNotBlank()

        appendTrace("Account Identity: email=$googleSignInEmail, androidName=$androidAccountName, match=$identityMatch")
        appendTrace("GoogleSignIn grantedScopes: [$googleSignInGrantedScopes], drive.appdata granted: $driveAppDataGrantedOnAccount")

        // 2. OAuth Client Configuration
        val configuredProjectId = "fivelight"
        val configuredProjectNumber = "985622795249"
        val configuredPackageName = context.packageName
        val configuredWebClientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "Not found in resources"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
        val projectIdentityMatch = configuredWebClientId?.startsWith(configuredProjectNumber) == true
        appendTrace("Project Config: id=$configuredProjectId, number=$configuredProjectNumber, webClientId=$configuredWebClientId, match=$projectIdentityMatch")

        // 3. Token Effective Authorization (tokeninfo via POST FormBody — token is never in URL or logged)
        var tokenInfoStatus: Int? = null
        var tokenInfoIssuedTo: String? = null
        var tokenInfoAudience: String? = null
        var tokenInfoScope: String? = null
        var tokenInfoEmail: String? = null
        var tokenInfoUserId: String? = null
        var tokenInfoExpiresIn: Int? = null
        var tokenInfoError: String? = null
        var tokenHasAppDataScope: Boolean? = null
        var tokenAudienceMatchesWebClientId: Boolean? = null

        try {
            val tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo"
            val formBody = FormBody.Builder()
                .add("access_token", accessToken)
                .build()

            val tokenInfoReq = Request.Builder()
                .url(tokenInfoUrl)
                .post(formBody)
                .build()

            httpClient.newCall(tokenInfoReq).execute().use { response ->
                tokenInfoStatus = response.code
                val bodyStr = response.body?.string() ?: ""
                appendTrace("tokeninfo HTTP status: $tokenInfoStatus")
                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    tokenInfoIssuedTo = json.optString("issued_to", null)
                    tokenInfoAudience = json.optString("audience", null)
                    tokenInfoScope = json.optString("scope", null)
                    tokenInfoEmail = json.optString("email", null)
                    tokenInfoUserId = json.optString("user_id", null)
                    tokenInfoExpiresIn = json.optInt("expires_in", 0)

                    tokenHasAppDataScope = tokenInfoScope?.contains("https://www.googleapis.com/auth/drive.appdata") == true
                    tokenAudienceMatchesWebClientId = (tokenInfoAudience == FIVELIGHT_WEB_CLIENT_ID ||
                            tokenInfoIssuedTo == FIVELIGHT_WEB_CLIENT_ID)

                    appendTrace("tokeninfo: issued_to=$tokenInfoIssuedTo, audience=$tokenInfoAudience, email=$tokenInfoEmail, hasAppDataScope=$tokenHasAppDataScope, matchesWebClientId=$tokenAudienceMatchesWebClientId")
                } else {
                    tokenInfoError = parseGoogleErrorDetailed(tokenInfoStatus!!, bodyStr)
                    appendTrace("tokeninfo error: $tokenInfoError")
                }
            }
        } catch (e: Exception) {
            tokenInfoError = "tokeninfo exception: ${e.message}"
            appendTrace("tokeninfo exception: ${e.message}")
        }

        // 4. Safe Drive API Diagnostic: about.get
        var aboutStatus: Int? = null
        var aboutUserEmail: String? = null
        var aboutPermissionId: String? = null
        var aboutError: String? = null

        try {
            val aboutUrl = "https://www.googleapis.com/drive/v3/about?fields=user(displayName,emailAddress,permissionId)"
            val aboutReq = Request.Builder()
                .url(aboutUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(aboutReq).execute().use { response ->
                aboutStatus = response.code
                val bodyStr = response.body?.string() ?: ""
                appendTrace("Drive about.get HTTP status: $aboutStatus")
                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    val userObj = json.optJSONObject("user")
                    aboutUserEmail = userObj?.optString("emailAddress", null)
                    aboutPermissionId = userObj?.optString("permissionId", null)
                    appendTrace("Drive about.get: email=$aboutUserEmail, permissionId=$aboutPermissionId")
                } else {
                    aboutError = parseGoogleErrorDetailed(aboutStatus!!, bodyStr)
                    appendTrace("Drive about.get error: $aboutError")
                }
            }
        } catch (e: Exception) {
            aboutError = "Drive about.get exception: ${e.message}"
            appendTrace("Drive about.get exception: ${e.message}")
        }

        // 5. Safe Drive API Diagnostic: files.get(appDataFolder)
        var appDataFolderStatus: Int? = null
        var appDataFolderId: String? = null
        var appDataFolderCanAddChildren: Boolean? = null
        var appDataFolderCanListChildren: Boolean? = null
        var appDataFolderError: String? = null

        try {
            val appDataUrl = "https://www.googleapis.com/drive/v3/files/appDataFolder?fields=id,name,capabilities"
            val appDataReq = Request.Builder()
                .url(appDataUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(appDataReq).execute().use { response ->
                appDataFolderStatus = response.code
                val bodyStr = response.body?.string() ?: ""
                appendTrace("files.get(appDataFolder) HTTP status: $appDataFolderStatus")
                if (response.isSuccessful) {
                    val json = JSONObject(bodyStr)
                    appDataFolderId = json.optString("id", null)
                    val capObj = json.optJSONObject("capabilities")
                    appDataFolderCanAddChildren = capObj?.optBoolean("canAddChildren", false)
                    appDataFolderCanListChildren = capObj?.optBoolean("canListChildren", false)
                    appendTrace("appDataFolder: id=$appDataFolderId, canAddChildren=$appDataFolderCanAddChildren, canListChildren=$appDataFolderCanListChildren")
                } else {
                    appDataFolderError = parseGoogleErrorDetailed(appDataFolderStatus!!, bodyStr)
                    appendTrace("files.get(appDataFolder) error: $appDataFolderError")
                }
            }
        } catch (e: Exception) {
            appDataFolderError = "files.get(appDataFolder) exception: ${e.message}"
            appendTrace("files.get(appDataFolder) exception: ${e.message}")
        }

        // 6. Metadata-only file creation: POST https://www.googleapis.com/drive/v3/files
        var postStatus = 0
        var createdFileId: String? = null
        var createdName: String? = null
        var createdParents: String? = null
        var createdMimeType: String? = null
        var patchStatus: Int? = null
        var patchDetails: String? = null
        var googleError: String? = null

        try {
            val createUrl = "https://www.googleapis.com/drive/v3/files"
            val parentsArray = JSONArray().apply { put("appDataFolder") }
            val createJson = JSONObject().apply {
                put("name", BACKUP_FILENAME)
                put("parents", parentsArray)
                put("mimeType", "application/octet-stream")
            }.toString()

            appendTrace("Metadata-only POST /drive/v3/files request body: $createJson")
            appendTrace("Explicitly using literal parent keyword: appDataFolder (NOT resolved ID ${appDataFolderId ?: "NONE"})")

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

            // 7. If creation succeeded, perform media PATCH
            if (createdFileId != null) {
                appendTrace("Executing media PATCH for created file ID: $createdFileId")
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
            appendTrace("Metadata-only create exception: ${e.message}")
            if (googleError == null) {
                googleError = "Exception during metadata create test: ${e.message}"
            }
        }

        val report = CreateDiagnosticReport(
            googleSignInEmail = googleSignInEmail,
            googleSignInId = googleSignInId,
            androidAccountName = androidAccountName,
            androidAccountType = androidAccountType,
            identityMatch = identityMatch,
            googleSignInGrantedScopes = googleSignInGrantedScopes,
            driveAppDataGrantedOnAccount = driveAppDataGrantedOnAccount,
            googleAuthUtilRequestedScope = googleAuthUtilRequestedScope,
            tokenAcquisitionSuccess = tokenAcquisitionSuccess,
            configuredProjectId = configuredProjectId,
            configuredProjectNumber = configuredProjectNumber,
            configuredPackageName = configuredPackageName,
            configuredWebClientId = configuredWebClientId,
            projectIdentityMatch = projectIdentityMatch,
            tokenInfoStatus = tokenInfoStatus,
            tokenInfoIssuedTo = tokenInfoIssuedTo,
            tokenInfoAudience = tokenInfoAudience,
            tokenInfoScope = tokenInfoScope,
            tokenInfoEmail = tokenInfoEmail,
            tokenInfoUserId = tokenInfoUserId,
            tokenInfoExpiresIn = tokenInfoExpiresIn,
            tokenInfoError = tokenInfoError,
            tokenHasAppDataScope = tokenHasAppDataScope,
            tokenAudienceMatchesWebClientId = tokenAudienceMatchesWebClientId,
            aboutStatus = aboutStatus,
            aboutUserEmail = aboutUserEmail,
            aboutPermissionId = aboutPermissionId,
            aboutError = aboutError,
            appDataFolderStatus = appDataFolderStatus,
            appDataFolderId = appDataFolderId,
            appDataFolderCanAddChildren = appDataFolderCanAddChildren,
            appDataFolderCanListChildren = appDataFolderCanListChildren,
            appDataFolderError = appDataFolderError,
            createStatus = postStatus,
            createdFileId = createdFileId,
            createdName = createdName,
            createdParents = createdParents,
            createdMimeType = createdMimeType,
            googleError = googleError,
            patchStatus = patchStatus,
            patchDetails = patchDetails
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
     * Searches appDataFolder for existing FiveLight backup file and returns metadata.
     */
    suspend fun findBackupFileInfo(accessToken: String, accountEmail: String): Result<DriveBackupInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name%3D%27$BACKUP_FILENAME%27+and+trashed%3Dfalse&fields=files(id%2Cname%2Cparents%2CmimeType%2CmodifiedTime%2Csize%2Ccapabilities%2Ctrashed)"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    val errParsed = parseGoogleError(response.code, errBody)
                    return@withContext Result.failure(Exception(errParsed))
                }
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val files = json.optJSONArray("files")
                val size = files?.length() ?: 0
                if (files != null && size > 0) {
                    val fileObj = files.getJSONObject(0)
                    val fileId = fileObj.getString("id")
                    val name = fileObj.optString("name", BACKUP_FILENAME)
                    val modifiedTime = fileObj.optString("modifiedTime", "")
                    val sizeBytes = fileObj.optLong("size", 0L)
                    Result.success(DriveBackupInfo(fileId, name, modifiedTime, sizeBytes, accountEmail))
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
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
        existingFileId: String?,
        googleAccount: GoogleSignInAccount? = null
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

                // Standard multipart creation with literal "appDataFolder" parent
                val parentsArray = JSONArray().apply { put("appDataFolder") }
                val metadataJson = JSONObject().apply {
                    put("name", BACKUP_FILENAME)
                    put("parents", parentsArray)
                    put("mimeType", "application/octet-stream")
                }.toString()

                appendTrace("Multipart create metadata JSON: $metadataJson")
                appendTrace("Literal parents keyword: appDataFolder (NOT resolved folder ID)")

                val url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
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
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        val newFileId = json.getString("id")
                        appendTrace("POST create succeeded! New file ID: $newFileId")
                        return@withContext Result.success(newFileId)
                    }

                    val errBody = response.body?.string() ?: ""
                    if (response.code == 403) {
                        Log.d(TAG, "Stage D: Inside HTTP 403 branch for create upload")
                        appendTrace("POST multipart failed with HTTP 403. Body: $errBody")
                    } else {
                        appendTrace("POST multipart failed with HTTP ${response.code}. Body: $errBody")
                    }
                    val mainErr = parseGoogleError(response.code, errBody)

                    // Run diagnostic test as fallback investigation
                    val diagResult = runCreateDiagnosticTest(context, accessToken, fileBytes, googleAccount)
                    if ((diagResult.createStatus == 200 || diagResult.createStatus == 201) &&
                        diagResult.createdFileId != null &&
                        diagResult.patchStatus in 200..299
                    ) {
                        appendTrace("Fallback diagnostic create + PATCH succeeded with ID: ${diagResult.createdFileId}")
                        return@withContext Result.success(diagResult.createdFileId)
                    }

                    val exceptionToReturn = Exception("${diagResult.formatReport()}\n\n--- MULTIPART POST ATTEMPT ---\n$mainErr")
                    Log.d(TAG, "Stage E (Create): Returning upload exception: ${exceptionToReturn.message}")
                    return@withContext Result.failure(exceptionToReturn)
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
