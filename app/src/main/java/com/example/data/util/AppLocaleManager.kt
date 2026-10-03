package com.example.data.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.model.AppLanguage
import java.util.Locale

/**
 * Authoritative single manager for app-wide locale application, context creation, and persistence helpers.
 */
object AppLocaleManager {
    private const val PREFS_NAME = "fivelight_prefs"
    private const val KEY_APP_LANGUAGE = "app_language"

    /**
     * Reads the currently persisted language safely from SharedPreferences with fallback.
     */
    fun getPersistedLanguage(context: Context?): AppLanguage {
        if (context == null) return AppLanguage.DEFAULT
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedId = prefs.getString(KEY_APP_LANGUAGE, null)
            AppLanguage.fromId(savedId)
        } catch (_: Exception) {
            AppLanguage.DEFAULT
        }
    }

    /**
     * Writes the chosen language to SharedPreferences.
     */
    fun setPersistedLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_APP_LANGUAGE, language.id).apply()
    }

    /**
     * Creates a localized Context with the specified AppLanguage applied to its Configuration.
     */
    fun createLocalizedContext(baseContext: Context, language: AppLanguage): Context {
        return try {
            val locale = language.toLocale()
            Locale.setDefault(locale)
            val config = Configuration(baseContext.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            baseContext.createConfigurationContext(config)
        } catch (_: Exception) {
            baseContext
        }
    }

    /**
     * Applies the selected language to application and base resources, updating Configuration.
     */
    fun applyAppLanguage(context: Context, language: AppLanguage) {
        try {
            val locale = language.toLocale()
            Locale.setDefault(locale)

            val res = context.resources
            val config = Configuration(res.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)

            val appContext = context.applicationContext
            if (appContext != null && appContext !== context) {
                val appRes = appContext.resources
                val appConfig = Configuration(appRes.configuration)
                appConfig.setLocale(locale)
                appConfig.setLayoutDirection(locale)
                @Suppress("DEPRECATION")
                appRes.updateConfiguration(appConfig, appRes.displayMetrics)
            }
        } catch (_: Exception) {}
    }
}

/**
 * CompositionLocal providing the current AppLanguage across the Compose hierarchy.
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.DEFAULT }
