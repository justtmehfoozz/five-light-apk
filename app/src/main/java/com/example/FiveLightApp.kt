package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FiveLightApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        private const val TAG = "FiveLightApp"
        private val lock = Any()

        /**
         * Guarantees that FirebaseApp is initialized exactly once before any Firebase service
         * (FirebaseAuth, Firestore, AuthRepository, etc.) is accessed.
         */
        fun ensureFirebaseInitialized(context: Context): FirebaseApp? {
            val appContext = context.applicationContext ?: context
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                return try { FirebaseApp.getInstance() } catch (_: Exception) { null }
            }
            synchronized(lock) {
                if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                    return try { FirebaseApp.getInstance() } catch (_: Exception) { null }
                }
                return try {
                    // Attempt 1: Standard auto-initialization via Google Services plugin
                    var app = FirebaseApp.initializeApp(appContext)
                    if (app == null || FirebaseApp.getApps(appContext).isEmpty()) {
                        Log.w(TAG, "Standard Firebase initialization returned null. Initializing with explicit FirebaseOptions...")
                        
                        fun getStringRes(resId: Int, key: String): String? {
                            return try {
                                appContext.getString(resId).trim().takeIf { it.isNotBlank() }
                            } catch (_: Exception) {
                                val res = appContext.resources
                                val id = res.getIdentifier(key, "string", appContext.packageName).takeIf { it != 0 }
                                    ?: res.getIdentifier(key, "string", "com.example").takeIf { it != 0 }
                                    ?: 0
                                if (id != 0) {
                                    try { res.getString(id).trim().takeIf { it.isNotBlank() } } catch (_: Exception) { null }
                                } else null
                            }
                        }

                        val appId = getStringRes(com.example.R.string.google_app_id, "google_app_id")
                        val apiKey = getStringRes(com.example.R.string.google_api_key, "google_api_key")
                        val projectId = getStringRes(com.example.R.string.project_id, "project_id")
                        val gcmSenderId = getStringRes(com.example.R.string.gcm_defaultSenderId, "gcm_defaultSenderId")
                        val storageBucket = getStringRes(com.example.R.string.google_storage_bucket, "google_storage_bucket")

                        if (!appId.isNullOrBlank() && !apiKey.isNullOrBlank() && !projectId.isNullOrBlank()) {
                            val builder = FirebaseOptions.Builder()
                                .setApplicationId(appId)
                                .setApiKey(apiKey)
                                .setProjectId(projectId)

                            if (!gcmSenderId.isNullOrBlank()) builder.setGcmSenderId(gcmSenderId)
                            if (!storageBucket.isNullOrBlank()) builder.setStorageBucket(storageBucket)

                            app = FirebaseApp.initializeApp(appContext, builder.build())
                            Log.i(TAG, "FirebaseApp initialized successfully with resources-derived FirebaseOptions (appId=$appId, project=$projectId)")
                        } else {
                            Log.e(TAG, "Cannot initialize Firebase: missing required configuration resources (appId=$appId, hasKey=${apiKey != null}, project=$projectId)")
                        }
                    } else {
                        Log.i(TAG, "FirebaseApp initialized successfully via standard resource loader")
                    }
                    app
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize FirebaseApp", e)
                    null
                }
            }
        }
    }
}
