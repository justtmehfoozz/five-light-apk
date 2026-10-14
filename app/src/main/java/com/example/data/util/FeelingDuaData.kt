package com.example.data.util

import androidx.compose.runtime.Immutable

/**
 * Filter categories for organizing the 50 emotions.
 */
enum class EmotionCategoryFilter(val label: String) {
    ALL("All"),
    DIFFICULT("Difficult feelings"),
    POSITIVE("Positive feelings"),
    RELATIONSHIPS("Relationships"),
    MOTIVATION("Motivation and uncertainty")
}

/**
 * Representation of an Emotion entry.
 */
@Immutable
data class EmotionItem(
    val id: String,
    val name: String,
    val categoryFilter: EmotionCategoryFilter,
    val secondaryFilters: List<EmotionCategoryFilter> = emptyList(),
    val description: String? = null
) {
    fun matchesFilter(filter: EmotionCategoryFilter): Boolean {
        if (filter == EmotionCategoryFilter.ALL) return true
        return categoryFilter == filter || secondaryFilters.contains(filter)
    }

    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        return name.contains(query, ignoreCase = true)
    }
}

/**
 * Verified Supplication (Dua) record tied to an emotion or general state.
 */
@Immutable
data class VerifiedDuaRecord(
    val id: String,
    val emotionId: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val sourceCollection: String,
    val referenceNumber: String,
    val authenticityGrade: String,
    val gradingAuthority: String,
    val relevanceType: RelevanceType,
    val contextExplanation: String? = null,
    val translationSource: String? = null,
    val sourceUrl: String? = null,
    val isSuicidalEmergency: Boolean = false
)

enum class RelevanceType(val displayLabel: String) {
    SPECIFICALLY_REPORTED("Specifically Reported in Sunnah"),
    GENERALLY_APPLICABLE("Generally Applicable Supplication")
}

/**
 * Repository containing all 50 emotions and verified authentic Islamic supplications.
 */
object FeelingDuaData {

    // -------------------------------------------------------------------------
    // 1. THE MANDATORY 50 EMOTIONS
    // -------------------------------------------------------------------------
    val ALL_EMOTIONS: List<EmotionItem> = listOf(
        EmotionItem("angry", "Angry", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("anxious", "Anxious", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("bored", "Bored", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("confident", "Confident", EmotionCategoryFilter.POSITIVE, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("confused", "Confused", EmotionCategoryFilter.MOTIVATION),
        EmotionItem("content", "Content", EmotionCategoryFilter.POSITIVE),
        EmotionItem("depressed", "Depressed", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("doubtful", "Doubtful", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("grateful", "Grateful", EmotionCategoryFilter.POSITIVE),
        EmotionItem("greedy", "Greedy", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("guilty", "Guilty", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("happy", "Happy", EmotionCategoryFilter.POSITIVE),
        EmotionItem("hurt", "Hurt", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("hypocritical", "Hypocritical", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("indecisive", "Indecisive", EmotionCategoryFilter.MOTIVATION),
        EmotionItem("jealous", "Jealous", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("lazy", "Lazy", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("lonely", "Lonely", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("lost", "Lost", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("nervous", "Nervous", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("overwhelmed", "Overwhelmed", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("regret", "Regret", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("sad", "Sad", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("scared", "Scared", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("suicidal", "Suicidal", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("tired", "Tired", EmotionCategoryFilter.DIFFICULT),
        EmotionItem("unloved", "Unloved", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("weak", "Weak", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("anticipation", "Anticipation", EmotionCategoryFilter.POSITIVE, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("aroused", "Aroused", EmotionCategoryFilter.RELATIONSHIPS),
        EmotionItem("curious", "Curious", EmotionCategoryFilter.POSITIVE, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("defeated", "Defeated", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("desire", "Desire", EmotionCategoryFilter.RELATIONSHIPS, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("desperate", "Desperate", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("determined", "Determined", EmotionCategoryFilter.POSITIVE, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("disbelief", "Disbelief", EmotionCategoryFilter.MOTIVATION),
        EmotionItem("envious", "Envious", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("hatred", "Hatred", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("humiliated", "Humiliated", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("impatient", "Impatient", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION)),
        EmotionItem("insecure", "Insecure", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("irritated", "Irritated", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("love", "Love", EmotionCategoryFilter.POSITIVE, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("nostalgic", "Nostalgic", EmotionCategoryFilter.POSITIVE),
        EmotionItem("offended", "Offended", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("peaceful", "Peaceful", EmotionCategoryFilter.POSITIVE),
        EmotionItem("rage", "Rage", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.RELATIONSHIPS)),
        EmotionItem("satisfied", "Satisfied", EmotionCategoryFilter.POSITIVE),
        EmotionItem("uncertain", "Uncertain", EmotionCategoryFilter.MOTIVATION),
        EmotionItem("uneasy", "Uneasy", EmotionCategoryFilter.DIFFICULT, listOf(EmotionCategoryFilter.MOTIVATION))
    )

    // -------------------------------------------------------------------------
    // 2. VERIFIED AUTHENTIC SUPPLICATIONS MAP (BY EMOTION ID)
    // -------------------------------------------------------------------------
    private val VERIFIED_DUAS: Map<String, List<VerifiedDuaRecord>> = mapOf(
        "angry" to listOf(
            VerifiedDuaRecord(
                id = "feel_angry_1",
                emotionId = "angry",
                title = "Refuge from Anger & Shaitan",
                arabic = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
                transliteration = "A'udhu billahi minash-shaitanir-rajim.",
                translation = "I seek refuge in Allah from Satan, the outcast.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "6115",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Sulaiman ibn Surad reported that two men quarreled in the presence of the Prophet ﷺ until one's face turned red with anger. The Prophet ﷺ said: 'I know a word which, if he said it, his anger would go away: A'udhu billahi minash-shaitanir-rajim.'",
                translationSource = "Sahih al-Bukhari Translation"
            )
        ),
        "rage" to listOf(
            VerifiedDuaRecord(
                id = "feel_rage_1",
                emotionId = "rage",
                title = "Calming Intense Anger & Wrath",
                arabic = "اللَّهُمَّ اغْفِرْ لِي ذَنْبِي ، وَأَذْهِبْ غَيْظَ قَلْبِي ، وَأَجِرْنِي مِنَ الشَّيْطَانِ",
                transliteration = "Allahum-maghfir li dhanbi, wa adh-hib ghaidha qalbi, wa ajirni minash-shaitan.",
                translation = "O Allah, forgive my sin, remove anger from my heart, and protect me from the Shaitan.",
                sourceCollection = "Amal al-Yawm wal-Laylah (Ibn al-Sunni)",
                referenceNumber = "460",
                authenticityGrade = "Hasan (Good)",
                gradingAuthority = "Al-Nawawi (Al-Adhkar)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Reported as a supplication taught to Umaymah bint Abi Sufyan to recite when anger or rage strikes the heart."
            )
        ),
        "irritated" to listOf(
            VerifiedDuaRecord(
                id = "feel_irritated_1",
                emotionId = "irritated",
                title = "Seeking Peace from Agitation",
                arabic = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
                transliteration = "A'udhu billahi minash-shaitanir-rajim.",
                translation = "I seek refuge in Allah from Satan, the outcast.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "6115",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Turning to Allah in remembrance removes agitation caused by satanic whispers or minor irritations."
            )
        ),
        "anxious" to listOf(
            VerifiedDuaRecord(
                id = "feel_anxious_1",
                emotionId = "anxious",
                title = "Relief from Anxiety & Distress",
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ",
                transliteration = "Allahumma inni 'audhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayni wa ghalabatir-rijal.",
                translation = "O Allah, I seek refuge in You from anxiety and sorrow, weakness and laziness, miserliness and cowardice, the burden of debt and being overpowered by men.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "6363",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Anas ibn Malik reported that the Messenger of Allah ﷺ frequently made this supplication to protect his heart from anxiety (hamm) regarding the future and grief (hazan) over the past."
            )
        ),
        "overwhelmed" to listOf(
            VerifiedDuaRecord(
                id = "feel_overwhelmed_1",
                emotionId = "overwhelmed",
                title = "Dua of Distress (Karb)",
                arabic = "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
                transliteration = "La ilaha illallahul-'Adhimul-Halim. La ilaha illallahu Rabbul-'Arshil-'Adhim. La ilaha illallahu Rabbus-samawati wa Rabbul-ardi wa Rabbul-'Arshil-Karim.",
                translation = "There is no god worthy of worship except Allah, the Supreme, the Forbearing. There is no god worthy of worship except Allah, Lord of the Magnificent Throne. There is no god worthy of worship except Allah, Lord of the heavens, Lord of the earth, and Lord of the Noble Throne.",
                sourceCollection = "Sahih al-Bukhari & Sahih Muslim",
                referenceNumber = "Bukhari 6346 / Muslim 2730",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari & Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Ibn Abbas reported that the Prophet ﷺ used to recite these words during times of severe hardship and feeling overwhelmed."
            )
        ),
        "nervous" to listOf(
            VerifiedDuaRecord(
                id = "feel_nervous_1",
                emotionId = "nervous",
                title = "Ease of Speech & Task Execution",
                arabic = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي يَفْقَهُوا قَوْلِي",
                transliteration = "Rabbish-rah li sadri wa yassir li amri wah-lul 'uqdatam-mil-lisani yafqahu qawli.",
                translation = "My Lord, expand for me my chest, and ease for me my task, and untie the knot from my tongue that they may understand my speech.",
                sourceCollection = "The Holy Qur'an",
                referenceNumber = "Surah Taha (20:25-28)",
                authenticityGrade = "Mutawatir (Qur'anic Verse)",
                gradingAuthority = "Divine Scripture",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The supplication of Prophet Musa (Moses) ﷺ when tasked with facing Pharaoh, asking Allah to calm his heart and grant him clarity in speech."
            )
        ),
        "suicidal" to listOf(
            VerifiedDuaRecord(
                id = "feel_suicidal_1",
                emotionId = "suicidal",
                title = "Supplication During Extreme Tribulation",
                arabic = "اللَّهُمَّ أَحْيِنِي مَا كَانَتِ الْحَيَاةُ خَيْرًا لِي، وَتَوَفَّنِي إِذَا كَانَتِ الْوَفَاةُ خَيْرًا لِي",
                transliteration = "Allahumma ahyini ma kanatil-hayatu khairan li, wa tawaffani idha kanatil-wafatu khairan li.",
                translation = "O Allah, grant me life as long as life is good for me, and cause me to die when death is better for me.",
                sourceCollection = "Sahih al-Bukhari & Sahih Muslim",
                referenceNumber = "Bukhari 5671 / Muslim 2680",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari & Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Prophet ﷺ prohibited wishing for death due to adversity. Instead, he taught those in deep agony to entrust their soul to Allah's infinite wisdom and mercy through this exact prayer.",
                isSuicidalEmergency = true
            )
        ),
        "depressed" to listOf(
            VerifiedDuaRecord(
                id = "feel_depressed_1",
                emotionId = "depressed",
                title = "Relief from Sorrow & Heavy Sadness",
                arabic = "اللَّهُمَّ إِنِّي عَبْدُكَ، وَابْنُ عَبْدِكَ، وَابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ سَمَّيْتَ بِهِ نَفْسَكَ، أَوْ أَنْزَلْتَهُ فِي كِتَابِكَ، أَوْ عَلَّمْتَهُ أَحَدًا مِنْ خَلْقِكَ، أَوْ اسْتَأْثَرْتَ بِهِ فِي عِلْمِ الْغَيْبِ عِنْدَكَ، أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجَلَاءَ حُزْنِي، وَذَهَابَ هَمِّي",
                transliteration = "Allahumma inni 'abduk, wabnu 'abdik, wabnu amatik, nasiyati bi-yadik, madin fiyya hukmuk, 'adlun fiyya qada'uk, as'aluka bi-kulli ismin huwa lak, sammayta bihi nafsak, aw anzaltahu fi kitabik, aw 'allamtahu ahadam-min khalqik, aw-ista'tharta bihi fi 'ilmil-ghaybi 'indak, an taj'alal-Qur'ana rabi'a qalbi, wa nura sadri, wa jala'a huzni, wa dhahaba hammi.",
                translation = "O Allah, I am Your servant, son of Your servant, son of Your female servant. My forelock is in Your hand. Your judgment upon me is assured and Your decree concerning me is just. I ask You by every Name that belongs to You, which You have named Yourself with, or revealed in Your Book, or taught to any of Your creation, or preserved in the knowledge of the unseen with You, that You make the Qur'an the spring of my heart, the light of my chest, the banisher of my grief, and the reliever of my anxiety.",
                sourceCollection = "Musnad Ahmad",
                referenceNumber = "3712",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Silsilah As-Sahihah 199)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Prophet ﷺ promised that Allah will remove sorrow and anxiety from the heart of anyone who recites this supplication and replace it with joy and relief."
            )
        ),
        "sad" to listOf(
            VerifiedDuaRecord(
                id = "feel_sad_1",
                emotionId = "sad",
                title = "Dua of Dhun-Nun (Yunus ﷺ) in Deep Sadness",
                arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
                transliteration = "La ilaha illa Anta Subhanaka inni kuntu minadh-dhalimin.",
                translation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
                sourceCollection = "Jami at-Tirmidhi",
                referenceNumber = "3505",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih at-Tirmidhi 3505)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Prophet ﷺ said: 'The supplication of Dhun-Nun (Yunus) when he called upon Allah inside the belly of the whale... No Muslim calls upon Allah with these words for anything, except that Allah answers him.'"
            )
        ),
        "hurt" to listOf(
            VerifiedDuaRecord(
                id = "feel_hurt_1",
                emotionId = "hurt",
                title = "Seeking Comfort & Compensation for Loss or Hurt",
                arabic = "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ، اللَّهُمَّ أْجُرْنِي فِي مُصِيبَتِي وَأَخْلِفْ لِي خَيْرًا مِنْهَا",
                transliteration = "Inna lillahi wa inna ilayhi raji'un. Allahum-majurni fi musibati wa akhlif li khairam-minha.",
                translation = "Indeed we belong to Allah and indeed to Him we will return. O Allah, reward me in my affliction and replace it for me with something better.",
                sourceCollection = "Sahih Muslim",
                referenceNumber = "918",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Umm Salamah reported that she heard the Prophet ﷺ say: 'No servant is afflicted with a calamity and says these words, except that Allah rewards him in his affliction and grants him better than what was lost.'"
            )
        ),
        "defeat" to listOf(
            VerifiedDuaRecord(
                id = "feel_defeated_1",
                emotionId = "defeated",
                title = "Sufficient is Allah for Us",
                arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                transliteration = "Hasbunallahu wa ni'mal-wakil.",
                translation = "Sufficient for us is Allah, and He is the best Disposer of affairs.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "4563",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Ibn Abbas said these words were spoken by Ibrahim (Abraham) ﷺ when he was thrown into the fire, and by Muhammad ﷺ when told that the enemy forces had gathered against him."
            )
        ),
        "grateful" to listOf(
            VerifiedDuaRecord(
                id = "feel_grateful_1",
                emotionId = "grateful",
                title = "Praise Upon Receiving Goodness",
                arabic = "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
                transliteration = "Alhamdulillahi alladhi bi-ni'matihi tatimmus-salihat.",
                translation = "All praise is due to Allah, by Whose grace good deeds are completed.",
                sourceCollection = "Sunan Ibn Majah",
                referenceNumber = "3803",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Ibn Majah 3071)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Aisha reported that whenever the Messenger of Allah ﷺ saw something that pleased him, he would recite these words of gratitude."
            )
        ),
        "happy" to listOf(
            VerifiedDuaRecord(
                id = "feel_happy_1",
                emotionId = "happy",
                title = "Expressing Joy & Gratitude to Allah",
                arabic = "الْحَمْدُ لِلَّهِ كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ",
                transliteration = "Alhamdulillahi kathiran tayyiban mubarakan fih.",
                translation = "Praise be to Allah, abundant, pure, and blessed praise.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "799",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "A companion recited this when rising from Ruku'. The Prophet ﷺ remarked that over thirty angels rushed to write down its immense reward."
            )
        ),
        "content" to listOf(
            VerifiedDuaRecord(
                id = "feel_content_1",
                emotionId = "content",
                title = "Contentment in Faith & Providence",
                arabic = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                transliteration = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
                translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
                sourceCollection = "Sunan Abi Dawud & Jami at-Tirmidhi",
                referenceNumber = "Abu Dawud 5072",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Abi Dawud 5072)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Prophet ﷺ guaranteed that whoever says this three times in the morning and evening, it is a right upon Allah to make them pleased on the Day of Judgment."
            )
        ),
        "peaceful" to listOf(
            VerifiedDuaRecord(
                id = "feel_peaceful_1",
                emotionId = "peaceful",
                title = "Greeting of Peace & Tranquility",
                arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ",
                transliteration = "Allahumma Antas-Salam wa minkas-salam, tabarakta dhal-jalali wal-ikram.",
                translation = "O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
                sourceCollection = "Sahih Muslim",
                referenceNumber = "592",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Thawban reported that when the Messenger of Allah ﷺ finished his prayer, he would ask for forgiveness three times and recite this supplication."
            )
        ),
        "guilty" to listOf(
            VerifiedDuaRecord(
                id = "feel_guilty_1",
                emotionId = "guilty",
                title = "Sayyidul Istighfar (Master Supplication for Forgiveness)",
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mas-tatat, a'udhu bika min sharri ma sana't, abu'u laka bi-ni'matika 'alayya, wa abu'u bi-dhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Ant.",
                translation = "O Allah, You are my Lord, there is no god worthy of worship except You. You created me and I am Your servant, and I abide by Your covenant and promise as best as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me, and I acknowledge my sin, so forgive me, for none forgives sins except You.",
                sourceCollection = "Sahih al-Bukhari",
                referenceNumber = "6306",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Shaddad ibn Aws reported that the Prophet ﷺ called this the 'Chief Supplication for Forgiveness', promising Paradise to whoever recites it with sincere conviction."
            )
        ),
        "regret" to listOf(
            VerifiedDuaRecord(
                id = "feel_regret_1",
                emotionId = "regret",
                title = "Prayer of Repentance & Divine Pardon",
                arabic = "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
                transliteration = "Rabbana dhalamna anfusana wa il-lam taghfir lana wa tarhamna lanakunanna minal-khasirin.",
                translation = "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers.",
                sourceCollection = "The Holy Qur'an",
                referenceNumber = "Surah Al-A'raf (7:23)",
                authenticityGrade = "Mutawatir (Qur'anic Verse)",
                gradingAuthority = "Divine Scripture",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The prayer of Adam and Hawwa (Eve) when seeking forgiveness after making a mistake, serving as the universal model for sincere repentance."
            )
        ),
        "confused" to listOf(
            VerifiedDuaRecord(
                id = "feel_confused_1",
                emotionId = "confused",
                title = "Supplication for Guidance & Right Decision",
                arabic = "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي",
                transliteration = "Allahum-mahdini wa saddidni.",
                translation = "O Allah, guide me and keep me on the straight and correct path.",
                sourceCollection = "Sahih Muslim",
                referenceNumber = "2725",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Ali ibn Abi Talib reported that the Prophet ﷺ instructed him to ask Allah specifically for guidance (hidayah) and steadfast correctness (saddad) when facing confusion."
            )
        ),
        "uncertain" to listOf(
            VerifiedDuaRecord(
                id = "feel_uncertain_1",
                emotionId = "uncertain",
                title = "Dua for Trust & Divine Sufficiency",
                arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                transliteration = "Hasbiyallahu la ilaha illa Huwa 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Adhim.",
                translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
                sourceCollection = "The Holy Qur'an & Sunan Abi Dawud",
                referenceNumber = "Surah At-Tawbah (9:129) / Abu Dawud 5081",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Abi Dawud 5081)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Abu ad-Darda reported that whoever recites this seven times in the morning and evening, Allah will suffice them against whatever worries or uncertainties trouble them."
            )
        ),
        "lost" to listOf(
            VerifiedDuaRecord(
                id = "feel_lost_1",
                emotionId = "lost",
                title = "Seeking Guidance to the Straight Path",
                arabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                transliteration = "Ihdinas-siratal-mustaqim.",
                translation = "Guide us to the straight path.",
                sourceCollection = "The Holy Qur'an",
                referenceNumber = "Surah Al-Fatiha (1:6)",
                authenticityGrade = "Mutawatir (Qur'anic Verse)",
                gradingAuthority = "Divine Scripture",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The central opening prayer of every Muslim recited in every unit of prayer, asking Allah for continuous direction whenever feeling lost or disoriented."
            )
        ),
        "doubtful" to listOf(
            VerifiedDuaRecord(
                id = "feel_doubtful_1",
                emotionId = "doubtful",
                title = "Refuge from Whispers of Doubt",
                arabic = "آمَنْتُ بِاللَّهِ وَرُسُلِهِ",
                transliteration = "Amantu billahi wa rusulih.",
                translation = "I believe in Allah and His messengers.",
                sourceCollection = "Sahih Muslim",
                referenceNumber = "134",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Abu Hurairah reported that the Messenger of Allah ﷺ instructed anyone who experiences stray thoughts of doubt to seek refuge in Allah and declare: 'Amantu billahi wa rusulih.'"
            )
        ),
        "lonely" to listOf(
            VerifiedDuaRecord(
                id = "feel_lonely_1",
                emotionId = "lonely",
                title = "Dua of Prophet Musa ﷺ in Solitude",
                arabic = "رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ",
                transliteration = "Rabbi inni lima anzalta ilayya min khairin faqir.",
                translation = "My Lord, indeed I am, for whatever good You would send down to me, in need.",
                sourceCollection = "The Holy Qur'an",
                referenceNumber = "Surah Al-Qasas (28:24)",
                authenticityGrade = "Mutawatir (Qur'anic Verse)",
                gradingAuthority = "Divine Scripture",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Recited by Prophet Musa (Moses) ﷺ when he was alone, exiled, and without family or support in Madyan. Allah responded by granting him shelter, family, and prophethood."
            )
        ),
        "unloved" to listOf(
            VerifiedDuaRecord(
                id = "feel_unloved_1",
                emotionId = "unloved",
                title = "Asking Allah for His Divine Love",
                arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ حُبَّكَ، وَحُبَّ مَنْ يُحِبُّكَ، وَالْعَمَلَ الَّذِي يُبَلِّغُنِي حُبَّكَ",
                transliteration = "Allahumma inni as'aluka hubbak, wa hubba man yuhibbuk, wal-'amalalladhi yuballighuni hubbak.",
                translation = "O Allah, I ask You for Your love, and the love of those who love You, and the deeds that will lead me to Your love.",
                sourceCollection = "Jami at-Tirmidhi",
                referenceNumber = "3490",
                authenticityGrade = "Hasan-Sahih (Good & Authentic)",
                gradingAuthority = "At-Tirmidhi & Al-Albani",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Messenger of Allah ﷺ said that Prophet Dawud (David) ﷺ used to invoke Allah with these beautiful words to seek the ultimate love that fulfills the soul."
            )
        ),
        "jealous" to listOf(
            VerifiedDuaRecord(
                id = "feel_jealous_1",
                emotionId = "jealous",
                title = "Cleansing the Heart from Envy & Resentment",
                arabic = "رَبَّنَا اغْفِرْ لَنَا وَلإِخْوَانِنَا الَّذِينَ سَبَقُونَا بِالإِيمَانِ وَلا تَجْعَلْ فِي قُلُوبِنَا غِلا لِّلَّذِينَ آمَنُوا رَبَّنَا إِنَّكَ رَؤُوفٌ رَّحِيمٌ",
                transliteration = "Rabbanagh-fir lana wa li-ikhwaninalladhina sabaquna bil-imani wa la taj'al fi qulubina ghillal-lilladhina amanu Rabbana innaka Ra'ufur-Rahim.",
                translation = "Our Lord, forgive us and our brothers who preceded us in faith, and put not in our hearts any resentment toward those who have believed. Our Lord, indeed You are Kind and Merciful.",
                sourceCollection = "The Holy Qur'an",
                referenceNumber = "Surah Al-Hashr (59:10)",
                authenticityGrade = "Mutawatir (Qur'anic Verse)",
                gradingAuthority = "Divine Scripture",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Qur'anic prayer praising believers who purify their hearts from envy, jealousy, or ill-will towards others."
            )
        ),
        "envious" to listOf(
            VerifiedDuaRecord(
                id = "feel_envious_1",
                emotionId = "envious",
                title = "Contentment with Allah's Blessings",
                arabic = "اللَّهُمَّ قَنِّعْنِي بِمَا رَزَقْتَنِي ، وَبَارِكْ لِي فِيهِ ، وَاخْلُفْ عَلَى كُلِّ غَائِبَةٍ لِي بِخَيْرٍ",
                transliteration = "Allahumma qanni'ni bima razaqtani, wa barik li fih, wakhluf 'ala kulli gha'ibatil-li bi-khair.",
                translation = "O Allah, make me content with what You have provided me, bless it for me, and replace anything I have missed with something better.",
                sourceCollection = "Al-Adab Al-Mufrad (Al-Bukhari)",
                referenceNumber = "681",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Al-Adab Al-Mufrad 525)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Ibn Abbas reported that the Prophet ﷺ taught this supplication to foster genuine gratitude and eliminate envy for what others possess."
            )
        ),
        "lazy" to listOf(
            VerifiedDuaRecord(
                id = "feel_lazy_1",
                emotionId = "lazy",
                title = "Protection Against Laziness & Incapacity",
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْهَرَمِ، وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، وَأَعُوذُ بِكَ مِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ",
                transliteration = "Allahumma inni 'audhu bika minal-'ajzi wal-kasal, wal-jubni wal-haram, wal-bukhl, wa 'audhu bika min 'adhabil-qabr, wa 'audhu bika min fitnatil-mahya wal-mamat.",
                translation = "O Allah, I seek refuge in You from incapacity, laziness, cowardice, senility, and miserliness. And I seek refuge in You from the punishment of the grave, and from the trials of life and death.",
                sourceCollection = "Sahih al-Bukhari & Sahih Muslim",
                referenceNumber = "Bukhari 6369 / Muslim 2706",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari & Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Anas ibn Malik reported that the Prophet ﷺ constantly invoked Allah with this prayer to stay energized, disciplined, and productive."
            )
        ),
        "weak" to listOf(
            VerifiedDuaRecord(
                id = "feel_weak_1",
                emotionId = "weak",
                title = "Seeking Strength & Mercy",
                arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                transliteration = "Ya Hayyu Ya Qayyum, bi-rahmatika astagheeth, aslih li sha'ni kullah, wa la takilni ila nafsi tarfata 'ayn.",
                translation = "O Ever-Living, O Self-Sustaining Sustainer, by Your mercy I seek assistance. Rectify all my affairs for me, and do not leave me to myself even for the blink of an eye.",
                sourceCollection = "Sunan an-Nasa'i & Al-Hakim",
                referenceNumber = "Al-Sunan al-Kubra 10405",
                authenticityGrade = "Hasan (Good)",
                gradingAuthority = "Al-Albani (Sahih al-Targhib 661)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "The Prophet ﷺ taught his daughter Fatimah (may Allah be pleased with her) to recite this prayer every morning and evening when feeling vulnerable or lacking strength."
            )
        ),
        "scared" to listOf(
            VerifiedDuaRecord(
                id = "feel_scared_1",
                emotionId = "scared",
                title = "Supplication During Fear & Alarm",
                arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ غَضَبِهِ وَعِقَابِهِ وَشَرِّ عِبَادِهِ، وَمِنْ هَمَزَاتِ الشَّيَاطِينِ وَأَنْ يَحْضُرُونِ",
                transliteration = "A'udhu bi-kalimatillahit-tammati min ghadabihi wa 'iqabihi wa sharri 'ibadih, wa min hamazatish-shayatini wa an yahdurun.",
                translation = "I seek refuge in the perfect words of Allah from His anger, His punishment, the evil of His servants, and from the promptings of devils and their presence near me.",
                sourceCollection = "Jami at-Tirmidhi & Sunan Abi Dawud",
                referenceNumber = "Abu Dawud 3893 / Tirmidhi 3528",
                authenticityGrade = "Hasan (Good)",
                gradingAuthority = "Al-Albani (Sahih Abi Dawud 3893)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Amr ibn Shu'ayb reported that the Messenger of Allah ﷺ taught companions to recite these protective words when frightened or startled during night or day."
            )
        ),
        "tired" to listOf(
            VerifiedDuaRecord(
                id = "feel_tired_1",
                emotionId = "tired",
                title = "Tasbeeh Fatimah (Spiritual Strength Before Rest)",
                arabic = "سُبْحَانَ اللَّهِ (٣٣x) ، الْحَمْدُ لِلَّهِ (٣٣x) ، اللَّهُ أَكْبَرُ (٣٤x)",
                transliteration = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (34x).",
                translation = "Glory be to Allah (33 times), Praise be to Allah (33 times), Allah is the Greatest (34 times).",
                sourceCollection = "Sahih al-Bukhari & Sahih Muslim",
                referenceNumber = "Bukhari 3113 / Muslim 2727",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam al-Bukhari & Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "When Fatimah (may Allah be pleased with her) approached the Prophet ﷺ exhausted from manual labor and asking for help, he gave her and Ali this remembrance, saying it is better than a servant."
            )
        ),
        "aroused" to listOf(
            VerifiedDuaRecord(
                id = "feel_aroused_1",
                emotionId = "aroused",
                title = "Protection Against Temptation & Desires",
                arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ سَمْعِي، وَمِنْ شَرِّ بَصَرِي، وَمِنْ شَرِّ لِسَانِي، وَمِنْ شَرِّ قَلْبِي، وَمِنْ شَرِّ مَنِيِّي",
                transliteration = "Allahumma inni 'audhu bika min sharri sam'i, wa min sharri basari, wa min sharri lisani, wa min sharri qalbi, wa min sharri maniyyi.",
                translation = "O Allah, I seek refuge in You from the evil of my hearing, from the evil of my sight, from the evil of my tongue, from the evil of my heart, and from the evil of my desires.",
                sourceCollection = "Sunan Abi Dawud & Jami at-Tirmidhi",
                referenceNumber = "Abu Dawud 1551 / Tirmidhi 3492",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Abi Dawud 1551)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Shutayr ibn Shakhal reported that his father asked the Prophet ﷺ to teach him a protective prayer, and he taught him this comprehensive refuge from physical and sensual temptations."
            )
        ),
        "desire" to listOf(
            VerifiedDuaRecord(
                id = "feel_desire_1",
                emotionId = "desire",
                title = "Asking for Chaste & Virtuous Living",
                arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
                transliteration = "Allahumma inni as'alukal-huda wat-tuqa wal-'afafa wal-ghina.",
                translation = "O Allah, I ask You for guidance, piety, chastity, and self-sufficiency.",
                sourceCollection = "Sahih Muslim",
                referenceNumber = "2721",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Imam Muslim",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Abdullah ibn Mas'ud reported that the Messenger of Allah ﷺ frequently made this concise supplication for spiritual purity and self-sufficiency."
            )
        ),
        "love" to listOf(
            VerifiedDuaRecord(
                id = "feel_love_1",
                emotionId = "love",
                title = "Blessing Love & Harmony in Relationships",
                arabic = "اللَّهُمَّ أَلِّفْ بَيْنَ قُلُوبِنَا وَأَصْلِحْ ذَاتَ بَيْنِنَا وَاهْدِنَا سُبُلَ السَّلاَمِ",
                transliteration = "Allahumma allif bayna qulubina wa aslih dhata baynina wahdina subulas-salam.",
                translation = "O Allah, join our hearts together, reconcile our differences, and guide us on the paths of peace.",
                sourceCollection = "Sunan Abi Dawud & Al-Hakim",
                referenceNumber = "Abu Dawud 969",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih Abi Dawud 969)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Ibn Mas'ud reported that the Prophet ﷺ taught this supplication to foster affection, reconciliation, and peace among family, spouses, and community."
            )
        ),
        "confident" to listOf(
            VerifiedDuaRecord(
                id = "feel_confident_1",
                emotionId = "confident",
                title = "Humility & Reliance Upon Allah",
                arabic = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
                transliteration = "Allahumma la sahla illa ma ja'altahu sahla, wa Anta taj'alul-hazna idha shi'ta sahla.",
                translation = "O Allah, there is no ease except in that which You have made easy, and You make hardship easy if You wish.",
                sourceCollection = "Sahih Ibn Hibban",
                referenceNumber = "974",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Silsilah As-Sahihah 2886)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Reminds us that true confidence comes from trusting Allah's empowerment, acknowledging that all success is facilitated by Him."
            )
        ),
        "determined" to listOf(
            VerifiedDuaRecord(
                id = "feel_determined_1",
                emotionId = "determined",
                title = "Steadfastness in Faith & Resolve",
                arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
                transliteration = "Ya Muqallibal-qulubi thabbit qalbi 'ala dinik.",
                translation = "O Turner of the hearts, make my heart firm upon Your religion.",
                sourceCollection = "Jami at-Tirmidhi",
                referenceNumber = "3522",
                authenticityGrade = "Sahih (Authentic)",
                gradingAuthority = "Al-Albani (Sahih at-Tirmidhi 3522)",
                relevanceType = RelevanceType.SPECIFICALLY_REPORTED,
                contextExplanation = "Anas reported that this was the most frequent supplication of the Prophet ﷺ to maintain focus, determination, and spiritual consistency."
            )
        )
    )

    /**
     * Retrieves all verified duas for a given emotion ID.
     */
    fun getDuasForEmotion(emotionId: String): List<VerifiedDuaRecord> {
        return VERIFIED_DUAS[emotionId.lowercase()] ?: emptyList()
    }

    /**
     * Checks whether an emotion has verified supplications.
     */
    fun hasVerifiedDua(emotionId: String): Boolean {
        return VERIFIED_DUAS.containsKey(emotionId.lowercase())
    }
}
