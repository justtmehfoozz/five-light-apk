package com.example.data.util

import com.example.data.model.DhikrPreset
import com.example.data.model.NameOfAllah
import com.example.data.model.NamesOfAllahData
import com.example.data.model.Surah
import com.example.ui.screens.AdhkarItem

object GlobalSearchEngine {

    /**
     * Strips Arabic diacritics (harakat/tashkeel) and unifies alef/ya/ta-marbuta variations
     * for seamless Arabic search queries.
     */
    fun normalizeArabic(input: String): String {
        return input
            // Remove Arabic diacritics / harakat (range U+064B to U+065F, U+0670, U+06D6-U+06ED)
            .replace(Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]"), "")
            // Normalize Alef variants (أ, إ, آ, ٱ) -> ا
            .replace(Regex("[أإآٱ]"), "ا")
            // Normalize Yeh variants (ى) -> ي
            .replace('ى', 'ي')
            // Normalize Ta Marbuta (ة) -> ه
            .replace('ة', 'ه')
            .trim()
    }

    /**
     * Search Surahs with canonical indexing across all 114 Surahs.
     */
    fun searchSurahs(query: String): List<Surah> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return QuranData.SURAHS_DIRECTORY.filter { surah ->
            surah.number.toString() == cleanQuery ||
            surah.nameEnglish.lowercase().contains(cleanQuery) ||
            normalizeArabic(surah.nameArabic).contains(normalizedArabicQuery) ||
            surah.englishTranslation.lowercase().contains(cleanQuery) ||
            surah.revelationPlace.lowercase().contains(cleanQuery)
        }
    }

    /**
     * Search 99 Names of Allah.
     */
    fun searchNamesOfAllah(query: String): List<NameOfAllah> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return NamesOfAllahData.NAMES.filter { name ->
            name.number.toString() == cleanQuery ||
            name.nameTransliteration.lowercase().contains(cleanQuery) ||
            name.englishMeaning.lowercase().contains(cleanQuery) ||
            normalizeArabic(name.nameArabic).contains(normalizedArabicQuery)
        }
    }

    /**
     * Search DailyDuas from DailyContentProvider.
     */
    fun searchDailyDuas(query: String): List<DailyDua> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return DailyContentProvider.authenticDuas.filter { dua ->
            dua.transliteration.lowercase().contains(cleanQuery) ||
            dua.translation.lowercase().contains(cleanQuery) ||
            dua.reference.lowercase().contains(cleanQuery) ||
            normalizeArabic(dua.arabic).contains(normalizedArabicQuery)
        }
    }

    /**
     * Search Library Duas from DuaData.ALL_DUAS.
     */
    fun searchLibraryDuas(query: String): List<DuaItem> {
        return searchDuas(query)
    }

    /**
     * Search normalized canonical Duas.
     * Returns unique canonical DuaItem objects without duplicates.
     */
    fun searchDuas(query: String): List<DuaItem> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return DuaData.ALL_DUAS.filter { dua ->
            dua.title.lowercase().contains(cleanQuery) ||
            dua.category.lowercase().contains(cleanQuery) ||
            dua.categories.any { it.lowercase().contains(cleanQuery) } ||
            dua.transliteration.lowercase().contains(cleanQuery) ||
            dua.translation.lowercase().contains(cleanQuery) ||
            (dua.reference != null && dua.reference.lowercase().contains(cleanQuery)) ||
            (dua.benefitOrNotes != null && dua.benefitOrNotes.lowercase().contains(cleanQuery)) ||
            normalizeArabic(dua.arabic).contains(normalizedArabicQuery)
        }
    }

    /**
     * Search Dhikr Presets.
     */
    fun searchDhikrPresets(query: String, allDhikrs: List<DhikrPreset>): List<DhikrPreset> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return allDhikrs.filter { dhikr ->
            dhikr.nameEnglish.lowercase().contains(cleanQuery) ||
            dhikr.translation.lowercase().contains(cleanQuery) ||
            normalizeArabic(dhikr.nameArabic).contains(normalizedArabicQuery)
        }
    }

    /**
     * Search Daily Adhkar items.
     */
    fun searchDailyAdhkar(query: String, adhkarList: List<AdhkarItem>): List<AdhkarItem> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        val normalizedArabicQuery = normalizeArabic(cleanQuery)

        return adhkarList.filter { dhikr ->
            dhikr.title.lowercase().contains(cleanQuery) ||
            dhikr.translation.lowercase().contains(cleanQuery) ||
            normalizeArabic(dhikr.arabic).contains(normalizedArabicQuery)
        }
    }

    fun searchAdhkar(query: String, adhkarList: List<AdhkarItem>): List<AdhkarItem> {
        return searchDailyAdhkar(query, adhkarList)
    }
}
