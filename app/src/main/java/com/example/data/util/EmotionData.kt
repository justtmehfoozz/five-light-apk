package com.example.data.util

import androidx.compose.ui.graphics.Color
import com.example.data.model.EmotionCategoryGroup
import com.example.data.model.EmotionItem

object EmotionData {

    val ALL_EMOTIONS: List<EmotionItem> by lazy {
        listOf(
            // 1. Angry
            EmotionItem(
                id = "angry",
                name = "Angry",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFEF4444),
                colorDark = Color(0xFFDC2626),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_angry",
                    title = "Supplication When Feeling Angry",
                    arabic = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
                    transliteration = "A'udhu billahi minash-shaitanir-rajim.",
                    translation = "I seek refuge in Allah from Satan, the outcast.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 6115",
                    authenticityGrade = "Authentic (Sahih)",
                    benefitOrNotes = "The Prophet ﷺ said when two men argued in anger: 'I know a word which if he said it, his anger would leave him.'"
                )
            ),
            // 2. Anxious
            EmotionItem(
                id = "anxious",
                name = "Anxious",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFF97316),
                colorDark = Color(0xFFEA580C),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_anxious",
                    title = "Dua for Relief from Anxiety & Sorrow",
                    arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ وَالْعَجْزِ وَالْكَسَلِ وَالْبُخْلِ وَالْجُبْنِ وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
                    transliteration = "Allahumma inni a'udhu bika minal-hammi wal-hazani, wal-'ajzi wal-kasali, wal-bukhli wal-jubni, wa dala'id-daini wa ghalabatir-rijal.",
                    translation = "O Allah, I seek refuge in You from anxiety and grief, incapacity and laziness, miserliness and cowardice, the burden of debt and being overpowered by men.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 6369",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 3. Bored
            EmotionItem(
                id = "bored",
                name = "Bored",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF8B5CF6),
                colorDark = Color(0xFF7C3AED),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_bored",
                    title = "Seeking Beneficial Knowledge & Purpose",
                    arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلًا مُتَقَبَّلًا",
                    transliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbala.",
                    translation = "O Allah, I ask You for beneficial knowledge, a wholesome provision, and deeds that are accepted.",
                    category = "Emotion",
                    reference = "Sunan Ibn Majah 925",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 4. Confident
            EmotionItem(
                id = "confident",
                name = "Confident",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF10B981),
                colorDark = Color(0xFF059669),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_confident",
                    title = "Supplication for Trust in Allah's Sufficiency",
                    arabic = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                    transliteration = "Hasbiyallahu la ilaha illa Huwa 'alaihi tawakkaltu wa Huwa Rabbul-'Arshil-'Adhim.",
                    translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
                    category = "Emotion",
                    reference = "Surah At-Tawbah (9:129)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 5. Confused
            EmotionItem(
                id = "confused",
                name = "Confused",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF06B6D4),
                colorDark = Color(0xFF0891B2),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_confused",
                    title = "Dua for Guidance and Clarity",
                    arabic = "رَبَّنَا آتِنَا مِنْ لَدُنْكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
                    transliteration = "Rabbana atina min ladunka rahmatan wa hayyi' lana min amrina rashada.",
                    translation = "Our Lord, grant us from Yourself mercy and prepare for us from our affair right guidance.",
                    category = "Emotion",
                    reference = "Surah Al-Kahf (18:10)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 6. Content
            EmotionItem(
                id = "content",
                name = "Content",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF14B8A6),
                colorDark = Color(0xFF0D9488),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_content",
                    title = "Dua for Contentment with Faith",
                    arabic = "رَضِيتُ بِاللَّهِ رَبًّا وَبِالْإِسْلَامِ دِينًا وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                    transliteration = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin sallallahu 'alaihi wa sallama Nabiyya.",
                    translation = "I am pleased with Allah as my Lord, Islam as my religion, and Muhammad ﷺ as my Prophet.",
                    category = "Emotion",
                    reference = "Sunan Abi Dawud 5072",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 7. Depressed
            EmotionItem(
                id = "depressed",
                name = "Depressed",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF6366F1),
                colorDark = Color(0xFF4F46E5),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_depressed",
                    title = "Dua in Times of Severe Distress & Sadness",
                    arabic = "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ وَأَصْلِحْ لِي شَأْنِي كُلَّهُ لَا إِلَهَ إِلَّا أَنْتَ",
                    transliteration = "Allahumma rahmataka arju fala takilni ila nafsi tarfata 'ainin, wa aslih li sha'ni kullahu, la ilaha illa Ant.",
                    translation = "O Allah, it is Your mercy that I hope for, so do not leave me to myself even for the blink of an eye, and set right all my affairs. There is no god but You.",
                    category = "Emotion",
                    reference = "Sunan Abi Dawud 5090",
                    authenticityGrade = "Authentic (Hasan/Sahih)"
                )
            ),
            // 8. Doubtful
            EmotionItem(
                id = "doubtful",
                name = "Doubtful",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF84CC16),
                colorDark = Color(0xFF65A30D),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_doubtful",
                    title = "Dua to Firm the Heart upon Faith",
                    arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
                    transliteration = "Ya Muqallibal-qulubi thabbit qalbi 'ala dinik.",
                    translation = "O Turner of the hearts, make my heart firm upon Your religion.",
                    category = "Emotion",
                    reference = "Jami' at-Tirmidhi 2140",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 9. Grateful
            EmotionItem(
                id = "grateful",
                name = "Grateful",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF22C55E),
                colorDark = Color(0xFF16A34A),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_grateful",
                    title = "Prayer of Gratitude for Blessings",
                    arabic = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَى وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ",
                    transliteration = "Rabbi awzi'ni an ashkura ni'matakal-lati an'amta 'alayya wa 'ala walidayya wa an a'mala salihan tardah.",
                    translation = "My Lord, enable me to be grateful for Your favor which You have bestowed upon me and upon my parents and to work righteousness of which You will approve.",
                    category = "Emotion",
                    reference = "Surah An-Naml (27:19)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 10. Greedy
            EmotionItem(
                id = "greedy",
                name = "Greedy",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFEC4899),
                colorDark = Color(0xFFDB2777),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_greedy",
                    title = "Dua for Sufficient Provision & Purified Desires",
                    arabic = "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
                    transliteration = "Allahummak-fini bi-halalika 'an haramika wa aghnini bi-fadlika 'amman siwak.",
                    translation = "O Allah, suffice me with Your lawful provisions against Your forbidden ones, and enrich me with Your bounty above all others.",
                    category = "Emotion",
                    reference = "Jami' at-Tirmidhi 3563",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 11. Guilty
            EmotionItem(
                id = "guilty",
                name = "Guilty",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFA855F7),
                colorDark = Color(0xFF9333EA),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_guilty",
                    title = "Master Supplication for Repentance (Sayyidul Istighfar)",
                    arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفرُ الذُّنُوبَ إِلَّا أَنْتَ",
                    transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Ant.",
                    translation = "O Allah, You are my Lord; there is no god except You. You created me, and I am Your servant, and I am faithful to my covenant and promise to You as much as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 6306",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 12. Happy
            EmotionItem(
                id = "happy",
                name = "Happy",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFFEAB308),
                colorDark = Color(0xFFCA8A04),
                isDarkText = true,
                duaItem = DuaItem(
                    id = "emo_happy",
                    title = "Praise to Allah for Joy & Wholesome Blessings",
                    arabic = "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
                    transliteration = "Al-hamdu lillahil-ladhi bini'matihi tatimmus-salihat.",
                    translation = "All praise is due to Allah by Whose favor good things are accomplished.",
                    category = "Emotion",
                    reference = "Sunan Ibn Majah 3803",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 13. Hurt
            EmotionItem(
                id = "hurt",
                name = "Hurt",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFF43F5E),
                colorDark = Color(0xFFE11D48),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_hurt",
                    title = "Dua When Experiencing Physical or Emotional Hurt",
                    arabic = "أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ",
                    transliteration = "A'udhu billahi wa qudratihi min sharri ma ajidu wa uhadhir.",
                    translation = "I seek refuge in Allah and His Power from the evil of what I feel and fear.",
                    category = "Emotion",
                    reference = "Sahih Muslim 2202",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 14. Hypocritical
            EmotionItem(
                id = "hypocritical",
                name = "Hypocritical",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF78716C),
                colorDark = Color(0xFF57534E),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_hypocritical",
                    title = "Supplication for Sincerity & Protection from Shirk",
                    arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ أَنْ أُشْرِكَ بِكَ وَأَنَا أَعْلَمُ وَأَسْتَغْفِرُكَ لِمَا لَا أَعْلَمُ",
                    transliteration = "Allahumma inni a'udhu bika an ushrika bika wa ana a'lamu, wa astaghfiruka lima la a'lam.",
                    translation = "O Allah, I seek refuge in You from knowingly associating anything with You, and I ask Your forgiveness for that which I do not know.",
                    category = "Emotion",
                    reference = "Al-Adab al-Mufrad 716",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 15. Indecisive
            EmotionItem(
                id = "indecisive",
                name = "Indecisive",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF0284C7),
                colorDark = Color(0xFF0369A1),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_indecisive",
                    title = "Supplication for Seeking Divine Counsel (Istikharah)",
                    arabic = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ",
                    transliteration = "Allahumma inni astakhiruka bi'ilmika, wa astaqdiruka biqudratika, wa as'aluka min fadlikal-'adhim.",
                    translation = "O Allah, I seek Your counsel through Your knowledge, and I seek strength through Your power, and I ask You from Your immense favor.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 1162",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 16. Jealous
            EmotionItem(
                id = "jealous",
                name = "Jealous",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFD97706),
                colorDark = Color(0xFFB45309),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_jealous",
                    title = "Dua to Cleanse the Heart from Resentment",
                    arabic = "رَبَّنَا اغْفِرْ لَنَا وَلإِخْوَانِنَا الَّذِينَ سَبَقُونَا بِالإِيمَانِ وَلا تَجْعَلْ فِي قُلُوبِنَا غِلاًّ لِلَّذِينَ آمَنُوا رَبَّنَا إِنَّكَ رَؤُوفٌ رَحِيمٌ",
                    transliteration = "Rabbanagh-fir lana wa li-ikhwaninal-ladhina sabaquna bil-imani wa la taj'al fi qulubina ghillan lilladhina amanu Rabbana innaka Ra'ufur-Rahim.",
                    translation = "Our Lord, forgive us and our brothers who preceded us in faith, and put not in our hearts any resentment toward those who have believed. Our Lord, indeed You are Kind and Merciful.",
                    category = "Emotion",
                    reference = "Surah Al-Hashr (59:10)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 17. Lazy
            EmotionItem(
                id = "lazy",
                name = "Lazy",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF64748B),
                colorDark = Color(0xFF475569),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_lazy",
                    title = "Dua for Energy, Vigor & Protection from Sloth",
                    arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ وَالْجُبْنِ وَالْهَرَمِ وَالْبُخْلِ",
                    transliteration = "Allahumma inni a'udhu bika minal-'ajzi wal-kasali wal-jubni wal-harami wal-bukhl.",
                    translation = "O Allah, I seek refuge in You from helplessness, laziness, cowardice, senility, and miserliness.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 6371",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 18. Lonely
            EmotionItem(
                id = "lonely",
                name = "Lonely",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF3B82F6),
                colorDark = Color(0xFF2563EB),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_lonely",
                    title = "Supplication for Company & Solace in Allah",
                    arabic = "رَبِّ لا تَذَرْنِي فَرْدًا وَأَنْتَ خَيْرُ الْوَارِثِينَ",
                    transliteration = "Rabbi la tadharni fardan wa Anta khairul-warithin.",
                    translation = "My Lord, do not leave me alone, and You are the best of inheritors.",
                    category = "Emotion",
                    reference = "Surah Al-Anbiya (21:89)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 19. Lost
            EmotionItem(
                id = "lost",
                name = "Lost",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF0D9488),
                colorDark = Color(0xFF0F766E),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_lost",
                    title = "Dua for Divine Guidance and Light",
                    arabic = "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا وَفِي بَصَرِي نُورًا وَفِي سَمْعِي نُورًا",
                    transliteration = "Allahummaj-'al fi qalbi nura, wa fi basari nura, wa fi sam'i nura.",
                    translation = "O Allah, place light in my heart, light in my sight, and light in my hearing.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 6316",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 20. Nervous
            EmotionItem(
                id = "nervous",
                name = "Nervous",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFFF59E0B),
                colorDark = Color(0xFFD97706),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_nervous",
                    title = "Prophet Musa's Prayer for Speech & Calmness",
                    arabic = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِنْ لِسَانِي يَفْقَهُوا قَوْلِي",
                    transliteration = "Rabbish-rah li sadri wa yassir li amri wahlul 'uqdatam-min lisani yafqahu qawli.",
                    translation = "My Lord, expand for me my chest, ease for me my task, and untie the knot from my tongue that they may understand my speech.",
                    category = "Emotion",
                    reference = "Surah Taha (20:25-28)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 21. Overwhelmed
            EmotionItem(
                id = "overwhelmed",
                name = "Overwhelmed",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF7C3AED),
                colorDark = Color(0xFF6D28D9),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_overwhelmed",
                    title = "Dua for Ease in Difficult Matters",
                    arabic = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
                    transliteration = "Allahumma la sahla illa ma ja'altahu sahla, wa Anta taj'alul-hazna idha shi'ta sahla.",
                    translation = "O Allah, there is no ease except in that which You have made easy, and You make difficulty easy if You wish.",
                    category = "Emotion",
                    reference = "Sahih Ibn Hibban 974",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 22. Regret
            EmotionItem(
                id = "regret",
                name = "Regret",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF475569),
                colorDark = Color(0xFF334155),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_regret",
                    title = "Dua of Prophet Adam for Forgiveness & Compassion",
                    arabic = "رَبَّنَا ظَلَمْنَا أَنْفُسَنَا وَإِنْ لَمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
                    transliteration = "Rabbana dhalamna anfusana wa il-lam taghfir lana wa tarhamna lanakunanna minal-khasirin.",
                    translation = "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy upon us, we will surely be among the losers.",
                    category = "Emotion",
                    reference = "Surah Al-A'raf (7:23)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 23. Sad
            EmotionItem(
                id = "sad",
                name = "Sad",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF2563EB),
                colorDark = Color(0xFF1D4ED8),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_sad",
                    title = "Supplication of Prophet Yunus (In Distress)",
                    arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                    transliteration = "La ilaha illa Anta subhanaka inni kuntu minadh-dhalimin.",
                    translation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
                    category = "Emotion",
                    reference = "Surah Al-Anbiya (21:87)",
                    authenticityGrade = "Qur'anic Verse",
                    benefitOrNotes = "The Prophet ﷺ said: 'No Muslim supplicates with this for anything except that Allah answers him.'"
                )
            ),
            // 24. Scared
            EmotionItem(
                id = "scared",
                name = "Scared",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFE11D48),
                colorDark = Color(0xFFBE123C),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_scared",
                    title = "Supplication for Refuge from Fear",
                    arabic = "اللَّهُمَّ إِنَّا نَجْعَلُكَ فِي نُحُورِهِمْ وَنَعُوذُ بِكَ مِنْ شُرُورِهِمْ",
                    transliteration = "Allahumma inna naj'aluka fi nuhurihim wa na'udhu bika min shururihim.",
                    translation = "O Allah, we place You before them and we seek refuge in You from their evil.",
                    category = "Emotion",
                    reference = "Sunan Abi Dawud 1537",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 25. Suicidal
            EmotionItem(
                id = "suicidal",
                name = "Suicidal",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF991B1B),
                colorDark = Color(0xFF7F1D1D),
                isDarkText = false,
                isSensitive = true,
                duaItem = DuaItem(
                    id = "emo_suicidal",
                    title = "Prophetic Supplication in Times of Deep Suffering",
                    arabic = "اللَّهُمَّ أَحْيِنِي مَا كَانَتِ الْحَيَاةُ خَيْرًا لِي وَتَوَفَّنِي إِذَا كَانَتِ الْوَفَاةُ خَيْرًا لِي",
                    transliteration = "Allahumma ahyini ma kanatil-hayatu khairan li, wa tawaffani idha kanatil-wafatu khairan li.",
                    translation = "O Allah, grant me life as long as life is good for me, and cause me to die when death is better for me.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 5671",
                    authenticityGrade = "Authentic (Sahih)",
                    benefitOrNotes = "You are deeply precious in the sight of Allah. Please reach out to compassionate crisis support professionals."
                )
            ),
            // 26. Tired
            EmotionItem(
                id = "tired",
                name = "Tired",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF6B7280),
                colorDark = Color(0xFF4B5563),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_tired",
                    title = "Tasbeeh of Fatima for Physical Fatigue & Strength",
                    arabic = "سُبْحَانَ اللَّهِ (٣٣) ، الْحَمْدُ لِلَّهِ (٣٣) ، اللَّهُ أَكْبَرُ (٣٤)",
                    transliteration = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (34x).",
                    translation = "Glory be to Allah, Praise be to Allah, Allah is the Greatest.",
                    category = "Emotion",
                    reference = "Sahih al-Bukhari 3113",
                    authenticityGrade = "Authentic (Sahih)",
                    benefitOrNotes = "The Prophet ﷺ prescribed this to Lady Fatima when she felt exhausted from labor, saying it is better than a servant."
                )
            ),
            // 27. Unloved
            EmotionItem(
                id = "unloved",
                name = "Unloved",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFE02424),
                colorDark = Color(0xFFC81E1E),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_unloved",
                    title = "Supplication for Allah's Divine Love",
                    arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ حُبَّكَ وَحُبَّ مَنْ يُحِبُّكَ وَالْعَمَلَ الَّذِي يُبَلِّغُنِي حُبَّكَ",
                    transliteration = "Allahumma inni as'aluka hubbaka wa hubba man yuhibbuka wal-'amalal-ladhi yuballighuni hubbak.",
                    translation = "O Allah, I ask You for Your love, the love of those who love You, and deeds that will bring me Your love.",
                    category = "Emotion",
                    reference = "Jami' at-Tirmidhi 3490",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 28. Weak
            EmotionItem(
                id = "weak",
                name = "Weak",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF4F46E5),
                colorDark = Color(0xFF4338CA),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_weak",
                    title = "Dua for Strength & Protection from Weakness",
                    arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                    transliteration = "Ya Hayyu Ya Qayyumu bi-rahmatika astaghith, aslih li sha'ni kullahu wa la takilni ila nafsi tarfata 'ain.",
                    translation = "O Ever-Living, O Self-Sustaining, by Your mercy I seek help. Rectify all my affairs and do not leave me to myself for even a moment.",
                    category = "Emotion",
                    reference = "Sunan an-Nasa'i 10405",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 29. Anticipation
            EmotionItem(
                id = "anticipation",
                name = "Anticipation",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF0284C7),
                colorDark = Color(0xFF0369A1),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_anticipation",
                    title = "Dua for Good Outcomes in All Matters",
                    arabic = "اللَّهُمَّ أَحْسِنْ عَاقِبَتَنَا فِي الْأُمُورِ كُلِّهَا وَأَجِرْنَا مِنْ خِزْيِ الدُّنْيَا وَعَذَابِ الْآخِرَةِ",
                    transliteration = "Allahumma ahsin 'aqibatana fil-umuri kulliha wa ajirna min khizyid-dunya wa 'adhabil-akhirah.",
                    translation = "O Allah, grant us a good outcome in all of our affairs, and save us from the disgrace of this world and the punishment of the Hereafter.",
                    category = "Emotion",
                    reference = "Musnad Ahmad 17628",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 30. Aroused
            EmotionItem(
                id = "aroused",
                name = "Aroused",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFFC026D3),
                colorDark = Color(0xFFA21CAF),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_aroused",
                    title = "Dua for Chastity & Purity of Desires",
                    arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
                    transliteration = "Allahumma inni as'alukal-huda wat-tuqa wal-'afafa wal-ghina.",
                    translation = "O Allah, I ask You for guidance, piety, chastity, and self-sufficiency.",
                    category = "Emotion",
                    reference = "Sahih Muslim 2721",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 31. Curious
            EmotionItem(
                id = "curious",
                name = "Curious",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF0EA5E9),
                colorDark = Color(0xFF0284C7),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_curious",
                    title = "Prayer for Increase in Wisdom & Knowledge",
                    arabic = "رَبِّ زِدْنِي عِلْمًا",
                    transliteration = "Rabbi zidni 'ilma.",
                    translation = "My Lord, increase me in knowledge.",
                    category = "Emotion",
                    reference = "Surah Taha (20:114)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 32. Defeated
            EmotionItem(
                id = "defeated",
                name = "Defeated",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF475569),
                colorDark = Color(0xFF334155),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_defeated",
                    title = "Prayer of Prophet Nuh When Feeling Overpowered",
                    arabic = "أَنِّي مَغْلُوبٌ فَانْتَصِرْ",
                    transliteration = "Anni maghtubun fantasir.",
                    translation = "Indeed, I am overpowered, so help me.",
                    category = "Emotion",
                    reference = "Surah Al-Qamar (54:10)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 33. Desire
            EmotionItem(
                id = "desire",
                name = "Desire",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFDB2777),
                colorDark = Color(0xFFBE185D),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_desire",
                    title = "Dua for Pure Intentions & Halal Fulfillment",
                    arabic = "اللَّهُمَّ اهْدِنِي لِأَحْسَنِ الْأَخْلَاقِ لَا يَهْدِي لِأَحْسَنِهَا إِلَّا أَنْتَ",
                    transliteration = "Allahummah-dini li-ahsanil-akhlaqi la yahdi li-ahsaniha illa Ant.",
                    translation = "O Allah, guide me to the best of manners; none can guide to the best of them except You.",
                    category = "Emotion",
                    reference = "Sahih Muslim 771",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 34. Desperate
            EmotionItem(
                id = "desperate",
                name = "Desperate",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFB91C1C),
                colorDark = Color(0xFF991B1B),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_desperate",
                    title = "Supplication of Prophet Musa in Total Need",
                    arabic = "رَبِّ إِنِّي لِمَا أَنْزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ",
                    transliteration = "Rabbi inni lima anzalta ilayya min khairin faqir.",
                    translation = "My Lord, indeed I am, for whatever good You would send down to me, in desperate need.",
                    category = "Emotion",
                    reference = "Surah Al-Qasas (28:24)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 35. Determined
            EmotionItem(
                id = "determined",
                name = "Determined",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF15803D),
                colorDark = Color(0xFF166534),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_determined",
                    title = "Dua for Steadfastness in Purpose",
                    arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الثَّبَاتَ فِي الْأَمْرِ وَالْعَزِيمَةَ عَلَى الرُّشْدِ",
                    transliteration = "Allahumma inni as'alukat-thabata fil-amri wal-'azimata 'alar-rushd.",
                    translation = "O Allah, I ask You for steadfastness in all affairs, and determination upon guidance.",
                    category = "Emotion",
                    reference = "Sunan an-Nasa'i 1304",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 36. Disbelief
            EmotionItem(
                id = "disbelief",
                name = "Disbelief",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF6B7280),
                colorDark = Color(0xFF374151),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_disbelief",
                    title = "Dua for Firm Belief & Truth",
                    arabic = "آمَنْتُ بِاللَّهِ وَرُسُلِهِ",
                    transliteration = "Amantu billahi wa rusulih.",
                    translation = "I believe in Allah and His Messengers.",
                    category = "Emotion",
                    reference = "Sahih Muslim 134",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 37. Envious
            EmotionItem(
                id = "envious",
                name = "Envious",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFD97706),
                colorDark = Color(0xFF92400E),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_envious",
                    title = "Dua to Pray Barakah for Others and Protect the Heart",
                    arabic = "اللَّهُمَّ بَارِكْ عَلَيْهِ وَلَا تَضُرَّهُ",
                    transliteration = "Allahumma barik 'alaihi wa la tadurrah.",
                    translation = "O Allah, bless him and do not harm him.",
                    category = "Emotion",
                    reference = "Sunan Ibn Majah 3509",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 38. Hatred
            EmotionItem(
                id = "hatred",
                name = "Hatred",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFF991B1B),
                colorDark = Color(0xFF7F1D1D),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_hatred",
                    title = "Dua for Softening Hearts and Reconciling Love",
                    arabic = "اللَّهُمَّ أَلِّفْ بَيْنَ قُلُوبِنَا وَأَصْلِحْ ذَاتَ بَيْنِنَا",
                    transliteration = "Allahumma allif baina qulubina wa aslih dhata bainina.",
                    translation = "O Allah, join our hearts together and reconcile between us.",
                    category = "Emotion",
                    reference = "Sunan Abi Dawud 969",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 39. Humiliated
            EmotionItem(
                id = "humiliated",
                name = "Humiliated",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF9333EA),
                colorDark = Color(0xFF7E22CE),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_humiliated",
                    title = "Dua for Honor & Divine Elevation",
                    arabic = "اللَّهُمَّ اهْدِنِي وَارْزُقْنِي وَعَافِنِي وَارْفَعْنِي",
                    transliteration = "Allahummah-dini war-zuqni wa 'afini war-fa'ni.",
                    translation = "O Allah, guide me, grant me provision, pardon me, and elevate me.",
                    category = "Emotion",
                    reference = "Sunan Ibn Majah 898",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 40. Impatient
            EmotionItem(
                id = "impatient",
                name = "Impatient",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFEA580C),
                colorDark = Color(0xFFC2410C),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_impatient",
                    title = "Supplication for Patience (Sabr)",
                    arabic = "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا وَثَبِّتْ أَقْدَامَنَا وَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
                    transliteration = "Rabbana afrigh 'alaina sabran wa thabbit aqdamana wan-surna 'alal-qawmil-kafirin.",
                    translation = "Our Lord, pour upon us patience and plant firmly our feet and give us victory over the disbelieving people.",
                    category = "Emotion",
                    reference = "Surah Al-Baqarah (2:250)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 41. Insecure
            EmotionItem(
                id = "insecure",
                name = "Insecure",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFF4338CA),
                colorDark = Color(0xFF3730A3),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_insecure",
                    title = "Dua for Security & Tranquility of Soul",
                    arabic = "اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي",
                    transliteration = "Allahummas-tur 'awrati wa amin raw'ati.",
                    translation = "O Allah, cover my faults and grant me safety from my anxieties and fears.",
                    category = "Emotion",
                    reference = "Sunan Abi Dawud 5074",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 42. Irritated
            EmotionItem(
                id = "irritated",
                name = "Irritated",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFF97316),
                colorDark = Color(0xFFC2410C),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_irritated",
                    title = "Dua for Calmness & Overcoming Frustration",
                    arabic = "رَبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ وَأَعُوذُ بِكَ رَبِّ أَنْ يَحْضُرُونِ",
                    transliteration = "Rabbi a'udhu bika min hamazatish-shayatini wa a'udhu bika Rabbi an yahdurun.",
                    translation = "My Lord, I seek refuge in You from the incitements of the devils, and I seek refuge in You, my Lord, lest they be present with me.",
                    category = "Emotion",
                    reference = "Surah Al-Mu'minun (23:97-98)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 43. Love
            EmotionItem(
                id = "love",
                name = "Love",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFEC4899),
                colorDark = Color(0xFFBE185D),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_love",
                    title = "Qur'anic Prayer for Love & Comfort in Family",
                    arabic = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
                    transliteration = "Rabbana hab lana min azwajina wa dhurriyyatina qurrata a'yunin waj-'alna lil-muttaqina imama.",
                    translation = "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us an example for the righteous.",
                    category = "Emotion",
                    reference = "Surah Al-Furqan (25:74)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 44. Nostalgic
            EmotionItem(
                id = "nostalgic",
                name = "Nostalgic",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFF8B5CF6),
                colorDark = Color(0xFF6D28D9),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_nostalgic",
                    title = "Prayer for Good Memories & Righteous Predecessors",
                    arabic = "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَلِمَنْ دَخَلَ بَيْتِيَ مُؤْمِنًا وَلِلْمُؤْمِنِينَ وَالْمُؤْمِنَاتِ",
                    transliteration = "Rabbigh-fir li wa li-walidayya wa liman dakhala baitiya mu'minan wa lil-mu'minina wal-mu'minat.",
                    translation = "My Lord, forgive me and my parents and whoever enters my house a believer and the believing men and believing women.",
                    category = "Emotion",
                    reference = "Surah Nuh (71:28)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 45. Offended
            EmotionItem(
                id = "offended",
                name = "Offended",
                categoryGroup = EmotionCategoryGroup.RELATIONSHIPS,
                colorLight = Color(0xFFE11D48),
                colorDark = Color(0xFF9F1239),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_offended",
                    title = "Dua for Forgiveness and Overlooking Insults",
                    arabic = "وَلْيَعْفُوا وَلْيَصْفَحُوا أَلَا تُحِبُّونَ أَنْ يَغْفِرَ اللَّهُ لَكُمْ وَاللَّهُ غَفُورٌ رَحِيمٌ",
                    transliteration = "Wal-ya'fu wal-yasfahu, ala tuhibbuna an yaghfirallahu lakum, wallahu Ghafurun Rahim.",
                    translation = "And let them pardon and overlook. Would you not like that Allah should forgive you? And Allah is Forgiving and Merciful.",
                    category = "Emotion",
                    reference = "Surah An-Nur (24:22)",
                    authenticityGrade = "Qur'anic Verse"
                )
            ),
            // 46. Peaceful
            EmotionItem(
                id = "peaceful",
                name = "Peaceful",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF10B981),
                colorDark = Color(0xFF047857),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_peaceful",
                    title = "Dua for Absolute Peace & Protection",
                    arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
                    transliteration = "Allahumma Antas-Salam wa minkas-salam, tabarakta ya Dhal-Jalali wal-Ikram.",
                    translation = "O Allah, You are Peace and from You is peace. Blessed are You, O Owner of Majesty and Honor.",
                    category = "Emotion",
                    reference = "Sahih Muslim 592",
                    authenticityGrade = "Authentic (Sahih)"
                )
            ),
            // 47. Rage
            EmotionItem(
                id = "rage",
                name = "Rage",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFB91C1C),
                colorDark = Color(0xFF7F1D1D),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_rage",
                    title = "Dua to Subdue Intense Anger and Hostility",
                    arabic = "اللَّهُمَّ رَبَّ نَبِيِّنَا مُحَمَّدٍ اغْفِرْ لِي ذَنْبِي وَأَذْهِبْ غَيْظَ قَلْبِي",
                    transliteration = "Allahumma Rabba nabiyyina Muhammad, ighfir li dhanbi wa adhib ghaidha qalbi.",
                    translation = "O Allah, Lord of our Prophet Muhammad, forgive my sin and remove the rage from my heart.",
                    category = "Emotion",
                    reference = "Musnad Ahmad 26601",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 48. Satisfied
            EmotionItem(
                id = "satisfied",
                name = "Satisfied",
                categoryGroup = EmotionCategoryGroup.POSITIVE,
                colorLight = Color(0xFF059669),
                colorDark = Color(0xFF047857),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_satisfied",
                    title = "Supplication for Sufficiency in Provision",
                    arabic = "اللَّهُمَّ قَنِّعْنِي بِمَا رَزَقْتَنِي وَبَارِكْ لِي فِيهِ",
                    transliteration = "Allahumma qanni'ni bima razaqtani wa barik li fih.",
                    translation = "O Allah, make me contented with what You have provided me, and bless it for me.",
                    category = "Emotion",
                    reference = "Al-Adab al-Mufrad 631",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 49. Uncertain
            EmotionItem(
                id = "uncertain",
                name = "Uncertain",
                categoryGroup = EmotionCategoryGroup.MOTIVATION,
                colorLight = Color(0xFF0284C7),
                colorDark = Color(0xFF0369A1),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_uncertain",
                    title = "Dua when Unsure of the Future",
                    arabic = "اللَّهُمَّ خِرْ لِي وَاخْتАРْ لِي",
                    transliteration = "Allahumma khir li wakhtar li.",
                    translation = "O Allah, choose for me and select for me what is best.",
                    category = "Emotion",
                    reference = "Jami' at-Tirmidhi 3516",
                    authenticityGrade = "Authentic (Hasan)"
                )
            ),
            // 50. Uneasy
            EmotionItem(
                id = "uneasy",
                name = "Uneasy",
                categoryGroup = EmotionCategoryGroup.DIFFICULT,
                colorLight = Color(0xFFD97706),
                colorDark = Color(0xFFB45309),
                isDarkText = false,
                duaItem = DuaItem(
                    id = "emo_uneasy",
                    title = "Supplication for Heart's Ease & Comfort",
                    arabic = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    transliteration = "Ala bidhikrillahi tatma'innul-qulub.",
                    translation = "Unquestionably, by the remembrance of Allah do hearts find rest.",
                    category = "Emotion",
                    reference = "Surah Ar-Ra'd (13:28)",
                    authenticityGrade = "Qur'anic Verse"
                )
            )
        )
    }

    fun getEmotionById(id: String): EmotionItem? {
        return ALL_EMOTIONS.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
    }
}
