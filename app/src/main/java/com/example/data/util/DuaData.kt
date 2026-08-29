package com.example.data.util

import androidx.compose.runtime.Immutable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.db.DuaCategoryEntity
import com.example.data.db.DuaEntity

// =========================================================================
// PASTEL THEME PALETTE FOR DUA CATEGORIES
// =========================================================================

enum class PastelTheme(
    val lightBg: Color,
    val lightIcon: Color,
    val darkBg: Color,
    val darkIcon: Color
) {
    SAGE(
        lightBg = Color(0xFFE2EDE6),
        lightIcon = Color(0xFF33634B),
        darkBg = Color(0xFF22362C),
        darkIcon = Color(0xFF86BFA0)
    ),
    DUSTY_BLUE(
        lightBg = Color(0xFFE2EAF4),
        lightIcon = Color(0xFF315579),
        darkBg = Color(0xFF212F3F),
        darkIcon = Color(0xFF89B3DC)
    ),
    TERRACOTTA(
        lightBg = Color(0xFFF6E8E0),
        lightIcon = Color(0xFF8D4A26),
        darkBg = Color(0xFF3B2921),
        darkIcon = Color(0xFFE39974)
    ),
    DUSTY_ROSE(
        lightBg = Color(0xFFF5E3E9),
        lightIcon = Color(0xFF883C56),
        darkBg = Color(0xFF3B222D),
        darkIcon = Color(0xFFD983A2)
    ),
    WARM_TAUPE(
        lightBg = Color(0xFFEEEAE2),
        lightIcon = Color(0xFF6E5E4C),
        darkBg = Color(0xFF343026),
        darkIcon = Color(0xFFC7B6A1)
    )
}

// =========================================================================
// NORMALIZED DUA ENTITY / DATA RECORD
// =========================================================================

@Immutable
data class DuaItem(
    val id: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val category: String, // primary display category
    val categories: List<String> = listOf(category), // all categories this Dua belongs to
    val reference: String? = null,
    val authenticityGrade: String = "Authentic (Sahih)",
    val benefitOrNotes: String? = null,
    val recommendedCount: String? = null,
    val sourceType: String = "Hadith", // "Qur'an" or "Hadith"
    val surahNumber: Int? = null,
    val verseNumber: Int? = null
)

@Immutable
data class DuaCategoryInfo(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val pastelTheme: PastelTheme,
    val duaIds: List<String>
)

// =========================================================================
// CANONICAL DUA DATA REPOSITORY
// =========================================================================

object DuaData {

    // -------------------------------------------------------------------------
    // 1. CANONICAL UNIQUE DUA RECORDS (NO DUPLICATES)
    // -------------------------------------------------------------------------
    private val DUA_LIST: List<DuaItem> = listOf(
        // === PROTECTION ===
        DuaItem(
            id = "prot_1",
            title = "Protection Against All Harm",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shai'un fil-ardi wa la fis-sama'i, wa Huwas-Sami'ul-'Alim.",
            translation = "In the Name of Allah with Whose Name there is protection against every kind of harm in the earth or in the heaven, and He is the All-Hearing and All-Knowing.",
            category = "Protection",
            categories = listOf("Protection", "Daily Life & Home"),
            reference = "Sunan Abi Dawud 5088 & Jami' at-Tirmidhi 3388",
            authenticityGrade = "Authentic (Hasan Sahih)",
            benefitOrNotes = "Recited 3 times in the morning and 3 times in the evening. The Prophet ﷺ stated nothing will harm whoever says it.",
            recommendedCount = "3 times (Morning & Evening)",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "prot_2",
            title = "Ayat al-Kursi (The Throne Verse)",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bi-shay'im-min 'ilmihi illa bima sha'. Wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim.",
            translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
            category = "Protection",
            categories = listOf("Protection", "Sleep & Rest"),
            reference = "Surah Al-Baqarah 2:255 & Sahih al-Bukhari 2311",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Recited after every obligatory prayer and before sleeping; an angelic protector guards the reciter throughout the night.",
            recommendedCount = "After every Fard Prayer & at Bedtime",
            sourceType = "Qur'an",
            surahNumber = 2,
            verseNumber = 255
        ),
        DuaItem(
            id = "prot_3",
            title = "Seeking Refuge in Perfect Words of Allah",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'udhu bikalimatil-lahit-tammati min sharri ma khalaq.",
            translation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            category = "Protection",
            categories = listOf("Protection", "Travel", "Daily Life & Home"),
            reference = "Sahih Muslim 2708",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Whoever recites this when staying at any house or lodging, nothing will harm him until he departs from that place.",
            recommendedCount = "3 times (Evening / Upon Arrival)",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "prot_4",
            title = "The Three Quls (Al-Ikhlas, Al-Falaq, An-Nas)",
            arabic = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            transliteration = "Qul Huwallahu Ahad... Qul a'udhu bi Rabbil-Falaq... Qul a'udhu bi Rabbin-Nas...",
            translation = "Say, 'He is Allah, [who is] One...' Say, 'I seek refuge in the Lord of daybreak...' Say, 'I seek refuge in the Lord of mankind...'",
            category = "Protection",
            categories = listOf("Protection", "Sleep & Rest"),
            reference = "Sunan Abi Dawud 5082 & Jami' at-Tirmidhi 3575",
            authenticityGrade = "Authentic (Hasan Sahih)",
            benefitOrNotes = "Recite 3 times in the morning and evening; the Prophet ﷺ said it will suffice you against all harm.",
            recommendedCount = "3 times (Morning, Evening & Bedtime)",
            sourceType = "Qur'an"
        ),
        DuaItem(
            id = "prot_5",
            title = "Shielding from Anxiety, Debt & Oppression",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allahumma inni a'udhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayni wa ghalabatir-rijal.",
            translation = "O Allah, I seek refuge in You from grief and sadness, from weakness and laziness, from miserliness and cowardice, from the burden of debt, and from being overpowered by people.",
            category = "Protection",
            categories = listOf("Protection", "Anxiety & Sorrow", "Rizq & Provision"),
            reference = "Sahih al-Bukhari 2893 & 5425",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "A comprehensive supplication frequently made by the Prophet ﷺ for complete mental and financial protection.",
            recommendedCount = "Morning & Evening",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "prot_6",
            title = "Sufficiency in Allah (Hasbiyallah)",
            arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azim.",
            translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
            category = "Protection",
            categories = listOf("Protection", "Difficulty & Ease"),
            reference = "Surah At-Tawbah 9:129 & Sunan Abi Dawud 5081",
            authenticityGrade = "Noble Qur'an (Sahih Mawquf)",
            benefitOrNotes = "Whoever recites this 7 times in the morning and evening, Allah will suffice him in all that concerns him of worldly and otherworldly affairs.",
            recommendedCount = "7 times (Morning & Evening)",
            sourceType = "Qur'an",
            surahNumber = 9,
            verseNumber = 129
        ),
        DuaItem(
            id = "prot_7",
            title = "Protection of Children & Family from Evil",
            arabic = "أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
            transliteration = "U'idhukuma bikalimatil-lahit-tammati min kulli shaytanin wa hammah, wa min kulli 'aynin lammah.",
            translation = "I seek refuge for you both in the perfect words of Allah from every devil and poisonous creature, and from every evil, envious eye.",
            category = "Protection",
            categories = listOf("Protection", "Parents & Family"),
            reference = "Sahih al-Bukhari 3371",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ used to seek protection with these words for Al-Hasan and Al-Husayn, noting that Prophet Ibrahim did so for Isma'il and Ishaq.",
            recommendedCount = "For children & loved ones daily",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "prot_8",
            title = "Comprehensive Reliance Upon Leaving Home",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi tawakkaltu 'alallahi, la hawla wa la quwwata illa billah.",
            translation = "In the name of Allah, I place my trust in Allah; there is no might and no power except with Allah.",
            category = "Protection",
            categories = listOf("Protection", "Daily Life & Home", "Travel"),
            reference = "Sunan Abi Dawud 5095 & Jami' at-Tirmidhi 3426",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Angels declare to the reciter: 'You are guided, defended, and protected,' and Shaytan moves away from him.",
            recommendedCount = "Whenever stepping outside the house",
            sourceType = "Hadith"
        ),

        // === ANXIETY & SORROW ===
        DuaItem(
            id = "anx_1",
            title = "Dua of Prophet Yunus (Dhun-Nun)",
            arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimin.",
            translation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Difficulty & Ease", "Forgiveness"),
            reference = "Surah Al-Anbiya 21:87 & Jami' at-Tirmidhi 3505",
            authenticityGrade = "Noble Qur'an (Sahih)",
            benefitOrNotes = "The Prophet ﷺ said: 'No Muslim supplicates with this in any distress except that Allah responds to him.'",
            recommendedCount = "During times of distress & anguish",
            sourceType = "Qur'an",
            surahNumber = 21,
            verseNumber = 87
        ),
        DuaItem(
            id = "anx_2",
            title = "Prayer of Relief from Heavy Grief & Anguish",
            arabic = "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ سَمَّيْتَ بِهِ نَفْسَكَ، أَوْ عَلَّمْتَهُ أَحَدًا مِنْ خَلْقِكَ، أَوْ أَنْزَلْتَهُ فِي كِتَابِكَ، أَوْ اسْتَأْثَرْتَ بِهِ فِي عِلْمِ الْغَيْبِ عِنْدَكَ، أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجَلَاءَ حُزْنِي، وَذَهَابَ هَمِّي",
            transliteration = "Allahumma inni 'abduk, ibnu 'abdik, ibnu amatik, nasiyati bi-yadik, madin fiyya hukmuk, 'adlun fiyya qada'uk, as'aluka bikulli ismin huwa lak, sammayta bihi nafsak, aw 'allamtahu ahadan min khalqik, aw anzaltahu fi kitabik, aw ista'tharta bihi fi 'ilmil-ghaybi 'indak, an taj'alal-Qur'ana rabi'a qalbi, wa nura sadri, wa jala'a huzni, wa dhahaba hammi.",
            translation = "O Allah, I am Your servant, son of Your servant, son of Your maidservant, my forelock is in Your hand, Your command over me is forever executed and Your decree over me is just. I ask You by every name belonging to You which You named Yourself with, or revealed in Your Book, or taught to any of Your creation, or preserved in the secret knowledge with You, that You make the Qur'an the spring of my heart and the light of my chest, the banisher of my grief and the reliever of my anxiety.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Difficulty & Ease"),
            reference = "Musnad Ahmad 3712 & Sahih Ibn Hibban 972",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ guaranteed that Allah will remove sorrow and replace it with joy for whoever recites this.",
            recommendedCount = "When experiencing grief or anxiety",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "anx_3",
            title = "Supplication of the Distressed (Du'a al-Karab)",
            arabic = "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ، وَأَصْلِحْ لِي شَأْنِي كُلَّهُ، لَا إِلَٰهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma rahmataka arju fala takilni ila nafsi tarfata 'ayn, wa aslih li sha'ni kullah, la ilaha illa Ant.",
            translation = "O Allah, it is Your mercy that I hope for, so do not leave me in charge of my affairs even for a blink of an eye, and rectify for me all of my concerns. There is no deity except You.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Difficulty & Ease"),
            reference = "Sunan Abi Dawud 5090 & Musnad Ahmad 20430",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "Specifically prescribed by the Prophet ﷺ as the foundational supplication for someone afflicted by distress.",
            recommendedCount = "Morning, Evening & during difficulty",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "anx_4",
            title = "Calling Upon the Ever-Living, Sustainer",
            arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ",
            transliteration = "Ya Hayyu Ya Qayyum, birahmatika astaghith.",
            translation = "O Ever-Living, O Self-Subsisting Sustainer, by Your mercy I seek assistance.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Difficulty & Ease"),
            reference = "Jami' at-Tirmidhi 3524 & Sunan an-Nasa'i (Al-Kubra 10405)",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "Whenever a matter distressed the Prophet ﷺ, he would pronounce this earnest appeal.",
            recommendedCount = "Repeated in moments of stress & anxiety",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "anx_5",
            title = "Supplication in Severe Calamities",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
            transliteration = "La ilaha illallahul-'Azimul-Halim, la ilaha illallahu Rabbul-'Arshil-'Azim, la ilaha illallahu Rabbus-samawati wa Rabbul-ardi wa Rabbul-'Arshil-Karim.",
            translation = "There is no deity except Allah, the All-Mighty, the Forbearing. There is no deity except Allah, Lord of the Magnificent Throne. There is no deity except Allah, Lord of the heavens and Lord of the earth and Lord of the Noble Throne.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Difficulty & Ease", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 6345 & Sahih Muslim 2730",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The exact formula uttered by the Prophet ﷺ at the onset of severe anguish and critical hardships.",
            recommendedCount = "During moments of crisis",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "anx_6",
            title = "Dua of Prophet Ayyub in Affliction",
            arabic = "أَنِّي مَسَّنِيَ الضُّرُّ وَأَنتَ أَرْحَمُ الرَّاحِمِينَ",
            transliteration = "Anni massaniyad-durru wa Anta arhamur-rahimin.",
            translation = "Indeed, adversity has touched me, and You are the Most Merciful of the merciful.",
            category = "Anxiety & Sorrow",
            categories = listOf("Anxiety & Sorrow", "Health & Healing"),
            reference = "Surah Al-Anbiya 21:83",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The heartfelt Quranic appeal of Prophet Ayyub (Job) in extreme illness and loss, answered immediately by Allah.",
            recommendedCount = "When experiencing trials or illness",
            sourceType = "Qur'an",
            surahNumber = 21,
            verseNumber = 83
        ),

        // === FORGIVENESS ===
        DuaItem(
            id = "forg_1",
            title = "Sayyid al-Istighfar (The Master of Forgiveness)",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, abu'u laka bini'matika 'alay, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Ant.",
            translation = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            category = "Forgiveness",
            categories = listOf("Forgiveness", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 6306",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ said: 'Whoever says this with conviction in the evening and dies that night will enter Paradise, and likewise in the morning.'",
            recommendedCount = "Once in the morning & once in the evening",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "forg_2",
            title = "Supplication for Complete Forgiveness",
            arabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal-'Azim alladhi la ilaha illa Huwal-Hayyul-Qayyum wa atubu ilayh.",
            translation = "I seek the forgiveness of Allah the Magnificent, besides Whom there is no deity, the Ever-Living, the Self-Subsisting Sustainer, and I repent unto Him.",
            category = "Forgiveness",
            categories = listOf("Forgiveness", "Gratitude & Praise"),
            reference = "Sunan Abi Dawud 1517 & Jami' at-Tirmidhi 3577",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ stated that whoever recites this will be forgiven even if he had fled from battle.",
            recommendedCount = "3 times daily",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "forg_3",
            title = "Dua of Adam & Hawwa for Mercy",
            arabic = "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            transliteration = "Rabbana zalamna anfusana wa il-lam taghfir lana wa tarhamna lanakunanna minal-khasirin.",
            translation = "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers.",
            category = "Forgiveness",
            categories = listOf("Forgiveness", "Parents & Family"),
            reference = "Surah Al-A'raf 7:23",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The timeless words of repentance revealed to Adam and Hawwa when seeking Allah's boundless pardon.",
            recommendedCount = "In every sincere Tawbah",
            sourceType = "Qur'an",
            surahNumber = 7,
            verseNumber = 23
        ),
        DuaItem(
            id = "forg_4",
            title = "Dua of Prophet Musa for Pardon",
            arabic = "رَبِّ إِنِّي ظَلَمْتُ نَفْسِي فَاغْفِرْ لِي",
            transliteration = "Rabbi inni zalamtu nafsi faghfir li.",
            translation = "My Lord, indeed I have wronged myself, so forgive me.",
            category = "Forgiveness",
            categories = listOf("Forgiveness"),
            reference = "Surah Al-Qasas 28:16",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Spoken by Prophet Musa (Moses) immediately after his mistake, and Allah pardoned him at once.",
            recommendedCount = "During personal repentance",
            sourceType = "Qur'an",
            surahNumber = 28,
            verseNumber = 16
        ),
        DuaItem(
            id = "forg_5",
            title = "Dua of Laylatul Qadr (The Night of Decree)",
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliteration = "Allahumma innaka 'Afuwwun tuhibbul-'afwa fa'fu 'anni.",
            translation = "O Allah, You are Most Forgiving and You love forgiveness, so forgive me.",
            category = "Forgiveness",
            categories = listOf("Forgiveness"),
            reference = "Jami' at-Tirmidhi 3513 & Sunan Ibn Majah 3850",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Taught by the Prophet ﷺ to 'A'ishah (RA) when she asked what to say if she encountered Laylatul Qadr.",
            recommendedCount = "Abundantly in the last 10 nights of Ramadan & daily",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "forg_6",
            title = "Repentance in the Gathering",
            arabic = "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
            transliteration = "Rabbigh-fir li wa tub 'alayya innaka Antat-Tawwabur-Rahim.",
            translation = "My Lord, forgive me and accept my repentance; indeed You are the Accepter of repentance, the Merciful.",
            category = "Forgiveness",
            categories = listOf("Forgiveness"),
            reference = "Sunan Abi Dawud 1516 & Jami' at-Tirmidhi 3434",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Companions counted the Prophet ﷺ reciting this supplication 100 times in a single sitting.",
            recommendedCount = "100 times daily",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "forg_7",
            title = "Supplication Before the Final Tasleem",
            arabic = "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ وَارْحَمْنِي إِنَّك أَنْتَ الْغَفُورُ الرَّحِيمُ",
            transliteration = "Allahumma inni zalamtu nafsi zulman kathiran, wa la yaghfirudh-dhunuba illa Ant, faghfir li maghfiratan min 'indik warhamni, innaka Antal-Ghafurur-Rahim.",
            translation = "O Allah, I have wronged myself greatly, and none forgives sins except You. So grant me forgiveness from Yourself and have mercy upon me; indeed You are the Forgiving, the Merciful.",
            category = "Forgiveness",
            categories = listOf("Forgiveness"),
            reference = "Sahih al-Bukhari 834 & Sahih Muslim 2705",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Taught by the Prophet ﷺ to Abu Bakr as-Siddiq (RA) to recite in the final sitting of Salah.",
            recommendedCount = "In the Tashahhud before concluding Salah",
            sourceType = "Hadith"
        ),

        // === RIZQ & PROVISION ===
        DuaItem(
            id = "rizq_1",
            title = "Supplication for Settling Heavy Debts",
            arabic = "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
            transliteration = "Allahummak-fini bi-halalika 'an haramik, wa aghnini bi-fadlika 'amman siwak.",
            translation = "O Allah, suffice me with Your lawful against Your prohibited, and make me independent of all besides You by Your bounty.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision", "Difficulty & Ease"),
            reference = "Jami' at-Tirmidhi 3563",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "Ali (RA) taught this saying: 'Even if your debt were like the mountain of Sabir, Allah would pay it off for you.'",
            recommendedCount = "Daily after Salah & in morning/evening",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "rizq_2",
            title = "Dua of Prophet Musa in Need of Provision",
            arabic = "رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ",
            transliteration = "Rabbi inni lima anzalta ilayya min khayrin faqir.",
            translation = "My Lord, indeed I am, for whatever good You would send down to me, in need.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision", "Difficulty & Ease", "Parents & Family"),
            reference = "Surah Al-Qasas 28:24",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Uttered by Musa (AS) in Midian with no home, food, or employment; Allah granted him shelter, employment, and marriage immediately.",
            recommendedCount = "When seeking employment, sustenance, or marital blessings",
            sourceType = "Qur'an",
            surahNumber = 28,
            verseNumber = 24
        ),
        DuaItem(
            id = "rizq_3",
            title = "Seeking Beneficial Knowledge, Pure Rizq & Accepted Deeds",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            transliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbala.",
            translation = "O Allah, I ask You for beneficial knowledge, good (pure and halal) provision, and accepted deeds.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision", "Guidance & Knowledge"),
            reference = "Sunan Ibn Majah 925 & Musnad Ahmad 26521",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Recited every morning by the Prophet ﷺ right after the Tasleem of the Fajr prayer.",
            recommendedCount = "Once every morning after Fajr Salah",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "rizq_4",
            title = "Supplication of the Apostles for Provision",
            arabic = "رَبَّنَا أَنزِلْ عَلَيْنَا مَائِدَةً مِّنَ السَّمَاءِ تَكُونُ لَنَا عِيدًا لِّأَوَّلِنَا وَآخِرِنَا وَآيَةً مِّنكَ ۖ وَارْزُقْنَا وَأَنتَ خَيْرُ الرَّازِقِينَ",
            transliteration = "Rabbana anzil 'alayna ma'idatam-minas-sama'i takunu lana 'idal-li-awwalina wa akhirina wa ayatan mink, warzuqna wa Anta Khayrur-raziqin.",
            translation = "Our Lord, send down to us a table spread from heaven to be for us a festival for the first of us and the last of us and a sign from You. And provide for us, and You are the best of providers.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision"),
            reference = "Surah Al-Ma'idah 5:114",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The supplication of Prophet 'Isa (Jesus) invoking Allah as the Supreme Provider.",
            recommendedCount = "When supplicating for abundance & barakah",
            sourceType = "Qur'an",
            surahNumber = 5,
            verseNumber = 114
        ),
        DuaItem(
            id = "rizq_5",
            title = "Prayer for the Rectification of Religion & Worldly Life",
            arabic = "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ",
            transliteration = "Allahumma aslih li diniyalladhi huwa 'ismatu amri, wa aslih li dunya-yallati fiha ma'ashi, wa aslih li akhiratillati fiha ma'adi, waj'alil-hayata ziyadatan li fi kulli khayr, waj'alil-mawta rahatan li min kulli sharr.",
            translation = "O Allah, rectify for me my religion which is the safeguard of my affairs, and rectify for me my worldly life in which is my livelihood, and rectify for me my Hereafter to which is my return, and make life an increase for me in every good, and make death a comfort for me from every evil.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision", "Guidance & Knowledge", "Gratitude & Praise"),
            reference = "Sahih Muslim 2720",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "A profound supplication encompassing spiritual stability, balanced livelihood, and eternal salvation.",
            recommendedCount = "Daily prayer for life harmony",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "rizq_6",
            title = "Seeking Refuge from Poverty & Disbelief",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma inni a'udhu bika minal-kufri wal-faqr, wa a'udhu bika min 'adhabil-qabr, la ilaha illa Ant.",
            translation = "O Allah, I seek refuge in You from disbelief and poverty, and I seek refuge in You from the torment of the grave. There is no deity except You.",
            category = "Rizq & Provision",
            categories = listOf("Rizq & Provision", "Protection"),
            reference = "Sunan Abi Dawud 5090 & Sunan an-Nasa'i 5465",
            authenticityGrade = "Authentic (Hasan Sahih)",
            benefitOrNotes = "The Prophet ﷺ paired seeking refuge from poverty directly with disbelief, emphasizing financial protection.",
            recommendedCount = "3 times (Morning & Evening)",
            sourceType = "Hadith"
        ),

        // === HEALTH & HEALING ===
        DuaItem(
            id = "health_1",
            title = "Ruqyah of the Prophet for the Sick",
            arabic = "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَأْسَ، اشْفِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            transliteration = "Allahumma Rabban-nas, adhhibil-ba's, ishfi wa Antash-Shafi, la shifa'a illa shifa'uk, shifa'an la yughadiru saqama.",
            translation = "O Allah, Lord of mankind, remove the affliction. Heal, for You are the Healer. There is no healing except Your healing—a healing that leaves behind no illness.",
            category = "Health & Healing",
            categories = listOf("Health & Healing", "Protection"),
            reference = "Sahih al-Bukhari 5743 & Sahih Muslim 2191",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ would wipe his right hand over the sick person while reciting this supplication.",
            recommendedCount = "When visiting or tending to the sick",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "health_2",
            title = "Supplication for Bodily Pain",
            arabic = "بِسْمِ اللَّهِ (ثَلَاثًا)، أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ (سَبْعًا)",
            transliteration = "Bismillah (3x). A'udhu billahi wa qudratihi min sharri ma ajidu wa uhadhir (7x).",
            translation = "In the Name of Allah (3 times). I seek refuge in Allah and in His power from the evil of what I feel and what I fear (7 times).",
            category = "Health & Healing",
            categories = listOf("Health & Healing", "Protection"),
            reference = "Sahih Muslim 2202",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Taught by the Prophet ﷺ to 'Uthman ibn Abi al-'As when complaining of severe bodily pain. Place your hand on the pain site while reciting.",
            recommendedCount = "Say Bismillah 3x, then the refuge formula 7x",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "health_3",
            title = "Seeking Healing Through the Lord of the Throne",
            arabic = "أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ",
            transliteration = "As'alullahal-'Azima Rabbal-'Arshil-'Azimi an yashfiyak.",
            translation = "I ask Allah the Magnificent, Lord of the Magnificent Throne, to heal you.",
            category = "Health & Healing",
            categories = listOf("Health & Healing"),
            reference = "Sunan Abi Dawud 3106 & Jami' at-Tirmidhi 2083",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ said: 'Whoever visits a sick person whose appointed time has not arrived and recites this 7 times, Allah will cure him.'",
            recommendedCount = "7 times beside a sick person",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "health_4",
            title = "Affirmation of Divine Cure",
            arabic = "وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ",
            transliteration = "Wa idha maridtu fa Huwa yashfin.",
            translation = "And when I am ill, it is He who heals me.",
            category = "Health & Healing",
            categories = listOf("Health & Healing", "Gratitude & Praise"),
            reference = "Surah Ash-Shu'ara 26:80",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The affirmation of Prophet Ibrahim (AS) acknowledging that ultimate healing rests purely with Allah.",
            recommendedCount = "Meditative recitation during illness",
            sourceType = "Qur'an",
            surahNumber = 26,
            verseNumber = 80
        ),
        DuaItem(
            id = "health_5",
            title = "Daily Well-Being in Body, Hearing & Sight",
            arabic = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari, la ilaha illa Ant.",
            translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight. There is no deity except You.",
            category = "Health & Healing",
            categories = listOf("Health & Healing", "Protection"),
            reference = "Sunan Abi Dawud 5090 & Musnad Ahmad 20430",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "The Prophet ﷺ preserved this supplication 3 times in the morning and 3 times in the evening without fail.",
            recommendedCount = "3 times (Morning & Evening)",
            sourceType = "Hadith"
        ),

        // === DIFFICULTY & EASE ===
        DuaItem(
            id = "diff_1",
            title = "Prayer for Ease in Hard Matters",
            arabic = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
            transliteration = "Allahumma la sahla illa ma ja'altahu sahla, wa Anta taj'alul-hazna idha shi'ta sahla.",
            translation = "O Allah, there is no ease except in that which You have made easy, and You make difficulty, when You wish, easy.",
            category = "Difficulty & Ease",
            categories = listOf("Difficulty & Ease", "Anxiety & Sorrow"),
            reference = "Sahih Ibn Hibban 974 & Sunan Al-Bayhaqi (As-Sunan Al-Kubra)",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The essential Sunnah prayer when confronting complex exams, hard tasks, or challenging dilemmas.",
            recommendedCount = "When undertaking difficult tasks or decisions",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "diff_2",
            title = "Dua of Musa for Clarity & Expansion of Chest",
            arabic = "رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي ۝ وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي ۝ يَفْقَهُوا قَوْلِي",
            transliteration = "Rabbish-rah li sadri, wa yassir li amri, wahlul 'uqdatam-mil-lisani, yafqahu qawli.",
            translation = "My Lord, expand for me my chest [with assurance], and ease for me my task, and untie the knot from my tongue, that they may understand my speech.",
            category = "Difficulty & Ease",
            categories = listOf("Difficulty & Ease", "Guidance & Knowledge"),
            reference = "Surah Taha 20:25-28",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Revealed when Musa (AS) was commanded to address Pharaoh; ideal for public speaking, interviews, and overcoming anxiety.",
            recommendedCount = "Before speaking, teaching, or facing trials",
            sourceType = "Qur'an",
            surahNumber = 20,
            verseNumber = 25
        ),
        DuaItem(
            id = "diff_3",
            title = "Hasbunallahu wa Ni'mal-Wakil (Sufficiency in the Best Disposer)",
            arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            transliteration = "Hasbunallahu wa ni'mal-Wakil.",
            translation = "Sufficient for us is Allah, and [He is] the best Disposer of affairs.",
            category = "Difficulty & Ease",
            categories = listOf("Difficulty & Ease", "Protection", "Gratitude & Praise"),
            reference = "Surah Ali 'Imran 3:173 & Sahih al-Bukhari 4563",
            authenticityGrade = "Noble Qur'an & Sahih Hadith",
            benefitOrNotes = "Uttered by Ibrahim (AS) when cast into the fire and by Muhammad ﷺ when people gathered against him.",
            recommendedCount = "Repeated during tests, oppression, or worry",
            sourceType = "Qur'an",
            surahNumber = 3,
            verseNumber = 173
        ),
        DuaItem(
            id = "diff_4",
            title = "Treasury of Paradise (La Hawla)",
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "La hawla wa la quwwata illa billah.",
            translation = "There is no power and no strength except with Allah.",
            category = "Difficulty & Ease",
            categories = listOf("Difficulty & Ease", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 4205 & Sahih Muslim 2704",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ described this statement as 'a treasure from beneath the Throne of Allah in Paradise.'",
            recommendedCount = "Abundantly throughout the day",
            sourceType = "Hadith"
        ),

        // === SLEEP & REST ===
        DuaItem(
            id = "sleep_1",
            title = "Supplication When Lying Down to Sleep",
            arabic = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbi wada'tu janbi wa bika arfa'uh, fa-in amsakta nafsi farhamha, wa in arsaltaha fahfazha bima tahfazu bihi 'ibadakas-salihin.",
            translation = "In Your name, my Lord, I lay down my side, and by You I raise it. If You take my soul, have mercy upon it; and if You send it back, protect it with that wherewith You protect Your righteous servants.",
            category = "Sleep & Rest",
            categories = listOf("Sleep & Rest", "Protection"),
            reference = "Sahih al-Bukhari 6320 & Sahih Muslim 2714",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ instructed dusting the bed thrice and reciting this upon lying on the right side.",
            recommendedCount = "Every night upon getting into bed",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "sleep_2",
            title = "Bedtime Affirmation of Life & Death",
            arabic = "اللَّهُمَّ بِاسْمِكَ أَمُوتُ وَأَحْيَا",
            transliteration = "Allahumma bismika amutu wa ahya.",
            translation = "O Allah, in Your Name I die and I live.",
            category = "Sleep & Rest",
            categories = listOf("Sleep & Rest"),
            reference = "Sahih al-Bukhari 6312 & Sahih Muslim 2711",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Recited as the final phrase spoken before falling asleep.",
            recommendedCount = "Immediately before sleeping",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "sleep_3",
            title = "Supplication Upon Waking Up",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
            translation = "All praise is for Allah Who gave us life after having caused us to die, and unto Him is the resurrection.",
            category = "Sleep & Rest",
            categories = listOf("Sleep & Rest", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 6312 & Sahih Muslim 2711",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The first praise uttered by the Prophet ﷺ upon waking in the morning.",
            recommendedCount = "Immediately upon waking",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "sleep_4",
            title = "Protection Against Punishment in Hereafter",
            arabic = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
            transliteration = "Allahumma qini 'adhabaka yawma tab'athu 'ibadak.",
            translation = "O Allah, protect me from Your punishment on the Day You resurrect Your servants.",
            category = "Sleep & Rest",
            categories = listOf("Sleep & Rest", "Forgiveness"),
            reference = "Sunan Abi Dawud 5045 & Jami' at-Tirmidhi 3398",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ would place his right hand under his cheek and recite this 3 times.",
            recommendedCount = "3 times before sleeping",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "sleep_5",
            title = "Praise for Food, Drink & Shelter at Bedtime",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا، وَكَفَانَا، وَآوَانَا، فَكَمْ مِمَّنْ لَا كَافِيَ لَهُ وَلَا مُؤْوِيَ",
            transliteration = "Alhamdu lillahil-ladhi at'amana wa saqana, wa kafana, wa awana, fakam mimman la kafiya lahu wa la mu'wiya.",
            translation = "All praise is for Allah Who fed us, gave us drink, sufficed us, and gave us shelter; for how many are there who have none to suffice them and none to give them shelter.",
            category = "Sleep & Rest",
            categories = listOf("Sleep & Rest", "Gratitude & Praise"),
            reference = "Sahih Muslim 2715",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ would utter this reflection on blessings each night before sleep.",
            recommendedCount = "Bedtime praise",
            sourceType = "Hadith"
        ),

        // === TRAVEL ===
        DuaItem(
            id = "travel_1",
            title = "Supplication for Mounting & Journeying",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ ۝ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            transliteration = "Subhanal-ladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun.",
            translation = "Glory be to Him Who has subjected this to us, and we could never have achieved it by ourselves. And indeed, to our Lord we will return.",
            category = "Travel",
            categories = listOf("Travel", "Gratitude & Praise"),
            reference = "Surah Az-Zukhruf 43:13-14 & Sahih Muslim 1342",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Recited upon boarding any vehicle, car, plane, or conveyance for travel.",
            recommendedCount = "Upon mounting or departing in a vehicle",
            sourceType = "Qur'an",
            surahNumber = 43,
            verseNumber = 13
        ),
        DuaItem(
            id = "travel_2",
            title = "Comprehensive Travel Supplication",
            arabic = "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَىٰ، وَمِنَ الْعَمَلِ مَا تَرْضَىٰ، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَٰذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالْخَلِيفَةُ فِي الْأَهْلِ",
            transliteration = "Allahumma inna nas'aluka fi safarina hadhal-birra wat-taqwa, wa minal-'amali ma tarda. Allahumma hawwin 'alayna safarana hadha watwi 'anna bu'dah. Allahumma Antas-sahibu fis-safar, wal-khalifatu fil-ahl.",
            translation = "O Allah, we ask You in this journey of ours for righteousness and piety, and for deeds that please You. O Allah, ease for us this journey of ours and make its distance short for us. O Allah, You are the Companion in travel and the Caretaker of the family.",
            category = "Travel",
            categories = listOf("Travel", "Protection"),
            reference = "Sahih Muslim 1342",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The full Sunnah supplication for journeys, seeking physical safety and spiritual steadfastness.",
            recommendedCount = "At the start of every travel journey",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "travel_3",
            title = "Entrusting Someone Departing for Travel",
            arabic = "أَسْتَوْدِعُ اللَّهَ دِينَكَ وَأَمَانَتَكَ وَخَوَاتِيمَ عَمَلِكَ",
            transliteration = "Astawdi'ullaha dinaka wa amanataka wa khawatima 'amalik.",
            translation = "I entrust to Allah your religion, your trusts, and the final outcomes of your deeds.",
            category = "Travel",
            categories = listOf("Travel", "Parents & Family"),
            reference = "Sunan Abi Dawud 2600 & Jami' at-Tirmidhi 3443",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Said to a traveling brother, sister, child, or friend when bidding them farewell.",
            recommendedCount = "When seeing off a traveler",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "travel_4",
            title = "Supplication Upon Returning from Travel",
            arabic = "آيِبُونَ، تَائِبُونَ، عَابِدُونَ، لِرَبِّنَا حَامِدُونَ",
            transliteration = "Ayibuna, ta'ibuna, 'abiduna, li Rabbina hamidun.",
            translation = "We return, repentant, worshiping, and praising our Lord.",
            category = "Travel",
            categories = listOf("Travel", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 1799 & Sahih Muslim 1342",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Repeated by the Prophet ﷺ upon catching sight of his home city after completing a journey.",
            recommendedCount = "Upon returning home from travel",
            sourceType = "Hadith"
        ),

        // === PARENTS & FAMILY ===
        DuaItem(
            id = "family_1",
            title = "Quranic Prayer for Mercy upon Parents",
            arabic = "رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir-hamhuma kama rabbayani saghira.",
            translation = "My Lord, have mercy upon them as they brought me up when I was small.",
            category = "Parents & Family",
            categories = listOf("Parents & Family", "Forgiveness"),
            reference = "Surah Al-Isra 17:24",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The divine Quranic injunction to supplicate for parents in lifetime and after their passing.",
            recommendedCount = "Daily in every prayer and reflection",
            sourceType = "Qur'an",
            surahNumber = 17,
            verseNumber = 24
        ),
        DuaItem(
            id = "family_2",
            title = "Dua of Ibrahim for Parents & Believers",
            arabic = "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
            transliteration = "Rabbanagh-fir li wa liwalidayya wa lil-mu'minina yawma yaqumul-hisab.",
            translation = "Our Lord, forgive me and my parents and the believers the Day the account is established.",
            category = "Parents & Family",
            categories = listOf("Parents & Family", "Forgiveness"),
            reference = "Surah Ibrahim 14:41",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The expansive supplication of Prophet Ibrahim (AS) for his family and all believers until the Day of Judgment.",
            recommendedCount = "In Tashahhud & Qunut",
            sourceType = "Qur'an",
            surahNumber = 14,
            verseNumber = 41
        ),
        DuaItem(
            id = "family_3",
            title = "Supplication for Righteous Spouse & Offspring",
            arabic = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            transliteration = "Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj'alna lil-muttaqina imama.",
            translation = "Our Lord, grant us from among our spouses and offspring comfort to our eyes and make us an example for the righteous.",
            category = "Parents & Family",
            categories = listOf("Parents & Family"),
            reference = "Surah Al-Furqan 25:74",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The supplication of the Servants of the Most Merciful ('Ibadur-Rahman) for family harmony and moral leadership.",
            recommendedCount = "For marital peace & righteous children",
            sourceType = "Qur'an",
            surahNumber = 25,
            verseNumber = 74
        ),
        DuaItem(
            id = "family_4",
            title = "Dua of Zakariyya for Righteous Offspring",
            arabic = "رَبِّ هَبْ لِي مِن لَّدُنكَ ذُرِّيَّةً طَيِّبَةً ۖ إِنَّكَ سَمِيعُ الدُّعَاءِ",
            transliteration = "Rabbi hab li mil-ladunka dhurriyyatan tayyibatan, innaka Sami'ud-du'a.",
            translation = "My Lord, grant me from Yourself a good offspring. Indeed, You are the Hearer of supplication.",
            category = "Parents & Family",
            categories = listOf("Parents & Family"),
            reference = "Surah Ali 'Imran 3:38",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "Uttered by Prophet Zakariyya (AS) when elderly, and granted Yahya (John) as a righteous son.",
            recommendedCount = "When praying for children",
            sourceType = "Qur'an",
            surahNumber = 3,
            verseNumber = 38
        ),
        DuaItem(
            id = "family_5",
            title = "Gratitude for Parents & Rectification of Lineage",
            arabic = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ وَأَصْلِحْ لِي فِي ذُرِّيَّتِي ۖ إِنِّي تُبْتُ إِلَيْكَ وَإِنِّي مِنَ الْمُسْلِمِينَ",
            transliteration = "Rabbi awzi'ni an ashkura ni'matakal-lati an'amta 'alayya wa 'ala walidayya wa an a'mala salihan tardahu wa aslih li fi dhurriyyati, inni tubtu ilayka wa inni minal-muslimin.",
            translation = "My Lord, enable me to be grateful for Your favor which You have bestowed upon me and upon my parents and to work righteousness of which You will approve and make righteous for me my offspring. Indeed, I have repented to You, and indeed, I am of the Muslims.",
            category = "Parents & Family",
            categories = listOf("Parents & Family", "Gratitude & Praise"),
            reference = "Surah Al-Ahqaf 46:15",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The Quranic prayer of maturity reaching 40 years of age, honoring parents and future generations.",
            recommendedCount = "For family righteousness & gratitude",
            sourceType = "Qur'an",
            surahNumber = 46,
            verseNumber = 15
        ),

        // === GUIDANCE & KNOWLEDGE ===
        DuaItem(
            id = "know_1",
            title = "Supplication for Increase in Knowledge",
            arabic = "رَّبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma.",
            translation = "My Lord, increase me in knowledge.",
            category = "Guidance & Knowledge",
            categories = listOf("Guidance & Knowledge"),
            reference = "Surah Taha 20:114",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The only matter about which Allah directly commanded Prophet Muhammad ﷺ to pray for an increase.",
            recommendedCount = "Before studying or reading",
            sourceType = "Qur'an",
            surahNumber = 20,
            verseNumber = 114
        ),
        DuaItem(
            id = "know_2",
            title = "Supplication for Steadfastness of Heart",
            arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَىٰ دِينِكَ",
            transliteration = "Ya Muqallibal-qulub, thabbit qalbi 'ala dinik.",
            translation = "O Turner of the hearts, make my heart firm upon Your religion.",
            category = "Guidance & Knowledge",
            categories = listOf("Guidance & Knowledge", "Forgiveness"),
            reference = "Jami' at-Tirmidhi 2140 & Sunan Ibn Majah 3834",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Umm Salamah (RA) noted this was the most frequent supplication made by the Prophet ﷺ.",
            recommendedCount = "Daily in Sujood & after prayers",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "know_3",
            title = "Seeking Guidance, Piety, Modesty & Contentment",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَىٰ وَالتُّقَىٰ وَالْعَفَافَ وَالْغِنَىٰ",
            transliteration = "Allahumma inni as'alukal-huda wat-tuqa wal-'afafa wal-ghina.",
            translation = "O Allah, I ask You for guidance, piety, chastity (self-restraint), and contentment (independence of means).",
            category = "Guidance & Knowledge",
            categories = listOf("Guidance & Knowledge", "Rizq & Provision"),
            reference = "Sahih Muslim 2722",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "A complete, noble four-pillar prayer encompassing inner spiritual light and dignified outward living.",
            recommendedCount = "Daily supplication",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "know_4",
            title = "Protection of the Heart from Deviation",
            arabic = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            transliteration = "Rabbana la tuzigh qulubana ba'da idh hadaytana wa hab lana mil-ladunka rahmah, innaka Antal-Wahhab.",
            translation = "Our Lord, let not our hearts deviate after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower.",
            category = "Guidance & Knowledge",
            categories = listOf("Guidance & Knowledge", "Forgiveness"),
            reference = "Surah Ali 'Imran 3:8",
            authenticityGrade = "Noble Qur'an (Mutawatir)",
            benefitOrNotes = "The supplication of those firmly grounded in knowledge (Ar-Rasikhuna fil-'Ilm).",
            recommendedCount = "To safeguard faith & clarity",
            sourceType = "Qur'an",
            surahNumber = 3,
            verseNumber = 8
        ),
        DuaItem(
            id = "know_5",
            title = "Supplication of Istikhara (Divine Guidance in Decisions)",
            arabic = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ",
            transliteration = "Allahumma inni astakhiruka bi-'ilmika wa astaqdiruka bi-qudratika wa as'aluka min fadlikal-'azim, fa-innaka taqdiru wa la aqdir, wa ta'lamu wa la a'lam, wa Anta 'Allamul-ghuyub.",
            translation = "O Allah, I seek Your counsel through Your knowledge, and I seek ability through Your power, and I ask You of Your immense bounty. For You are capable and I am not, and You know and I do not, and You are the Knower of all unseen matters.",
            category = "Guidance & Knowledge",
            categories = listOf("Guidance & Knowledge", "Difficulty & Ease"),
            reference = "Sahih al-Bukhari 1162",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ taught the Companions this prayer for all decisions just as he taught them a Surah from the Qur'an.",
            recommendedCount = "After 2 voluntary Rakahs when facing decisions",
            sourceType = "Hadith"
        ),

        // === GRATITUDE & PRAISE ===
        DuaItem(
            id = "grat_1",
            title = "Glorification Measuring Creation & the Throne",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "Subhanallahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatih.",
            translation = "Glory be to Allah and praise is due to Him, as much as the number of His creation, according to His pleasure, by the weight of His Throne, and by the ink of His words.",
            category = "Gratitude & Praise",
            categories = listOf("Gratitude & Praise", "Daily Life & Home"),
            reference = "Sahih Muslim 2726",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ told Juwayriyah (RA): 'If these four phrases were weighed against all your dhikr this morning, they would outweigh it.'",
            recommendedCount = "3 times every morning",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "grat_2",
            title = "Two Light Phrases Heavy on the Scale",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            transliteration = "Subhanallahi wa bihamdihi, Subhanallahil-'Azim.",
            translation = "Glory be to Allah and praise Him; Glory be to Allah the Magnificent.",
            category = "Gratitude & Praise",
            categories = listOf("Gratitude & Praise"),
            reference = "Sahih al-Bukhari 6406 & Sahih Muslim 2694",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ concluded Sahih al-Bukhari with this: 'Two phrases light on the tongue, heavy on the scale, beloved to the Most Merciful.'",
            recommendedCount = "Constantly on the tongue",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "grat_3",
            title = "Supplication to Aid in Remembrance & Gratitude",
            arabic = "اللَّهُمَّ أَعِنِّي عَلَىٰ ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            transliteration = "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik.",
            translation = "O Allah, help me to remember You, to be grateful to You, and to worship You in an excellent manner.",
            category = "Gratitude & Praise",
            categories = listOf("Gratitude & Praise", "Guidance & Knowledge"),
            reference = "Sunan Abi Dawud 1522 & Sunan an-Nasa'i 1303",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The Prophet ﷺ took Mu'adh ibn Jabal (RA) by the hand and advised him: 'Never omit saying this after every prayer.'",
            recommendedCount = "After every obligatory Salah",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "grat_4",
            title = "Praise Upon Seeing Good Happen",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
            transliteration = "Alhamdu lillahil-ladhi bini'matihi tatimmus-salihat.",
            translation = "All praise is for Allah by Whose favor good things are accomplished.",
            category = "Gratitude & Praise",
            categories = listOf("Gratitude & Praise", "Daily Life & Home"),
            reference = "Sunan Ibn Majah 3803 & Al-Mustadrak 'ala al-Sahihayn 1872",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Uttered by the Prophet ﷺ whenever he witnessed an achievement, blessing, or joyful event.",
            recommendedCount = "Upon seeing accomplishments & blessings",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "grat_5",
            title = "The Comprehensive Good in Dunya & Akhirah",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
            translation = "Our Lord, give us in this world that which is good and in the Hereafter that which is good and protect us from the punishment of the Fire.",
            category = "Gratitude & Praise",
            categories = listOf("Gratitude & Praise", "Forgiveness", "Daily Life & Home"),
            reference = "Surah Al-Baqarah 2:201 & Sahih al-Bukhari 6389",
            authenticityGrade = "Noble Qur'an & Sahih Hadith",
            benefitOrNotes = "Anas (RA) reported that this was the most frequent supplication uttered by the Prophet ﷺ throughout his entire life.",
            recommendedCount = "In Tawaf, Sujood, and daily prayers",
            sourceType = "Qur'an",
            surahNumber = 2,
            verseNumber = 201
        ),

        // === DAILY LIFE & HOME ===
        DuaItem(
            id = "home_1",
            title = "Supplication Before Eating a Meal",
            arabic = "بِسْمِ اللَّهِ، (فَإِنْ نَسِيَ: بِسْمِ اللَّهِ أَوَّلَهُ وَآخِرَهُ)",
            transliteration = "Bismillah (If forgotten: Bismillahi awwalahu wa akhirahu).",
            translation = "In the Name of Allah (If forgotten at start: In the Name of Allah at its beginning and its end).",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Sunan Abi Dawud 3767 & Jami' at-Tirmidhi 1858",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Prevents Shaytan from sharing in the sustenance and invites divine barakah into the meal.",
            recommendedCount = "Before eating or drinking",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_2",
            title = "Supplication After Finishing a Meal",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            transliteration = "Alhamdulillahi-lladhi at'amana wa saqana wa ja'alana muslimin.",
            translation = "All praise is for Allah Who gave us food and drink, and made us among those who submit (Muslims).",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Sunan Abi Dawud 3850 & Jami' at-Tirmidhi 3457",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "Sunnah praise after concluding meals in deep acknowledgment of sustenance.",
            recommendedCount = "After concluding meals",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_3",
            title = "Supplication Upon Entering the Mosque",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaftah li abwaba rahmatik.",
            translation = "O Allah, open for me the doors of Your mercy.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Sahih Muslim 713 & Sunan Abi Dawud 465",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Step in with the right foot while sending Salawat and reciting this prayer.",
            recommendedCount = "When entering the Masjid",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_4",
            title = "Supplication Upon Leaving the Mosque",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allahumma inni as'aluka min fadlik.",
            translation = "O Allah, I ask You from Your bounty.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Rizq & Provision"),
            reference = "Sahih Muslim 713 & Sunan an-Nasa'i 729",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Step out with the left foot while seeking Allah's lawful bounty in the world.",
            recommendedCount = "When leaving the Masjid",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_5",
            title = "Supplication When It Rains",
            arabic = "اللَّهُمَّ صَيِّبًا نَافِعًا",
            transliteration = "Allahumma sayyiban nafi'a.",
            translation = "O Allah, make it a beneficial and abundant rain.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Sahih al-Bukhari 1032",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "Recited during rainfall, a blessed time when prayers are directly answered.",
            recommendedCount = "Whenever rain descends",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_6",
            title = "Supplication When Wearing New Clothes",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَٰذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahil-ladhi kasani hadhath-thawba wa razaqanihi min ghayri hawlim-minni wa la quwwah.",
            translation = "All praise is for Allah Who has clothed me with this garment and provided it for me without any might or power on my part.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Sunan Abi Dawud 4023 & Jami' at-Tirmidhi 3458",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "The Prophet ﷺ stated that whoever recites this upon dressing will have their past minor sins forgiven.",
            recommendedCount = "When putting on clothes",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_7",
            title = "Supplication When Entering the Market or Mall",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ، بِيَدِهِ الْخَيْرُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illallahu Wahdahu la sharika lahu, Lahul-mulku wa Lahul-hamdu, yuhyi wa yumitu wa Huwa Hayyun la yamutu, biyadihil-khayru wa Huwa 'ala kulli shay'in Qadir.",
            translation = "There is no god but Allah alone without partner; to Him belongs sovereignty and praise. He gives life and causes death, and He is Ever-Living and dies not. In His Hand is all good, and He is over all things competent.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Gratitude & Praise"),
            reference = "Jami' at-Tirmidhi 3428 & Sunan Ibn Majah 2235",
            authenticityGrade = "Authentic (Hasan)",
            benefitOrNotes = "The Prophet ﷺ stated Allah writes a million good deeds, erases a million sins, and raises a million degrees for whoever says this in the marketplace.",
            recommendedCount = "When entering markets, malls or crowded commerce areas",
            sourceType = "Hadith"
        ),
        DuaItem(
            id = "home_8",
            title = "Supplication When Exiting the Restroom",
            arabic = "غُفْرَانَكَ",
            transliteration = "Ghufranak.",
            translation = "I seek Your forgiveness.",
            category = "Daily Life & Home",
            categories = listOf("Daily Life & Home", "Forgiveness"),
            reference = "Sunan Abi Dawud 30 & Jami' at-Tirmidhi 7",
            authenticityGrade = "Authentic (Sahih)",
            benefitOrNotes = "The constant Sunnah of the Prophet ﷺ upon stepping outside the restroom.",
            recommendedCount = "Every time after leaving the restroom",
            sourceType = "Hadith"
        )
    )

    // Distinct unique lookup map
    val DUA_MAP: Map<String, DuaItem> = DUA_LIST.associateBy { it.id }

    val ALL_DUAS: List<DuaItem> = DUA_LIST

    // -------------------------------------------------------------------------
    // 2. CATEGORY DEFINITIONS (REFERENCING DUA IDS, NOT DUPLICATED OBJECTS)
    // -------------------------------------------------------------------------
    val CATEGORIES: List<DuaCategoryInfo> = listOf(
        DuaCategoryInfo(
            id = "protection",
            title = "Protection",
            description = "Shielding from all harm, evil & affliction",
            icon = Icons.Outlined.Shield,
            pastelTheme = PastelTheme.SAGE,
            duaIds = listOf("prot_1", "prot_2", "prot_3", "prot_4", "prot_5", "prot_6", "prot_7", "prot_8")
        ),
        DuaCategoryInfo(
            id = "anxiety_sorrow",
            title = "Anxiety & Sorrow",
            description = "Relief from distress, grief & anguish",
            icon = Icons.Outlined.FavoriteBorder,
            pastelTheme = PastelTheme.DUSTY_BLUE,
            duaIds = listOf("anx_1", "anx_2", "anx_3", "anx_4", "anx_5", "anx_6", "prot_5")
        ),
        DuaCategoryInfo(
            id = "forgiveness",
            title = "Forgiveness",
            description = "Tawbah & seeking Allah's boundless mercy",
            icon = Icons.Outlined.CleanHands,
            pastelTheme = PastelTheme.DUSTY_ROSE,
            duaIds = listOf("forg_1", "forg_2", "forg_3", "forg_4", "forg_5", "forg_6", "forg_7", "anx_1")
        ),
        DuaCategoryInfo(
            id = "rizq_provision",
            title = "Rizq & Provision",
            description = "Halal barakah, debt relief & sufficiency",
            icon = Icons.Outlined.AccountBalanceWallet,
            pastelTheme = PastelTheme.TERRACOTTA,
            duaIds = listOf("rizq_1", "rizq_2", "rizq_3", "rizq_4", "rizq_5", "rizq_6", "prot_5")
        ),
        DuaCategoryInfo(
            id = "health_healing",
            title = "Health & Healing",
            description = "Shifa & cure in illness & pain",
            icon = Icons.Outlined.Favorite,
            pastelTheme = PastelTheme.SAGE,
            duaIds = listOf("health_1", "health_2", "health_3", "health_4", "health_5", "anx_6")
        ),
        DuaCategoryInfo(
            id = "difficulty_ease",
            title = "Difficulty & Ease",
            description = "Overcoming hardships & trials",
            icon = Icons.Outlined.AutoAwesome,
            pastelTheme = PastelTheme.WARM_TAUPE,
            duaIds = listOf("diff_1", "diff_2", "diff_3", "diff_4", "anx_1", "anx_3", "prot_6", "rizq_1")
        ),
        DuaCategoryInfo(
            id = "sleep_rest",
            title = "Sleep & Rest",
            description = "Peaceful night & waking remembrance",
            icon = Icons.Outlined.NightsStay,
            pastelTheme = PastelTheme.DUSTY_BLUE,
            duaIds = listOf("sleep_1", "sleep_2", "sleep_3", "sleep_4", "sleep_5", "prot_2", "prot_4")
        ),
        DuaCategoryInfo(
            id = "travel",
            title = "Travel",
            description = "Safety on paths & journeys",
            icon = Icons.Outlined.Explore,
            pastelTheme = PastelTheme.WARM_TAUPE,
            duaIds = listOf("travel_1", "travel_2", "travel_3", "travel_4", "prot_3", "prot_8")
        ),
        DuaCategoryInfo(
            id = "parents_family",
            title = "Parents & Family",
            description = "Righteousness & unity in the home",
            icon = Icons.Outlined.People,
            pastelTheme = PastelTheme.DUSTY_ROSE,
            duaIds = listOf("family_1", "family_2", "family_3", "family_4", "family_5", "prot_7", "rizq_2", "forg_3")
        ),
        DuaCategoryInfo(
            id = "guidance_knowledge",
            title = "Guidance & Knowledge",
            description = "Wisdom, clarity & steadfastness",
            icon = Icons.Outlined.MenuBook,
            pastelTheme = PastelTheme.SAGE,
            duaIds = listOf("know_1", "know_2", "know_3", "know_4", "know_5", "diff_2", "rizq_3")
        ),
        DuaCategoryInfo(
            id = "gratitude_praise",
            title = "Gratitude & Praise",
            description = "Shukr & glorification of Allah",
            icon = Icons.Outlined.VolunteerActivism,
            pastelTheme = PastelTheme.TERRACOTTA,
            duaIds = listOf("grat_1", "grat_2", "grat_3", "grat_4", "grat_5", "forg_1", "diff_3", "diff_4")
        ),
        DuaCategoryInfo(
            id = "daily_home",
            title = "Daily Life & Home",
            description = "Blessing entrances, exits, meals & routines",
            icon = Icons.Outlined.Home,
            pastelTheme = PastelTheme.WARM_TAUPE,
            duaIds = listOf("home_1", "home_2", "home_3", "home_4", "home_5", "home_6", "home_7", "home_8", "prot_1", "prot_8")
        )
    )

    fun getCategoryByIdOrTitle(idOrTitle: String): DuaCategoryInfo? {
        return CATEGORIES.find {
            it.id.equals(idOrTitle, ignoreCase = true) ||
            it.title.equals(idOrTitle, ignoreCase = true)
        }
    }

    fun getDuasForCategory(categoryTitleOrId: String): List<DuaItem> {
        val cat = getCategoryByIdOrTitle(categoryTitleOrId)
        return if (cat != null) {
            cat.duaIds.mapNotNull { DUA_MAP[it] }
        } else {
            ALL_DUAS.filter {
                it.category.equals(categoryTitleOrId, ignoreCase = true) ||
                it.categories.any { c -> c.equals(categoryTitleOrId, ignoreCase = true) }
            }
        }
    }

    fun getCategoryCount(categoryTitleOrId: String): Int {
        val cat = getCategoryByIdOrTitle(categoryTitleOrId)
        return cat?.duaIds?.size ?: getDuasForCategory(categoryTitleOrId).size
    }

    fun getDuaById(id: String): DuaItem? {
        return DUA_MAP[id]
    }

    fun getFeaturedDua(dayOfYear: Int): DuaItem {
        val safeIndex = (if (dayOfYear < 0) 0 else dayOfYear) % ALL_DUAS.size
        return ALL_DUAS[safeIndex]
    }

    // Room Entity bridge helpers
    fun toDuaEntities(): List<DuaEntity> {
        return ALL_DUAS.mapIndexed { index, dua ->
            DuaEntity(
                id = dua.id,
                categoryId = dua.category,
                categoryTitle = dua.category,
                title = dua.title,
                arabic = dua.arabic,
                transliteration = dua.transliteration,
                translation = dua.translation,
                reference = dua.reference,
                displayOrder = index
            )
        }
    }

    fun toCategoryEntities(): List<DuaCategoryEntity> {
        return CATEGORIES.mapIndexed { index, cat ->
            DuaCategoryEntity(
                id = cat.id,
                title = cat.title,
                description = cat.description,
                iconName = cat.title,
                displayOrder = index
            )
        }
    }
}
