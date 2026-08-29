package com.example.data.util

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import com.example.data.model.Surah
import com.example.data.model.Verse

@Immutable
data class QuranIntegrityIssue(
    val verseKey: String,
    val surahNumber: Int,
    val verseNumber: Int,
    val issueType: String,
    val details: String
)

@Immutable
data class QuranIntegrityReport(
    val isValid: Boolean,
    val totalSurahsChecked: Int,
    val totalVersesChecked: Int,
    val issues: List<QuranIntegrityIssue>,
    val summary: String
)

object QuranData {

    const val BISMILLAH_ARABIC = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    const val BISMILLAH_ENGLISH = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
    const val BISMILLAH_AUDIO_URL = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"

    val SURAHS_DIRECTORY: List<Surah> = listOf(
        Surah(1, "Al-Fatihah", "الفاتحة", "The Opening", 7, "Meccan"),
        Surah(2, "Al-Baqarah", "البقرة", "The Cow", 286, "Medinan"),
        Surah(3, "Ali 'Imran", "آل عمران", "Family of Imran", 200, "Medinan"),
        Surah(4, "An-Nisa", "النساء", "The Women", 176, "Medinan"),
        Surah(5, "Al-Ma'idah", "المائدة", "The Table Spread", 120, "Medinan"),
        Surah(6, "Al-An'am", "الأنعام", "The Cattle", 165, "Meccan"),
        Surah(7, "Al-A'raf", "الأعراف", "The Heights", 206, "Meccan"),
        Surah(8, "Al-Anfal", "الأنفال", "The Spoils of War", 75, "Medinan"),
        Surah(9, "At-Tawbah", "التوبة", "The Repentance", 129, "Medinan"),
        Surah(10, "Yunus", "يونس", "Jonah", 109, "Meccan"),
        Surah(11, "Hud", "هود", "Hud", 123, "Meccan"),
        Surah(12, "Yusuf", "يوسف", "Joseph", 111, "Meccan"),
        Surah(13, "Ar-Ra'd", "الرعد", "The Thunder", 43, "Medinan"),
        Surah(14, "Ibrahim", "إبراهيم", "Abraham", 52, "Meccan"),
        Surah(15, "Al-Hijr", "الحجر", "The Rocky Tract", 99, "Meccan"),
        Surah(16, "An-Nahl", "النحل", "The Bee", 128, "Meccan"),
        Surah(17, "Al-Isra", "الإسراء", "The Night Journey", 111, "Meccan"),
        Surah(18, "Al-Kahf", "الكهف", "The Cave", 110, "Meccan"),
        Surah(19, "Maryam", "مريم", "Mary", 98, "Meccan"),
        Surah(20, "Taha", "طه", "Ta-Ha", 135, "Meccan"),
        Surah(21, "Al-Anbiya", "الأنبياء", "The Prophets", 112, "Meccan"),
        Surah(22, "Al-Hajj", "الحج", "The Pilgrimage", 78, "Medinan"),
        Surah(23, "Al-Mu'minun", "المؤمنون", "The Believers", 118, "Meccan"),
        Surah(24, "An-Nur", "النور", "The Light", 64, "Medinan"),
        Surah(25, "Al-Furqan", "الفرقان", "The Criterion", 77, "Meccan"),
        Surah(26, "Ash-Shu'ara", "الشعراء", "The Poets", 227, "Meccan"),
        Surah(27, "An-Naml", "النمل", "The Ant", 93, "Meccan"),
        Surah(28, "Al-Qasas", "القصص", "The Stories", 88, "Meccan"),
        Surah(29, "Al-'Ankabut", "العنكبوت", "The Spider", 69, "Meccan"),
        Surah(30, "Ar-Rum", "الروم", "The Romans", 60, "Meccan"),
        Surah(31, "Luqman", "لقمان", "Luqman", 34, "Meccan"),
        Surah(32, "As-Sajdah", "السجدة", "The Prostration", 30, "Meccan"),
        Surah(33, "Al-Ahzab", "الأحزاب", "The Combined Forces", 73, "Medinan"),
        Surah(34, "Saba", "سبأ", "Sheba", 54, "Meccan"),
        Surah(35, "Fatir", "فاطر", "Originator", 45, "Meccan"),
        Surah(36, "Ya-Sin", "يس", "Ya-Sin", 83, "Meccan"),
        Surah(37, "As-Saffat", "الصافات", "Those Who Set the Ranks", 182, "Meccan"),
        Surah(38, "Sad", "ص", "The Letter Sad", 88, "Meccan"),
        Surah(39, "Az-Zumar", "الزمر", "The Troops", 75, "Meccan"),
        Surah(40, "Ghafir", "غافر", "The Forgiver", 85, "Meccan"),
        Surah(41, "Fussilat", "فصلت", "Explained in Detail", 54, "Meccan"),
        Surah(42, "Ash-Shura", "الشورى", "The Consultation", 53, "Meccan"),
        Surah(43, "Az-Zukhruf", "الزخرف", "The Ornaments of Gold", 89, "Meccan"),
        Surah(44, "Ad-Dukhan", "الدخان", "The Smoke", 59, "Meccan"),
        Surah(45, "Al-Jathiyah", "الجاثية", "The Crouching", 37, "Meccan"),
        Surah(46, "Al-Ahqaf", "الأحقاف", "The Wind-Curved Sandhills", 35, "Meccan"),
        Surah(47, "Muhammad", "محمد", "Muhammad", 38, "Medinan"),
        Surah(48, "Al-Fath", "الفتح", "The Victory", 29, "Medinan"),
        Surah(49, "Al-Hujurat", "الحجرات", "The Dwellings", 18, "Medinan"),
        Surah(50, "Qaf", "ق", "The Letter Qaf", 45, "Meccan"),
        Surah(51, "Adh-Dhariyat", "الذاريات", "The Winnowing Winds", 60, "Meccan"),
        Surah(52, "At-Tur", "الطور", "The Mount", 49, "Meccan"),
        Surah(53, "An-Najm", "النجم", "The Star", 62, "Meccan"),
        Surah(54, "Al-Qamar", "القمر", "The Moon", 55, "Meccan"),
        Surah(55, "Ar-Rahman", "الرحمن", "The Beneficent", 78, "Medinan"),
        Surah(56, "Al-Waqi'ah", "الواقعة", "The Inevitable", 96, "Meccan"),
        Surah(57, "Al-Hadid", "الحديد", "The Iron", 29, "Medinan"),
        Surah(58, "Al-Mujadila", "المجادلة", "The Pleading Woman", 22, "Medinan"),
        Surah(59, "Al-Hashr", "الحشر", "The Exile", 24, "Medinan"),
        Surah(60, "Al-Mumtahanah", "الممتحنة", "She That Is To Be Examined", 13, "Medinan"),
        Surah(61, "As-Saff", "الصف", "The Ranks", 14, "Medinan"),
        Surah(62, "Al-Jumu'ah", "الجمعة", "Friday", 11, "Medinan"),
        Surah(63, "Al-Munafiqun", "المنافقون", "The Hypocrites", 11, "Medinan"),
        Surah(64, "At-Taghabun", "التغابن", "The Mutual Disillusion", 18, "Medinan"),
        Surah(65, "At-Talaq", "الطلاق", "The Divorce", 12, "Medinan"),
        Surah(66, "At-Tahrim", "التحريم", "The Prohibition", 12, "Medinan"),
        Surah(67, "Al-Mulk", "الملك", "The Sovereignty", 30, "Meccan"),
        Surah(68, "Al-Qalam", "القلم", "The Pen", 52, "Meccan"),
        Surah(69, "Al-Haqqah", "الحاقة", "The Reality", 52, "Meccan"),
        Surah(70, "Al-Ma'arij", "المعارج", "The Ascending Stairways", 44, "Meccan"),
        Surah(71, "Nuh", "نوح", "Noah", 28, "Meccan"),
        Surah(72, "Al-Jinn", "الجن", "The Jinn", 28, "Meccan"),
        Surah(73, "Al-Muzzammil", "المزمل", "The Enshrouded One", 20, "Meccan"),
        Surah(74, "Al-Muddaththir", "المدثر", "The Cloaked One", 56, "Meccan"),
        Surah(75, "Al-Qiyamah", "القيامة", "The Resurrection", 40, "Meccan"),
        Surah(76, "Al-Insan", "الإنسان", "Man", 31, "Medinan"),
        Surah(77, "Al-Mursalat", "المرسلات", "The Emissaries", 50, "Meccan"),
        Surah(78, "An-Naba", "النبأ", "The Tidings", 40, "Meccan"),
        Surah(79, "An-Nazi'at", "النازعات", "Those Who Drag Forth", 46, "Meccan"),
        Surah(80, "'Abasa", "عبس", "He Frowned", 42, "Meccan"),
        Surah(81, "At-Takwir", "التكوير", "The Overthrowing", 29, "Meccan"),
        Surah(82, "Al-Infitar", "الإنفطار", "The Cleaving", 19, "Meccan"),
        Surah(83, "Al-Mutaffifin", "المطففين", "The Defrauding", 36, "Meccan"),
        Surah(84, "Al-Inshiqaq", "الإنشقاق", "The Splitting Open", 25, "Meccan"),
        Surah(85, "Al-Buruj", "البروج", "The Mansions of the Stars", 22, "Meccan"),
        Surah(86, "At-Tariq", "الطارق", "The Morning Star", 17, "Meccan"),
        Surah(87, "Al-A'la", "الأعلى", "The Most High", 19, "Meccan"),
        Surah(88, "Al-Ghashiyah", "الغاشية", "The Overwhelming", 26, "Meccan"),
        Surah(89, "Al-Fajr", "الفجر", "The Dawn", 30, "Meccan"),
        Surah(90, "Al-Balad", "البلد", "The City", 20, "Meccan"),
        Surah(91, "Ash-Shams", "الشمس", "The Sun", 15, "Meccan"),
        Surah(92, "Al-Layl", "الليل", "The Night", 21, "Meccan"),
        Surah(93, "Ad-Duha", "الضحى", "The Morning Hours", 11, "Meccan"),
        Surah(94, "Ash-Sharh", "الشرح", "The Relief", 8, "Meccan"),
        Surah(95, "At-Tin", "التين", "The Fig", 8, "Meccan"),
        Surah(96, "Al-'Alaq", "العلق", "The Clot", 19, "Meccan"),
        Surah(97, "Al-Qadr", "القدر", "The Power", 5, "Meccan"),
        Surah(98, "Al-Bayyinah", "البينة", "The Clear Proof", 8, "Medinan"),
        Surah(99, "Az-Zalzalah", "الزلزلة", "The Earthquake", 8, "Medinan"),
        Surah(100, "Al-'Adiyat", "العاديات", "The Courser", 11, "Meccan"),
        Surah(101, "Al-Qari'ah", "القارعة", "The Calamity", 11, "Meccan"),
        Surah(102, "At-Takathur", "التكاثر", "Rivalry in Worldly Increase", 8, "Meccan"),
        Surah(103, "Al-'Asr", "العصر", "The Declining Day", 3, "Meccan"),
        Surah(104, "Al-Humazah", "الهمزة", "The Traducer", 9, "Meccan"),
        Surah(105, "Al-Fil", "الفيل", "The Elephant", 5, "Meccan"),
        Surah(106, "Quraysh", "قريش", "Quraysh", 4, "Meccan"),
        Surah(107, "Al-Ma'un", "المواعون", "Small Kindnesses", 7, "Meccan"),
        Surah(108, "Al-Kawthar", "الكوثر", "Abundance", 3, "Meccan"),
        Surah(109, "Al-Kafirun", "الكافرون", "The Disbelievers", 6, "Meccan"),
        Surah(110, "An-Nasr", "النصر", "The Divine Support", 3, "Medinan"),
        Surah(111, "Al-Masad", "المسد", "The Palm Fiber", 5, "Meccan"),
        Surah(112, "Al-Ikhlas", "الإخلاص", "Sincerity", 4, "Meccan"),
        Surah(113, "Al-Falaq", "الفلق", "The Daybreak", 5, "Meccan"),
        Surah(114, "An-Nas", "الناس", "Mankind", 6, "Meccan")
    )

    @Volatile
    private var cachedVersesMap: Map<Int, List<Verse>>? = null

    @Volatile
    private var cachedVerseIdentityMap: Map<String, Verse>? = null

    @Volatile
    private var cachedTranslationIdentityMap: Map<String, String>? = null

    fun getSurahById(surahNumber: Int): Surah? {
        return SURAHS_DIRECTORY.find { it.number == surahNumber }
    }

    @Synchronized
    fun ensureDataLoaded(context: Context) {
        if (cachedVersesMap == null || cachedVerseIdentityMap == null || cachedTranslationIdentityMap == null) {
            val (surahMap, identityMap, transMap) = loadAllVersesFromAssets(context)
            cachedVersesMap = surahMap
            cachedVerseIdentityMap = identityMap
            cachedTranslationIdentityMap = transMap
        }
    }

    fun preload(context: Context) {
        ensureDataLoaded(context)
    }

    fun getVersesForSurah(context: Context, surahNumber: Int): List<Verse> {
        ensureDataLoaded(context)
        return cachedVersesMap?.get(surahNumber) ?: emptyList()
    }

    /**
     * Authoritative identity-based lookup for a Verse by "surahNumber:ayahNumber".
     * Returns null if key is not found (never silently falls back to Bismillah).
     */
    fun getVerseByKey(context: Context, verseKey: String): Verse? {
        ensureDataLoaded(context)
        return cachedVerseIdentityMap?.get(verseKey)
    }

    fun getVerse(context: Context, surahNumber: Int, verseNumber: Int): Verse? {
        return getVerseByKey(context, "$surahNumber:$verseNumber")
    }

    /**
     * Authoritative identity-based translation lookup.
     * KEY = "surahNumber:ayahNumber", VALUE = Saheeh International translation text.
     * Returns null if not found (never silently falls back to Bismillah).
     */
    fun getTranslationByKey(context: Context, verseKey: String): String? {
        ensureDataLoaded(context)
        return cachedTranslationIdentityMap?.get(verseKey)
    }

    fun getTranslation(context: Context, surahNumber: Int, verseNumber: Int): String? {
        return getTranslationByKey(context, "$surahNumber:$verseNumber")
    }

    /**
     * Standalone Surah-level Bismillah metadata.
     * Returns null for Surah At-Tawbah (9).
     * For Surahs 2..114, returns the Bismillah header entity with verseNumber = 0 and verseKey = "$surahNumber:0".
     */
    fun getSurahBismillah(surahNumber: Int): Verse? {
        if (surahNumber == 9) return null
        return Verse(
            surahNumber = surahNumber,
            verseNumber = 0,
            textArabic = BISMILLAH_ARABIC,
            textEnglish = BISMILLAH_ENGLISH,
            audioUrl = BISMILLAH_AUDIO_URL,
            verseKey = "$surahNumber:0"
        )
    }

    private data class ParsedQuranData(
        val surahMap: Map<Int, List<Verse>>,
        val identityVerseMap: Map<String, Verse>,
        val identityTranslationMap: Map<String, String>
    )

    private fun loadAllVersesFromAssets(context: Context): ParsedQuranData {
        val surahMap = HashMap<Int, List<Verse>>(114)
        val identityVerseMap = HashMap<String, Verse>(6250)
        val identityTranslationMap = HashMap<String, String>(6250)

        try {
            context.assets.open("quran_complete.json").use { inputStream ->
                android.util.JsonReader(java.io.InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val surahKey = reader.nextName()
                        val surahNum = surahKey.toIntOrNull() ?: 0
                        reader.beginArray()
                        val versesList = ArrayList<Verse>()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            var sNum = surahNum
                            var vNum = 0
                            var textAr = ""
                            var textEn = ""
                            var audio = ""
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "surahNumber" -> sNum = reader.nextInt()
                                    "verseNumber" -> vNum = reader.nextInt()
                                    "textArabic" -> textAr = reader.nextString()
                                    "textEnglish" -> textEn = reader.nextString()
                                    "audioUrl" -> audio = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                            
                            val verseKey = "$sNum:$vNum"
                            val verse = Verse(
                                surahNumber = sNum,
                                verseNumber = vNum,
                                textArabic = textAr,
                                textEnglish = textEn,
                                audioUrl = audio,
                                verseKey = verseKey
                            )
                            versesList.add(verse)
                            identityVerseMap[verseKey] = verse
                            identityTranslationMap[verseKey] = textEn
                        }
                        reader.endArray()
                        if (surahNum > 0) {
                            surahMap[surahNum] = versesList
                        }
                    }
                    reader.endObject()
                }
            }
        } catch (e: Exception) {
            Log.e("QuranData", "Error parsing quran_complete.json: ${e.message}", e)
        }

        return ParsedQuranData(surahMap, identityVerseMap, identityTranslationMap)
    }

    /**
     * Diagnostic utility to validate the entire 114 Surahs / 6,236 Ayahs.
     * Verifies strict ID identity, non-empty translations, and absence of Bismillah leakage.
     * Does NOT falsely flag legitimate duplicate translations across different Ayahs.
     */
    fun validateIntegrity(context: Context): QuranIntegrityReport {
        ensureDataLoaded(context)
        val issues = mutableListOf<QuranIntegrityIssue>()
        var totalSurahs = 0
        var totalVerses = 0

        for (surahMeta in SURAHS_DIRECTORY) {
            val surahNum = surahMeta.number
            val verses = getVersesForSurah(context, surahNum)
            totalSurahs++

            if (verses.isEmpty()) {
                issues.add(
                    QuranIntegrityIssue(
                        verseKey = "$surahNum:0",
                        surahNumber = surahNum,
                        verseNumber = 0,
                        issueType = "MISSING_SURAH_DATA",
                        details = "Surah ${surahMeta.nameEnglish} ($surahNum) has 0 loaded verses. Expected ${surahMeta.versesCount}."
                    )
                )
                continue
            }

            if (verses.size != surahMeta.versesCount) {
                issues.add(
                    QuranIntegrityIssue(
                        verseKey = "$surahNum:0",
                        surahNumber = surahNum,
                        verseNumber = 0,
                        issueType = "SURAH_COUNT_MISMATCH",
                        details = "Surah ${surahMeta.nameEnglish} ($surahNum) loaded ${verses.size} verses, expected ${surahMeta.versesCount}."
                    )
                )
            }

            for ((idx, verse) in verses.withIndex()) {
                totalVerses++
                val expectedVerseNum = idx + 1
                val expectedKey = "$surahNum:$expectedVerseNum"

                // 1. Identity match check
                if (verse.surahNumber != surahNum) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = verse.verseKey,
                            surahNumber = surahNum,
                            verseNumber = verse.verseNumber,
                            issueType = "SURAH_NUMBER_MISMATCH",
                            details = "Verse has surahNumber ${verse.surahNumber} instead of $surahNum."
                        )
                    )
                }

                if (verse.verseNumber != expectedVerseNum) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = verse.verseKey,
                            surahNumber = surahNum,
                            verseNumber = verse.verseNumber,
                            issueType = "VERSE_NUMBER_MISMATCH",
                            details = "Verse at index $idx has verseNumber ${verse.verseNumber} instead of $expectedVerseNum."
                        )
                    )
                }

                // 2. Authoritative identity mapping resolution
                val mappedVerse = getVerseByKey(context, expectedKey)
                val mappedTranslation = getTranslationByKey(context, expectedKey)

                if (mappedVerse == null) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = expectedKey,
                            surahNumber = surahNum,
                            verseNumber = expectedVerseNum,
                            issueType = "MISSING_IDENTITY_MAPPING",
                            details = "getVerseByKey('$expectedKey') returned null."
                        )
                    )
                }

                if (mappedTranslation.isNullOrBlank()) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = expectedKey,
                            surahNumber = surahNum,
                            verseNumber = expectedVerseNum,
                            issueType = "MISSING_TRANSLATION",
                            details = "getTranslationByKey('$expectedKey') is null or empty."
                        )
                    )
                } else if (mappedVerse != null && mappedTranslation != mappedVerse.textEnglish) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = expectedKey,
                            surahNumber = surahNum,
                            verseNumber = expectedVerseNum,
                            issueType = "TRANSLATION_MAPPING_DESYNC",
                            details = "mappedTranslation != mappedVerse.textEnglish for $expectedKey."
                        )
                    )
                }

                // 3. Content completeness
                if (verse.textArabic.isBlank()) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = expectedKey,
                            surahNumber = surahNum,
                            verseNumber = expectedVerseNum,
                            issueType = "EMPTY_ARABIC_TEXT",
                            details = "Arabic text is blank for $expectedKey."
                        )
                    )
                }

                if (verse.textEnglish.isBlank()) {
                    issues.add(
                        QuranIntegrityIssue(
                            verseKey = expectedKey,
                            surahNumber = surahNum,
                            verseNumber = expectedVerseNum,
                            issueType = "EMPTY_ENGLISH_TEXT",
                            details = "English text is blank for $expectedKey."
                        )
                    )
                }

                // 4. Bismillah Leakage Detection
                // Note: Surah 1:1 is Bismillah. Surah 27:30 contains Bismillah in letter from Solomon.
                // All other Ayahs (including 62:1, 109:1, 114:1) MUST NOT have the Bismillah translation.
                if (surahNum != 1 && !(surahNum == 27 && expectedVerseNum == 30)) {
                    if (verse.textEnglish.startsWith("In the name of Allah, the Entirely Merciful", ignoreCase = true)) {
                        issues.add(
                            QuranIntegrityIssue(
                                verseKey = expectedKey,
                                surahNumber = surahNum,
                                verseNumber = expectedVerseNum,
                                issueType = "BISMILLAH_LEAKAGE",
                                details = "Ayah $expectedKey improperly contains Bismillah translation: '${verse.textEnglish}'"
                            )
                        )
                    }
                }
            }
        }

        val isValid = issues.isEmpty()
        val summary = if (isValid) {
            "Qur'an Data Integrity Verified: 114 Surahs, $totalVerses Verses checked with 0 issues."
        } else {
            "Qur'an Data Integrity FAILED with ${issues.size} issues across $totalSurahs Surahs."
        }

        Log.i("QuranDataIntegrity", summary)
        if (!isValid) {
            for (issue in issues.take(10)) {
                Log.e("QuranDataIntegrity", "Issue: [${issue.issueType}] ${issue.verseKey}: ${issue.details}")
            }
        }

        return QuranIntegrityReport(
            isValid = isValid,
            totalSurahsChecked = totalSurahs,
            totalVersesChecked = totalVerses,
            issues = issues,
            summary = summary
        )
    }

    private val ARABIC_DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670\u0671]")

    fun normalizeArabic(text: String): String {
        return text.replace(ARABIC_DIACRITICS_REGEX, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
    }

    fun getQuranLensInfoForVerse(context: Context, surahNumber: Int, verseNumber: Int): com.example.data.model.QuranLensInfo {
        val verse = if (surahNumber == 1) {
            if (verseNumber == 0) {
                getSurahBismillah(1)
            } else if (verseNumber in 1..6) {
                val raw = getVerse(context, 1, verseNumber + 1)
                raw?.copy(verseNumber = verseNumber, verseKey = "1:$verseNumber")
            } else {
                getVerse(context, surahNumber, verseNumber)
            }
        } else if (verseNumber == 0) {
            getSurahBismillah(surahNumber)
        } else {
            getVerse(context, surahNumber, verseNumber)
        }
        val surah = SURAHS_DIRECTORY.find { it.number == surahNumber }

        if (verse == null) {
            return com.example.data.model.QuranLensInfo(
                arabicWordOrPhrase = "رَحْمَة",
                transliteration = "Rahmah",
                meaning = "Divine Mercy & Compassion",
                occurrencesCount = 0,
                occurrences = emptyList()
            )
        }

        val words = verse.textArabic.split(" ")
            .map { it.trim() }
            .filter { it.length >= 3 }

        val mainWord = words.getOrNull(0) ?: verse.textArabic
        val cleanKeyword = normalizeArabic(mainWord)

        ensureDataLoaded(context)
        val allMap = cachedVersesMap ?: emptyMap()
        val occurrences = mutableListOf<com.example.data.model.VerseOccurrence>()

        for ((sNum, vList) in allMap) {
            val sMeta = SURAHS_DIRECTORY.find { it.number == sNum } ?: continue
            for (v in vList) {
                if (normalizeArabic(v.textArabic).contains(cleanKeyword)) {
                    occurrences.add(
                        com.example.data.model.VerseOccurrence(
                            surahNumber = sNum,
                            surahNameEnglish = sMeta.nameEnglish,
                            surahNameArabic = sMeta.nameArabic,
                            verseNumber = v.verseNumber,
                            textArabic = v.textArabic,
                            textEnglish = v.textEnglish
                        )
                    )
                    if (occurrences.size >= 12) break
                }
            }
            if (occurrences.size >= 12) break
        }

        return com.example.data.model.QuranLensInfo(
            arabicWordOrPhrase = mainWord,
            transliteration = "${surah?.nameEnglish ?: "Surah"} $surahNumber:$verseNumber",
            meaning = verse.textEnglish,
            occurrencesCount = occurrences.size,
            occurrences = occurrences
        )
    }
}
