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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            com.example.FiveLightApp.ensureFirebaseInitialized(context)
            Log.d(tag, "Attempting email registration with Firebase project: ${firebaseAuth.app.options.projectId}")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Registration succeeded but user is null")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(tag, "Email registration failure: ${e.javaClass.name} - ${e.message}", e)
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

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val tag = "AuthRepository"
        try {
            // 0. Ensure Firebase is initialized
            com.example.FiveLightApp.ensureFirebaseInitialized(activityContext)

            // 1. Resolve Server Web Client ID from compile-time generated resources
            val webClientId = try {
                activityContext.getString(com.example.R.string.default_web_client_id).trim().takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            } ?: run {
                val res = activityContext.resources
                val id = res.getIdentifier("default_web_client_id", "string", activityContext.packageName).takeIf { it != 0 }
                    ?: res.getIdentifier("default_web_client_id", "string", "com.example").takeIf { it != 0 }
                    ?: 0
                if (id != 0) {
                    try { res.getString(id).trim().takeIf { it.isNotBlank() } } catch (_: Exception) { null }
                } else null
            }
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
