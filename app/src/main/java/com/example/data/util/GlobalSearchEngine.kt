package com.example.data.util

import com.example.data.model.DhikrPreset
import com.example.data.model.NameOfAllah
import com.example.data.model.NamesOfAllahData
import com.example.data.model.Surah
import com.example.ui.screens.AdhkarItem

object GlobalSearchEngine {

    private val LATIN_PUNCTUATION_REGEX = Regex("[’'‘`\\-_\u2019\u2018\\.,;:!\\?\"\\(\\)\\[\\]\\{\\}]")
    private val MULTI_SPACE_REGEX = Regex("\\s+")
    private val LATIN_COMPACT_REGEX = Regex("[’'‘`\\-_\u2019\u2018\\.,;:!\\?\"\\(\\)\\[\\]\\{\\}\\s]")
    private val ARABIC_DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]")
    private val ARABIC_ALEF_REGEX = Regex("[أإآٱ]")
    private val ARABIC_PUNCTUATION_REGEX = Regex("[\\s\\.,;:!\\?\\-]")
    private val SURAH_NUM_QUERY_REGEX = Regex("^(?:surah|sura|chapter|s)[\\s\\-_]*(\\d{1,3})$", RegexOption.IGNORE_CASE)

    private class SurahSearchMeta(
        val surah: Surah,
        val normName: String,               // e.g. "alaraf"
        val strippedName: String,           // e.g. "araf"
        val tokens: List<String>,           // e.g. ["al", "araf"]
        val strippedTokens: List<String>,   // e.g. ["araf"]
        val normTranslation: String,        // e.g. "theelevatedplaces"
        val normArabic: String              // e.g. "الاعراف"
    )

    private val cachedSurahMetas: List<SurahSearchMeta> by lazy {
        QuranData.SURAHS_DIRECTORY.map { surah ->
            val normName = toCompactLatin(surah.nameEnglish)
            val strippedName = stripArticlePrefix(normName)
            val tokens = toTokenList(surah.nameEnglish)
            val strippedTokens = tokens.map { stripArticlePrefix(it) }.filter { it.isNotEmpty() }
            val normTranslation = toCompactLatin(surah.englishTranslation)
            val normArabic = normalizeArabic(surah.nameArabic)

            SurahSearchMeta(
                surah = surah,
                normName = normName,
                strippedName = strippedName,
                tokens = tokens,
                strippedTokens = strippedTokens,
                normTranslation = normTranslation,
                normArabic = normArabic
            )
        }
    }

    /**
     * Strips English punctuation, hyphens, apostrophes (including curly ones),
     * extra spaces, and converts to lowercase.
     */
    fun normalizeLatin(input: String): String {
        return input.lowercase()
            .replace(LATIN_PUNCTUATION_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
    }

    /**
     * Compact normalized string with NO spaces or punctuation.
     * E.g. "Al-A'raf" -> "alaraf"
     */
    fun toCompactLatin(input: String): String {
        return input.lowercase()
            .replace(LATIN_COMPACT_REGEX, "")
    }

    /**
     * Tokenize text into normalized words.
     */
    fun toTokenList(input: String): List<String> {
        return normalizeLatin(input).split(" ").filter { it.isNotBlank() }
    }

    /**
     * Strips common English transliteration article prefixes (e.g., "al", "an", "ar", "as", "at", "ad", "az", "ash").
     * E.g., "alaraf" -> "araf", "an-nisa" -> "nisa", "al-baqarah" -> "baqarah".
     */
    fun stripArticlePrefix(compactInput: String): String {
        val prefixes = listOf("ash", "al", "an", "ar", "as", "at", "ad", "az")
        for (prefix in prefixes) {
            if (compactInput.startsWith(prefix) && compactInput.length > prefix.length + 2) {
                return compactInput.substring(prefix.length)
            }
        }
        return compactInput
    }

    /**
     * Strips Arabic diacritics (harakat/tashkeel) and unifies alef/ya/ta-marbuta variations
     * for seamless Arabic search queries.
     */
    fun normalizeArabic(input: String): String {
        return input
            .replace(ARABIC_DIACRITICS_REGEX, "")
            .replace(ARABIC_ALEF_REGEX, "ا")
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace(ARABIC_PUNCTUATION_REGEX, "")
            .trim()
    }

    /**
     * Calculate Levenshtein Distance for fuzzy matching.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val dp = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..s2.length) {
                val temp = dp[j]
                if (s1[i - 1] == s2[j - 1]) {
                    dp[j] = prev
                } else {
                    dp[j] = 1 + minOf(prev, dp[j], dp[j - 1])
                }
                prev = temp
            }
        }
        return dp[s2.length]
    }

    /**
     * Check if query specifies a Surah number like "7", "surah 7", "surah-7", "s 7", "s7", "chapter 7".
     */
    fun parseSurahNumber(query: String): Int? {
        val trimmed = query.trim().lowercase()
        trimmed.toIntOrNull()?.let {
            if (it in 1..114) return it
        }
        val match = SURAH_NUM_QUERY_REGEX.find(trimmed)
        if (match != null) {
            val num = match.groupValues[1].toIntOrNull()
            if (num != null && num in 1..114) return num
        }
        return null
    }

    /**
     * Search Surahs with canonical indexing across all 114 Surahs.
     */
    fun searchSurahs(query: String): List<Surah> {
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val parsedSurahNum = parseSurahNumber(rawQuery)

        val normQ = toCompactLatin(rawQuery)
        val strippedQ = stripArticlePrefix(normQ)
        val normArabicQ = normalizeArabic(rawQuery)

        val scoredResults = cachedSurahMetas.mapNotNull { meta ->
            var score = 0

            // 1. Surah number match
            if (parsedSurahNum != null && meta.surah.number == parsedSurahNum) {
                score += 10000
            }

            // 2. Exact canonical match
            if (meta.surah.nameEnglish.equals(rawQuery, ignoreCase = true)) {
                score += 9000
            }

            // 3. Exact compact normalized match
            if (normQ.isNotEmpty() && normQ == meta.normName) {
                score += 8500
            }

            // 4. Exact stripped match (e.g., query "araf" vs Surah "Al-A'raf")
            if (normQ.isNotEmpty() && (normQ == meta.strippedName || strippedQ == meta.strippedName)) {
                score += 8000
            }

            // 5. Exact token match (e.g., "imran" in "Ali 'Imran")
            if (normQ.isNotEmpty() && (meta.tokens.contains(normQ) || meta.strippedTokens.contains(normQ) || meta.strippedTokens.contains(strippedQ))) {
                score += 7500
            }

            // 6. Prefix match on full or stripped name
            if (normQ.isNotEmpty() && meta.normName.startsWith(normQ)) {
                score += 7000
            } else if (normQ.isNotEmpty() && (meta.strippedName.startsWith(normQ) || meta.strippedName.startsWith(strippedQ))) {
                score += 6500
            } else if (normQ.isNotEmpty() && (meta.tokens.any { it.startsWith(normQ) } || meta.strippedTokens.any { it.startsWith(normQ) || it.startsWith(strippedQ) })) {
                score += 6000
            }

            // 7. Translation match
            if (normQ.isNotEmpty() && meta.normTranslation == normQ) {
                score += 5500
            } else if (normQ.isNotEmpty() && meta.normTranslation.startsWith(normQ)) {
                score += 5000
            } else if (normQ.isNotEmpty() && meta.normTranslation.contains(normQ)) {
                score += 3500
            }

            // 8. Arabic match
            if (normArabicQ.isNotEmpty()) {
                if (meta.normArabic == normArabicQ) {
                    score += 5000
                } else if (meta.normArabic.startsWith(normArabicQ)) {
                    score += 4500
                } else if (meta.normArabic.contains(normArabicQ)) {
                    score += 3000
                }
            }

            // 9. Substring match on name
            if (score == 0 && normQ.length >= 2) {
                if (meta.normName.contains(normQ) || meta.strippedName.contains(normQ) || meta.strippedName.contains(strippedQ)) {
                    score += 4000
                }
            }

            // 10. Fuzzy / Typo match
            if (score == 0 && normQ.length >= 3) {
                val distFull = levenshteinDistance(normQ, meta.normName)
                val targetStripped = if (strippedQ.length >= 3) strippedQ else normQ
                val distStripped = levenshteinDistance(targetStripped, meta.strippedName)
                val minDist = minOf(distFull, distStripped)

                val maxAllowedDist = when {
                    normQ.length in 3..4 -> 1
                    normQ.length in 5..7 -> 2
                    else -> 3
                }

                if (minDist <= maxAllowedDist) {
                    score += (2500 - minDist * 400)
                }
            }

            if (score > 0) {
                Pair(meta.surah, score)
            } else {
                null
            }
        }

        return scoredResults.sortedWith(
            compareByDescending<Pair<Surah, Int>> { it.second }
                .thenBy { it.first.number }
        ).map { it.first }
    }

    /**
     * Search 99 Names of Allah.
     */
    fun searchNamesOfAllah(query: String): List<NameOfAllah> {
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val normQ = toCompactLatin(rawQuery)
        val strippedQ = stripArticlePrefix(normQ)
        val normArabicQ = normalizeArabic(rawQuery)

        return NamesOfAllahData.NAMES.mapNotNull { name ->
            var score = 0

            if (name.number.toString() == rawQuery) {
                score += 10000
            }

            val normTrans = toCompactLatin(name.nameTransliteration)
            val strippedTrans = stripArticlePrefix(normTrans)
            val normMeaning = toCompactLatin(name.englishMeaning)
            val normArabic = normalizeArabic(name.nameArabic)

            if (normQ.isNotEmpty()) {
                if (normTrans == normQ || strippedTrans == normQ || strippedTrans == strippedQ) score += 8000
                else if (normTrans.startsWith(normQ) || strippedTrans.startsWith(normQ) || strippedTrans.startsWith(strippedQ)) score += 6000
                else if (normTrans.contains(normQ) || strippedTrans.contains(normQ) || strippedTrans.contains(strippedQ)) score += 4000

                if (normMeaning == normQ) score += 5000
                else if (normMeaning.contains(normQ)) score += 3000
            }

            if (normArabicQ.isNotEmpty()) {
                if (normArabic == normArabicQ) score += 5000
                else if (normArabic.startsWith(normArabicQ)) score += 4000
                else if (normArabic.contains(normArabicQ)) score += 3000
            }

            if (score == 0 && normQ.length >= 3) {
                val dist = levenshteinDistance(normQ, normTrans)
                val distStripped = levenshteinDistance(if (strippedQ.length >= 3) strippedQ else normQ, strippedTrans)
                val minDist = minOf(dist, distStripped)
                if (minDist <= (if (normQ.length >= 5) 2 else 1)) {
                    score += (2000 - minDist * 400)
                }
            }

            if (score > 0) Pair(name, score) else null
        }.sortedWith(compareByDescending<Pair<NameOfAllah, Int>> { it.second }.thenBy { it.first.number }).map { it.first }
    }

    /**
     * Search DailyDuas from DailyContentProvider.
     */
    fun searchDailyDuas(query: String): List<DailyDua> {
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val normQ = toCompactLatin(rawQuery)
        val normArabicQ = normalizeArabic(rawQuery)

        return DailyContentProvider.authenticDuas.filter { dua ->
            val normTrans = toCompactLatin(dua.transliteration)
            val normTranslation = toCompactLatin(dua.translation)
            val normRef = toCompactLatin(dua.reference)
            val normAr = normalizeArabic(dua.arabic)

            (normQ.isNotEmpty() && (normTrans.contains(normQ) || normTranslation.contains(normQ) || normRef.contains(normQ))) ||
            (normArabicQ.isNotEmpty() && normAr.contains(normArabicQ))
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
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val normQ = toCompactLatin(rawQuery)
        val normArabicQ = normalizeArabic(rawQuery)

        return DuaData.ALL_DUAS.filter { dua ->
            val normTitle = toCompactLatin(dua.title)
            val normCategory = toCompactLatin(dua.category)
            val normTrans = toCompactLatin(dua.transliteration)
            val normTranslation = toCompactLatin(dua.translation)
            val normRef = dua.reference?.let { toCompactLatin(it) } ?: ""
            val normNotes = dua.benefitOrNotes?.let { toCompactLatin(it) } ?: ""
            val normAr = normalizeArabic(dua.arabic)

            (normQ.isNotEmpty() && (
                normTitle.contains(normQ) ||
                normCategory.contains(normQ) ||
                dua.categories.any { toCompactLatin(it).contains(normQ) } ||
                normTrans.contains(normQ) ||
                normTranslation.contains(normQ) ||
                normRef.contains(normQ) ||
                normNotes.contains(normQ)
            )) ||
            (normArabicQ.isNotEmpty() && normAr.contains(normArabicQ))
        }
    }

    /**
     * Search Dhikr Presets.
     */
    fun searchDhikrPresets(query: String, allDhikrs: List<DhikrPreset>): List<DhikrPreset> {
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val normQ = toCompactLatin(rawQuery)
        val normArabicQ = normalizeArabic(rawQuery)

        return allDhikrs.filter { dhikr ->
            val normName = toCompactLatin(dhikr.nameEnglish)
            val normTrans = toCompactLatin(dhikr.translation)
            val normAr = normalizeArabic(dhikr.nameArabic)

            (normQ.isNotEmpty() && (normName.contains(normQ) || normTrans.contains(normQ))) ||
            (normArabicQ.isNotEmpty() && normAr.contains(normArabicQ))
        }
    }

    /**
     * Search Daily Adhkar items.
     */
    fun searchDailyAdhkar(query: String, adhkarList: List<AdhkarItem>): List<AdhkarItem> {
        val rawQuery = query.trim()
        if (rawQuery.isBlank()) return emptyList()

        val normQ = toCompactLatin(rawQuery)
        val normArabicQ = normalizeArabic(rawQuery)

        return adhkarList.filter { dhikr ->
            val normTitle = toCompactLatin(dhikr.title)
            val normTrans = toCompactLatin(dhikr.translation)
            val normAr = normalizeArabic(dhikr.arabic)

            (normQ.isNotEmpty() && (normTitle.contains(normQ) || normTrans.contains(normQ))) ||
            (normArabicQ.isNotEmpty() && normAr.contains(normArabicQ))
        }
    }

    fun searchAdhkar(query: String, adhkarList: List<AdhkarItem>): List<AdhkarItem> {
        return searchDailyAdhkar(query, adhkarList)
    }
}

