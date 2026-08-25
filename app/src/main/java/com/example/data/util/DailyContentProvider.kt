package com.example.data.util

import java.time.LocalDate

data class DailyDua(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String
)

data class DailyHadith(
    val id: Int,
    val arabic: String,
    val translation: String,
    val narrator: String,
    val reference: String,
    val grade: String
)

data class DailyReflection(
    val id: Int,
    val arabic: String,
    val translation: String,
    val reference: String,
    val surahNumber: Int,
    val verseNumber: Int
)

object DailyContentProvider {

    val authenticDuas = listOf(
        DailyDua(
            id = 1,
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "\"Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar\"",
            translation = "Our Lord, give us in this world that which is good and in the Hereafter that which is good and protect us from the punishment of the Fire.",
            reference = "Surah Al-Baqarah (2:201)"
        ),
        DailyDua(
            id = 2,
            arabic = "رَبَّنَا لاَ تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً إِنَّكَ أَنتَ الْوَهَّابُ",
            transliteration = "\"Rabbana la tuzigh qulubana ba'da idh hadaitana wa hab lana milladunka rahmatan innaka antal-Wahhab\"",
            translation = "Our Lord, let not our hearts deviate after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower.",
            reference = "Surah Ali 'Imran (3:8)"
        ),
        DailyDua(
            id = 3,
            arabic = "رَّبِّ زِدْنِي عِلْمًا",
            transliteration = "\"Rabbi zidni 'ilma\"",
            translation = "My Lord, increase me in knowledge.",
            reference = "Surah Taha (20:114)"
        ),
        DailyDua(
            id = 4,
            arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "\"La ilaha illa anta subhanaka inni kuntu minadh-dhalimin\"",
            translation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            reference = "Surah Al-Anbiya (21:87) • Sahih al-Tirmidhi 3505"
        ),
        DailyDua(
            id = 5,
            arabic = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            transliteration = "\"Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj'alna lil-muttaqina imama\"",
            translation = "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us an example for the righteous.",
            reference = "Surah Al-Furqan (25:74)"
        ),
        DailyDua(
            id = 6,
            arabic = "رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ",
            transliteration = "\"Rabbi inni lima anzalta ilayya min khairin faqir\"",
            translation = "My Lord, indeed I am, for whatever good You would send down to me, in need.",
            reference = "Surah Al-Qasas (28:24)"
        ),
        DailyDua(
            id = 7,
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بـِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
            transliteration = "\"Allahumma anta Rabbi la ilaha illa anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa ant\"",
            translation = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            reference = "Sahih al-Bukhari 6306 (Sayyid al-Istighfar)"
        ),
        DailyDua(
            id = 8,
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ",
            transliteration = "\"Allahumma inni as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah\"",
            translation = "O Allah, I ask You for forgiveness and well-being in this world and the Hereafter.",
            reference = "Sunan Abi Dawud 5080 • Sahih"
        ),
        DailyDua(
            id = 9,
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliteration = "\"Allahumma innaka 'afuwwun tuhibbul-'afwa fa'fu 'anni\"",
            translation = "O Allah, You are Most Forgiving, and You love forgiveness, so forgive me.",
            reference = "Jami' at-Tirmidhi 3513 • Sahih"
        ),
        DailyDua(
            id = 10,
            arabic = "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي",
            transliteration = "\"Allahumm-aslih li diniyalladhi huwa 'ismatu amri, wa aslih li dunyayallati fiha ma'ashi, wa aslih li akhiratillati fiha ma'adi\"",
            translation = "O Allah, set right for me my religion which is the safeguard of my affairs, set right for me my worldly affairs in which is my livelihood, and set right for me my Hereafter to which is my return.",
            reference = "Sahih Muslim 2725"
        ),
        DailyDua(
            id = 11,
            arabic = "رَبِّ هَبْ لِي حُكْمًا وَأَلْحِقْنِي بِالصَّالِحِينَ وَاجْعَل لِّي لِسَانَ صِدْقٍ فِي الآخِرِينَ",
            transliteration = "\"Rabbi hab li hukman wa al-hiqni bis-salihin waj'al li lisana sidqin fil-akhirin\"",
            translation = "My Lord, grant me wisdom and join me with the righteous, and grant me a reputation of honor among later generations.",
            reference = "Surah Ash-Shu'ara (26:83-84)"
        ),
        DailyDua(
            id = 12,
            arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            transliteration = "\"Ya Muqallibal-qulubi thabbit qalbi 'ala dinik\"",
            translation = "O Turner of the hearts, make my heart firm upon Your religion.",
            reference = "Jami' at-Tirmidhi 3522 • Sahih"
        )
    )

    val authenticHadiths = listOf(
        DailyHadith(
            id = 1,
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            translation = "\"Actions are judged by intentions, and every person will get what they intended.\"",
            narrator = "Narrated by 'Umar bin Al-Khattab (RA)",
            reference = "Sahih al-Bukhari 1 • Sahih Muslim 1907",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 2,
            arabic = "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لاَ يَذْكُرُ رَبَّهُ مَثَلُ الْحَيِّ وَالْمَيِّتِ",
            translation = "\"The example of the one who remembers his Lord in comparison to the one who does not remember his Lord is that of a living person compared to a dead person.\"",
            narrator = "Narrated by Abu Musa Al-Ash'ari (RA)",
            reference = "Sahih al-Bukhari 6407",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 3,
            arabic = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ",
            translation = "\"Purity is half of faith, and 'Alhamdulillah' (Praise be to Allah) fills the scale.\"",
            narrator = "Narrated by Abu Malik Al-Ash'ari (RA)",
            reference = "Sahih Muslim 223",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 4,
            arabic = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            translation = "\"None of you truly believes until he loves for his brother what he loves for himself.\"",
            narrator = "Narrated by Anas bin Malik (RA)",
            reference = "Sahih al-Bukhari 13 • Sahih Muslim 45",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 5,
            arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            translation = "\"Whoever believes in Allah and the Last Day, let him speak good or remain silent.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih al-Bukhari 6011 • Sahih Muslim 47",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 6,
            arabic = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
            translation = "\"The merciful will be shown mercy by the Most Merciful. Be merciful to those on the earth and the One in the heavens will have mercy upon you.\"",
            narrator = "Narrated by 'Abdullah bin 'Amr (RA)",
            reference = "Jami' at-Tirmidhi 1924 • Sunan Abi Dawud 4941",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 7,
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            translation = "\"The best among you are those who learn the Quran and teach it.\"",
            narrator = "Narrated by 'Uthman bin 'Affan (RA)",
            reference = "Sahih al-Bukhari 5027",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 8,
            arabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            translation = "\"Whoever treads a path in search of knowledge, Allah will make easy for him a path to Paradise.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih Muslim 2699",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 9,
            arabic = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            translation = "\"Two words are light on the tongue, heavy on the scales, and beloved to the Most Merciful: Subhan Allahi wa bihamdihi, Subhan Allahil-'Azim.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih al-Bukhari 6405 • Sahih Muslim 2694",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 10,
            arabic = "إِنَّ اللَّهَ لاَ يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
            translation = "\"Indeed, Allah does not look at your appearance or wealth, but He looks at your hearts and your deeds.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih Muslim 2564",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 11,
            arabic = "مَا أَنْزَلَ اللَّهُ دَاءً إِلاَّ أَنْزَلَ لَهُ شِفَاءً",
            translation = "\"Allah has not sent down any disease except that He has also sent down its cure.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih al-Bukhari 5678",
            grade = "Sahih"
        ),
        DailyHadith(
            id = 12,
            arabic = "لاَ يَدْخُلُ الْجَنَّةَ مَنْ لاَ يَأْمَنُ جَارُهُ بَوَائِقَهُ",
            translation = "\"He will not enter Paradise whose neighbor is not secure from his harm.\"",
            narrator = "Narrated by Abu Hurairah (RA)",
            reference = "Sahih Muslim 46",
            grade = "Sahih"
        )
    )

    val authenticReflections = listOf(
        DailyReflection(
            id = 1,
            arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            translation = "\"Indeed, in the remembrance of Allah do hearts find rest.\"",
            reference = "Qur'an 13:28",
            surahNumber = 13,
            verseNumber = 28
        ),
        DailyReflection(
            id = 2,
            arabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            translation = "\"So remember Me; I will remember you. And be grateful to Me and do not deny Me.\"",
            reference = "Qur'an 2:152",
            surahNumber = 2,
            verseNumber = 152
        ),
        DailyReflection(
            id = 3,
            arabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            translation = "\"For indeed, with hardship [will be] ease. Indeed, with hardship [will be] ease.\"",
            reference = "Qur'an 94:5-6",
            surahNumber = 94,
            verseNumber = 5
        ),
        DailyReflection(
            id = 4,
            arabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            translation = "\"And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.\"",
            reference = "Qur'an 2:186",
            surahNumber = 2,
            verseNumber = 186
        ),
        DailyReflection(
            id = 5,
            arabic = "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ",
            translation = "\"So do not weaken and do not grieve, and you will be superior if you are [true] believers.\"",
            reference = "Qur'an 3:139",
            surahNumber = 3,
            verseNumber = 139
        )
    )

    fun getDuaForDate(date: LocalDate = LocalDate.now()): DailyDua {
        val epochDay = date.toEpochDay()
        val index = (epochDay % authenticDuas.size).toInt().let { if (it < 0) it + authenticDuas.size else it }
        return authenticDuas[index]
    }

    fun getHadithForDate(date: LocalDate = LocalDate.now()): DailyHadith {
        val epochDay = date.toEpochDay()
        val index = (epochDay % authenticHadiths.size).toInt().let { if (it < 0) it + authenticHadiths.size else it }
        return authenticHadiths[index]
    }

    fun getReflections(): List<DailyReflection> {
        return authenticReflections
    }
}
