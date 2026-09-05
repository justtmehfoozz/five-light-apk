package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class GoogleAuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    object Cancelled : GoogleAuthException("Sign-in cancelled")
    class ConfigurationError(message: String) : GoogleAuthException(message)
    class EnvironmentLimitation(message: String, cause: Throwable? = null) : GoogleAuthException(message, cause)
    class ProviderError(message: String, cause: Throwable? = null) : GoogleAuthException(message, cause)
    class NoAccountsAvailable(message: String) : GoogleAuthException(message)
    class FirebaseAuthFailure(message: String, cause: Throwable? = null) : GoogleAuthException(message, cause)
}

class AuthRepository(private val context: Context) {

    private val firebaseAuth: FirebaseAuth by lazy {
        com.example.FiveLightApp.ensureFirebaseInitialized(context)
        FirebaseAuth.getInstance()
    }
    private val prefs = context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE)

    private val _hasSeenAccountPrompt = MutableStateFlow(
        prefs.getBoolean("has_seen_account_prompt", false)
    )
    val hasSeenAccountPrompt: StateFlow<Boolean> = _hasSeenAccountPrompt.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(
        try {
            com.example.FiveLightApp.ensureFirebaseInitialized(context)
            firebaseAuth.currentUser
        } catch (_: Exception) {
            null
        }
    )
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth.addAuthStateListener { auth ->
                _currentUser.value = auth.currentUser
            }
        } catch (_: Exception) {}
    }

    fun setHasSeenAccountPrompt(seen: Boolean = true) {
        _hasSeenAccountPrompt.value = seen
        prefs.edit().putBoolean("has_seen_account_prompt", seen).apply()
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            com.example.FiveLightApp.ensureFirebaseInitialized(context)
            Log.d(tag, "Attempting email sign in with Firebase project: ${firebaseAuth.app.options.projectId}")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Sign in succeeded but user is null")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(tag, "Email sign-in failure: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String, displayName: String? = null): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            com.example.FiveLightApp.ensureFirebaseInitialized(context)
            Log.d(tag, "Attempting email registration with Firebase project: ${firebaseAuth.app.options.projectId}")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Registration succeeded but user is null")
            val cleanName = displayName?.trim()
            if (!cleanName.isNullOrBlank()) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    user.reload().await()
                } catch (pe: Exception) {
                    Log.w(tag, "Failed to update profile name on registration", pe)
                }
            }
            try {
                user.sendEmailVerification().await()
                Log.d(tag, "Verification email sent to ${user.email}")
            } catch (ve: Exception) {
                Log.w(tag, "Failed to send verification email during registration", ve)
            }
            _currentUser.value = firebaseAuth.currentUser ?: user
            Result.success(_currentUser.value ?: user)
        } catch (e: Exception) {
            Log.e(tag, "Email registration failure: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(newName: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user found")
            val cleanName = newName.trim()
            if (cleanName.isBlank()) {
                throw Exception("Display name cannot be empty")
            }
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            user.updateProfile(profileUpdates).await()
            user.reload().await()
            val updatedUser = firebaseAuth.currentUser ?: user
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Log.e(tag, "Update display name failure: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user found")
            val isGoogle = user.providerData.any { it.providerId == "google.com" }
            if (isGoogle) {
                throw Exception("Google accounts do not require email verification")
            }
            if (user.isEmailVerified) {
                throw Exception("Email is already verified")
            }
            user.sendEmailVerification().await()
            Log.d(tag, "Verification email successfully sent to ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Send email verification failure: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user found")
            user.reload().await()
            val refreshedUser = firebaseAuth.currentUser ?: user
            _currentUser.value = refreshedUser
            Log.d(tag, "Refreshed user profile. isEmailVerified: ${refreshedUser.isEmailVerified}")
            Result.success(refreshedUser)
        } catch (e: Exception) {
            Log.e(tag, "Reload user error: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user found")
            val email = user.email
            val trimmedNewPass = newPassword.trim()
            val trimmedCurrPass = currentPassword.trim()

            if (trimmedNewPass.length < 6) {
                throw Exception("New password must be at least 6 characters")
            }

            try {
                user.updatePassword(trimmedNewPass).await()
                Result.success(Unit)
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                if (email.isNullOrBlank() || trimmedCurrPass.isBlank()) {
                    throw Exception("Recent authentication required. Please enter your current password.")
                }
                Log.d(tag, "Recent authentication required for password update. Re-authenticating...")
                val credential = EmailAuthProvider.getCredential(email, trimmedCurrPass)
                user.reauthenticate(credential).await()
                user.updatePassword(trimmedNewPass).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(tag, "Change password error: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun resolveWebClientId(context: Context): String? {
        return try {
            context.getString(com.example.R.string.default_web_client_id).trim().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } ?: run {
            val res = context.resources
            val id = res.getIdentifier("default_web_client_id", "string", context.packageName).takeIf { it != 0 }
                ?: res.getIdentifier("default_web_client_id", "string", "com.example").takeIf { it != 0 }
                ?: 0
            if (id != 0) {
                try { res.getString(id).trim().takeIf { it.isNotBlank() } } catch (_: Exception) { null }
            } else null
        }
    }

    suspend fun reauthenticateWithGoogle(activityContext: Context, user: FirebaseUser): Result<Unit> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            com.example.FiveLightApp.ensureFirebaseInitialized(activityContext)
            val webClientId = resolveWebClientId(activityContext)
                ?: return@withContext Result.failure(
                    GoogleAuthException.ConfigurationError(
                        "Google Sign-In is not configured for this project. Please verify default_web_client_id in Firebase configuration."
                    )
                )

            val credentialManager = CredentialManager.create(activityContext)

            Log.d(tag, "Google Re-Authentication Stage 1: Attempting with filterByAuthorizedAccounts=true")
            val initialOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val initialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(initialOption)
                .build()

            val credential = try {
                val result = credentialManager.getCredential(activityContext, initialRequest)
                Log.d(tag, "Google Re-Auth Stage 1 succeeded.")
                result.credential
            } catch (e: GetCredentialCancellationException) {
                Log.d(tag, "Google Re-Auth cancelled by user during Stage 1")
                return@withContext Result.failure(GoogleAuthException.Cancelled)
            } catch (e: Exception) {
                Log.d(tag, "Google Re-Auth Stage 1 error [${e.javaClass.simpleName}]. Proceeding to account chooser...")
                retryWithAccountChooser(activityContext, credentialManager, webClientId)
            }

            if (credential == null) {
                return@withContext Result.failure(GoogleAuthException.Cancelled)
            }

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                user.reauthenticate(authCredential).await()
                Log.d(tag, "Google re-authentication successfully re-authenticated Firebase user UID: ${user.uid}")
                Result.success(Unit)
            } else {
                Log.e(tag, "Unsupported credential type during re-auth: ${credential.type}")
                Result.failure(GoogleAuthException.ProviderError("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GoogleAuthException) {
            Result.failure(e)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleAuthException.Cancelled)
        } catch (e: FirebaseAuthException) {
            Log.e(tag, "Firebase re-authentication failed: ${e.errorCode} - ${e.message}", e)
            Result.failure(GoogleAuthException.FirebaseAuthFailure("Firebase re-auth failed: [${e.errorCode}] ${e.localizedMessage ?: e.message ?: "Unknown error"}", e))
        } catch (e: Exception) {
            Log.e(tag, "Google re-authentication error: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(GoogleAuthException.ProviderError("Google re-authentication error: [${e.javaClass.simpleName}] ${e.message ?: "no message"}", e))
        }
    }

    suspend fun deleteAccount(
        syncManager: com.example.data.sync.FirestoreSyncManager,
        passwordForReauth: String? = null,
        activityContext: Context? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user found")
            val uid = user.uid
            val isGoogle = user.providerData.any { it.providerId == "google.com" }

            // 1. MUST succeed Cloud Deletion BEFORE Auth account deletion
            val cloudDeleteResult = syncManager.deleteUserCloudData(uid)
            if (cloudDeleteResult.isFailure) {
                val cause = cloudDeleteResult.exceptionOrNull()
                Log.e(tag, "Cloud data cleanup failed for UID $uid. Halting account deletion to prevent orphan data.", cause)
                return@withContext Result.failure(
                    cause ?: Exception("Failed to delete cloud data. Account deletion aborted to protect data integrity.")
                )
            }

            // 2. Cloud deletion succeeded -> Attempt Firebase Auth account deletion
            try {
                user.delete().await()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                Log.d(tag, "Recent login required for account deletion. Attempting re-authentication...")
                if (!isGoogle) {
                    val email = user.email ?: throw Exception("User email unavailable")
                    val pass = passwordForReauth?.trim().orEmpty()
                    if (pass.isBlank()) {
                        throw Exception("REAUTH_REQUIRED_PASSWORD")
                    }
                    val credential = EmailAuthProvider.getCredential(email, pass)
                    user.reauthenticate(credential).await()
                    user.delete().await()
                } else {
                    val targetContext = activityContext ?: context
                    val reauthResult = reauthenticateWithGoogle(targetContext, user)
                    if (reauthResult.isFailure) {
                        val reauthError = reauthResult.exceptionOrNull() ?: Exception("Google re-authentication failed")
                        Log.e(tag, "Google re-authentication failed during account deletion", reauthError)
                        return@withContext Result.failure(reauthError)
                    }
                    user.delete().await()
                }
            }

            // 3. Auth account successfully deleted -> Stop sync and sign out session
            syncManager.stopSync()
            signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Account deletion error: ${e.javaClass.name} - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            // 0. Ensure Firebase is initialized
            com.example.FiveLightApp.ensureFirebaseInitialized(activityContext)

            // 1. Resolve Server Web Client ID from compile-time generated resources
            val webClientId = resolveWebClientId(activityContext)
            if (webClientId.isNullOrBlank()) {
                Log.w(tag, "Google Sign-In configuration check: default_web_client_id resource not found in configuration.")
                return@withContext Result.failure(
                    GoogleAuthException.ConfigurationError(
                        "Google Sign-In is not configured for this project. Please verify default_web_client_id in Firebase configuration."
                    )
                )
            }

            val credentialManager = CredentialManager.create(activityContext)

            // 2. Stage 1: Query for previously authorized accounts (silent fast-path for returning users)
            Log.d(tag, "Google Sign-In Stage 1: Attempting with filterByAuthorizedAccounts=true")
            val initialOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val initialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(initialOption)
                .build()

            val credential = try {
                val result = credentialManager.getCredential(activityContext, initialRequest)
                Log.d(tag, "Google Sign-In Stage 1 succeeded with existing authorized account.")
                result.credential
            } catch (e: GetCredentialCancellationException) {
                Log.d(tag, "Google Sign-In cancelled by user during Stage 1")
                return@withContext Result.failure(GoogleAuthException.Cancelled)
            } catch (e: NoCredentialException) {
                // No authorized credential on first attempt -> automatically retry with filterByAuthorizedAccounts(false)
                Log.d(tag, "Stage 1 returned NoCredentialException. Proceeding to Stage 2 (account chooser)...")
                retryWithAccountChooser(activityContext, credentialManager, webClientId)
            } catch (e: GetCredentialException) {
                val isNoCred = e is NoCredentialException ||
                        e.type.contains("no_credential", ignoreCase = true) ||
                        e.message?.contains("no credentials", ignoreCase = true) == true
                if (isNoCred) {
                    Log.d(tag, "Stage 1 indicated no authorized credential [type=${e.type}]. Proceeding to Stage 2 (account chooser)...")
                    retryWithAccountChooser(activityContext, credentialManager, webClientId)
                } else {
                    Log.e(tag, "Stage 1 provider failure: type=${e.type}, class=${e.javaClass.name}, message=${e.message}", e)
                    return@withContext Result.failure(
                        GoogleAuthException.ProviderError(
                            "Google Sign-In failed in Stage 1: [${e.javaClass.simpleName}] type=${e.type} - ${e.message ?: "no message"}",
                            e
                        )
                    )
                }
            }

            if (credential == null) {
                return@withContext Result.failure(GoogleAuthException.Cancelled)
            }

            // 3. Extract Google ID Token and authenticate with Firebase
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Google sign in succeeded but user profile is null")
                _currentUser.value = user
                Result.success(user)
            } else {
                Log.e(tag, "Unsupported credential type: ${credential.type}")
                Result.failure(GoogleAuthException.ProviderError("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GoogleAuthException.Cancelled) {
            Result.failure(e)
        } catch (e: GoogleAuthException) {
            Result.failure(e)
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "Google Sign-In cancelled by user")
            Result.failure(GoogleAuthException.Cancelled)
        } catch (e: FirebaseAuthException) {
            Log.e(tag, "Firebase authentication failed: ${e.errorCode} - ${e.message}", e)
            Result.failure(GoogleAuthException.FirebaseAuthFailure("Firebase auth failed: [${e.errorCode}] ${e.localizedMessage ?: e.message ?: "Unknown error"}", e))
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In error: ${e.javaClass.name}: ${e.message}", e)
            Result.failure(GoogleAuthException.ProviderError("Google Sign-In error: [${e.javaClass.simpleName}] ${e.message ?: "no message"}", e))
        }
    }

    private suspend fun retryWithAccountChooser(
        activityContext: Context,
        credentialManager: CredentialManager,
        webClientId: String
    ): Credential? {
        val tag = "AuthRepository"
        Log.d(tag, "Google Sign-In Stage 2: Requesting account chooser with filterByAuthorizedAccounts=false")
        val chooserOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val chooserRequest = GetCredentialRequest.Builder()
            .addCredentialOption(chooserOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activityContext, chooserRequest)
            Log.d(tag, "Stage 2 succeeded; credential obtained from account chooser.")
            result.credential
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "Stage 2: User cancelled Google account chooser")
            null
        } catch (e: NoCredentialException) {
            Log.w(tag, "Stage 2 NoCredentialException: class=${e.javaClass.name}, message=${e.message}", e)
            val msg = e.message.orEmpty()
            throw GoogleAuthException.EnvironmentLimitation(
                "Google Sign-In failed (Stage 2): [${e.javaClass.simpleName}] ${if (msg.isNotBlank()) msg else "No accounts/credentials available"}",
                e
            )
        } catch (e: GetCredentialException) {
            Log.e(tag, "Stage 2 GetCredentialException: type=${e.type}, class=${e.javaClass.name}, message=${e.message}", e)
            val type = e.type.lowercase()
            val msg = e.message.orEmpty()
            when {
                type.contains("cancel") || type.contains("interrupted") || msg.lowercase().contains("cancel") -> {
                    Log.d(tag, "Stage 2: Cancellation detected from exception type: ${e.type}")
                    null
                }
                else -> {
                    throw GoogleAuthException.ProviderError(
                        "Google Sign-In failed (Stage 2): [${e.javaClass.simpleName}] type=${e.type} - ${if (msg.isNotBlank()) msg else "no message"}",
                        e
                    )
                }
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
