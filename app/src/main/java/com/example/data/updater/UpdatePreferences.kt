package com.example.data.updater

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UpdatePreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isUpdateAvailable = MutableStateFlow(getIsUpdateAvailable())
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    fun getIsUpdateAvailable(): Boolean {
        return prefs.getBoolean(KEY_UPDATE_AVAILABLE, false)
    }

    fun getAvailableVersionCode(): Long {
        return prefs.getLong(KEY_AVAILABLE_VERSION_CODE, -1L)
    }

    fun getAvailableVersionName(): String {
        return prefs.getString(KEY_AVAILABLE_VERSION_NAME, "").orEmpty()
    }

    fun getLastNotifiedVersionCode(): Long {
        return prefs.getLong(KEY_LAST_NOTIFIED_VERSION_CODE, -1L)
    }

    fun setUpdateAvailable(isAvailable: Boolean, versionCode: Long = -1L, versionName: String = "") {
        prefs.edit()
            .putBoolean(KEY_UPDATE_AVAILABLE, isAvailable)
            .putLong(KEY_AVAILABLE_VERSION_CODE, versionCode)
            .putString(KEY_AVAILABLE_VERSION_NAME, versionName)
            .apply()
        _isUpdateAvailable.value = isAvailable
    }

    fun setLastNotifiedVersionCode(versionCode: Long) {
        prefs.edit()
            .putLong(KEY_LAST_NOTIFIED_VERSION_CODE, versionCode)
            .apply()
    }

    fun clearUpdateAvailable() {
        setUpdateAvailable(false, -1L, "")
    }

    companion object {
        private const val PREFS_NAME = "fivelight_update_prefs"
        private const val KEY_UPDATE_AVAILABLE = "key_update_available"
        private const val KEY_AVAILABLE_VERSION_CODE = "key_available_version_code"
        private const val KEY_AVAILABLE_VERSION_NAME = "key_available_version_name"
        private const val KEY_LAST_NOTIFIED_VERSION_CODE = "key_last_notified_version_code"

        @Volatile
        private var instance: UpdatePreferences? = null

        fun getInstance(context: Context): UpdatePreferences {
            return instance ?: synchronized(this) {
                instance ?: UpdatePreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
