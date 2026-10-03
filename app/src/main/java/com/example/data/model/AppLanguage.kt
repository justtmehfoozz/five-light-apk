package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/**
 * Authoritative single model representing the supported app languages in FiveLight.
 *
 * Supported Languages:
 * 1. English (en) - LTR
 * 2. Hindi-script Urdu / Hindustani (ur-Deva) - LTR
 * 3. Urdu (ur-Arab) - RTL
 * 4. Roman Urdu (ur-Latn) - LTR
 */
@Immutable
enum class AppLanguage(
    val id: String,
    val languageTag: String,
    val displayName: String,
    val nativeSubtitle: String,
    val layoutDirection: LayoutDirection
) {
    ENGLISH(
        id = "en",
        languageTag = "en",
        displayName = "English",
        nativeSubtitle = "English",
        layoutDirection = LayoutDirection.Ltr
    ),
    HINDI_URDU(
        id = "ur_Deva",
        languageTag = "ur-Deva",
        displayName = "हिंदी",
        nativeSubtitle = "Hindi-script Urdu / Hindustani",
        layoutDirection = LayoutDirection.Ltr
    ),
    URDU(
        id = "ur_Arab",
        languageTag = "ur-Arab",
        displayName = "اردو",
        nativeSubtitle = "اردو (Urdu)",
        layoutDirection = LayoutDirection.Rtl
    ),
    ROMAN_URDU(
        id = "ur_Latn",
        languageTag = "ur-Latn",
        displayName = "Roman Urdu",
        nativeSubtitle = "Urdu in Latin script",
        layoutDirection = LayoutDirection.Ltr
    );

    val isRtl: Boolean get() = layoutDirection == LayoutDirection.Rtl

    fun toLocale(): Locale {
        return when (this) {
            ENGLISH -> Locale.ENGLISH
            HINDI_URDU -> Locale.forLanguageTag("ur-Deva")
            URDU -> Locale.forLanguageTag("ur-Arab")
            ROMAN_URDU -> Locale.forLanguageTag("ur-Latn")
        }
    }

    companion object {
        val DEFAULT = ENGLISH

        fun fromId(id: String?): AppLanguage {
            if (id.isNullOrBlank()) return DEFAULT
            return entries.find { 
                it.id.equals(id, ignoreCase = true) ||
                it.name.equals(id, ignoreCase = true) ||
                it.languageTag.equals(id, ignoreCase = true)
            } ?: DEFAULT
        }

        fun fromLocale(locale: Locale?): AppLanguage {
            if (locale == null) return DEFAULT
            val tag = locale.toLanguageTag()
            return entries.find { it.languageTag.equals(tag, ignoreCase = true) }
                ?: if (locale.language == "ur") URDU else DEFAULT
        }
    }
}
