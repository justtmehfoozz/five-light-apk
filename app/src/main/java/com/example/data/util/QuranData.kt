package com.example.data.util

import com.example.data.model.Surah
import com.example.data.model.Verse

object QuranData {

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

    private val FEATURED_VERSES = mapOf(
        1 to listOf(
            Verse(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"),
            Verse(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is due to Allah, Lord of the worlds -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2.mp3"),
            Verse(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3.mp3"),
            Verse(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/4.mp3"),
            Verse(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5.mp3"),
            Verse(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6.mp3"),
            Verse(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/7.mp3")
        ),
        112 to listOf(
            Verse(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, 'He is Allah, [who is] One,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6222.mp3"),
            Verse(112, 2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6223.mp3"),
            Verse(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6224.mp3"),
            Verse(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.'", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6225.mp3")
        ),
        113 to listOf(
            Verse(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, 'I seek refuge in the Lord of daybreak", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6226.mp3"),
            Verse(113, 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6227.mp3"),
            Verse(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6228.mp3"),
            Verse(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6229.mp3"),
            Verse(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.'", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6230.mp3")
        ),
        114 to listOf(
            Verse(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, 'I seek refuge in the Lord of mankind,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6231.mp3"),
            Verse(114, 2, "مَلِكِ النَّاسِ", "The Sovereign of mankind.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6232.mp3"),
            Verse(114, 3, "إِلَٰهِ النَّاسِ", "The God of mankind,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6233.mp3"),
            Verse(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6234.mp3"),
            Verse(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6235.mp3"),
            Verse(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.'", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/6236.mp3")
        ),
        67 to listOf(
            Verse(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5242.mp3"),
            Verse(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "[He] who created death and life to test you as to which of you is best in deed - and He is the Exalted in Might, the Forgiving -", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5243.mp3"),
            Verse(67, 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/5244.mp3")
        ),
        36 to listOf(
            Verse(36, 1, "يس", "Ya, Seen.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3706.mp3"),
            Verse(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", "By the wise Qur'an,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3707.mp3"),
            Verse(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Indeed you, [O Muhammad], are from among the messengers,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3708.mp3"),
            Verse(36, 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "On a straight path.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3709.mp3"),
            Verse(36, 5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "[This is] a revelation of the Exalted in Might, the Merciful,", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/3710.mp3")
        ),
        18 to listOf(
            Verse(18, 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا", "[All] praise is due to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2141.mp3"),
            Verse(18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward", "https://cdn.islamic.network/quran/audio/128/ar.alafasy/2142.mp3")
        )
    )

    fun getVersesForSurah(surahNumber: Int): List<Verse> {
        val featured = FEATURED_VERSES[surahNumber]
        if (featured != null) return featured

        val surahInfo = SURAHS_DIRECTORY.find { it.number == surahNumber } ?: SURAHS_DIRECTORY[0]
        val list = mutableListOf<Verse>()
        
        // Always include Bismillah unless Surah 9
        if (surahNumber != 9 && surahNumber != 1) {
            list.add(
                Verse(
                    surahNumber = surahNumber,
                    verseNumber = 0,
                    textArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    textEnglish = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
                )
            )
        }

        val sampleVersesCount = surahInfo.versesCount.coerceAtMost(12)
        for (i in 1..sampleVersesCount) {
            list.add(
                Verse(
                    surahNumber = surahNumber,
                    verseNumber = i,
                    textArabic = "وَإِذَا قُرِئَ الْقُرْآنُ فَاسْتَمِعُوا لَهُ وَأَنصِتُوا لَعَلَّكُمْ تُرْحَمُونَ ($i)",
                    textEnglish = "So when the Qur'an is recited, then listen to it and pay attention that you may receive mercy. (Verse $i)",
                    audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3"
                )
            )
        }
        return list
    }
}
