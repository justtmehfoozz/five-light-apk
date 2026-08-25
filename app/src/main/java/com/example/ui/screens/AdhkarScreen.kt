package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.predictiveBackTransform
import com.example.ui.components.rememberPredictiveBackState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Calendar

// ---------------------------------------------------------------------------
// In-Memory Session State for Adhkar Tab Selection (Part 4)
// ---------------------------------------------------------------------------
object AdhkarSessionState {
    var manualSelectedTab: Int? = null
}

@Composable
private fun AdhkarSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = Color.semanticControl
    val activePillBg = Color.semanticPrimaryAccent
    val activeContentColor = Color.semanticAccentForeground
    val inactiveContentColor = Color.semanticSecondaryText

    BoxWithConstraints(
        modifier = modifier
            .clip(CircleShape)
            .background(containerBg)
            .border(
                width = 1.dp,
                color = Color.semanticBorder,
                shape = CircleShape
            )
            .padding(3.dp)
    ) {
        val totalWidth = maxWidth
        val count = tabs.size.coerceAtLeast(1)
        val segmentWidth = totalWidth / count

        val animatedIndex by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 320f),
            label = "adhkarTabIndicator"
        )

        val indicatorOffset = (animatedIndex * segmentWidth.value).dp

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .height(34.dp)
                .clip(CircleShape)
                .background(activePillBg)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val contentColor = if (isSelected) activeContentColor else inactiveContentColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(CircleShape)
                        .clickable { onTabSelected(index) }
                        .testTag("adhkar_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Circular Progress Ring per Dhikr Card (Part 5)
// ---------------------------------------------------------------------------
@Composable
fun DhikrProgressRing(
    currentCount: Int,
    targetCount: Int,
    accentColor: Color = Color.semanticPrimaryAccent,
    modifier: Modifier = Modifier
) {
    val progress = (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "dhikrProgressRing"
    )
    val isComplete = currentCount >= targetCount
    val trackColor = Color.semanticBorder.copy(alpha = 0.6f)

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 2.5.dp.toPx()
            val diameter = size.minDimension
            val arcSize = Size(diameter - strokePx, diameter - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedProgress > 0.001f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (isComplete) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completed",
                tint = accentColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

data class AdhkarItem(
    val title: String,
    val arabic: String,
    val transliteration: String = "",
    val translation: String = "",
    val count: Int = 1,
    val reference: String = "",
    val benefit: String = ""
)

val morningAdhkar = listOf(
    AdhkarItem(
        title = "Ayat al-Kursi (The Throne Verse)",
        arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bi-shay'im-min 'ilmihi illa bima sha'. Wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim.",
        translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
        count = 1,
        reference = "Surah Al-Baqarah (2:255)",
        benefit = "Recited in the morning; an angelic protector guards the reciter until evening."
    ),
    AdhkarItem(
        title = "Sayyid al-Istighfar (Master of Forgiveness)",
        arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
        transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, abu'u laka bini'matika 'alay, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Ant.",
        translation = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
        count = 1,
        reference = "Sahih al-Bukhari 6306",
        benefit = "Whoever recites this with conviction in the morning and dies that day will enter Paradise."
    ),
    AdhkarItem(
        title = "Protection Against All Harm",
        arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shai'un fil-ardi wa la fis-sama'i, wa Huwas-Sami'ul-'Alim.",
        translation = "In the Name of Allah with Whose Name there is protection against every kind of harm in the earth or in the heaven, and He is the All-Hearing and All-Knowing.",
        count = 3,
        reference = "Sunan Abi Dawud 5088 & Jami' at-Tirmidhi 3388",
        benefit = "Recited 3 times in the morning; nothing will harm the reciter until evening."
    ),
    AdhkarItem(
        title = "Morning Gratitude & Sovereignty",
        arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ",
        transliteration = "Asbahna wa asbahal-mulku lillah, walhamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir. Rabbi as'aluka khayra ma fi hadhal-yawmi wa khayra ma ba'dah, wa a'udhu bika min sharri ma fi hadhal-yawmi wa sharri ma ba'dah.",
        translation = "We have reached the morning and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, with no partner. To Him belongs sovereignty and praise, and He is over all things Omnipotent. My Lord, I ask You for the good of this day and the good of what comes after it, and I seek refuge in You from the evil of this day and the evil of what comes after it.",
        count = 1,
        reference = "Sahih Muslim 2723",
        benefit = "Supplication for divine protection, guidance, and goodness throughout the day."
    ),
    AdhkarItem(
        title = "Pleased with Allah, Islam & the Prophet ﷺ",
        arabic = "رَضِيتُ بِاللَّهِ رَبًّـا وَبِالإِسْلَامِ دِينًـا وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّـا",
        transliteration = "Raditu billahi Rabban wa bil-Islami dinan wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
        translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
        count = 3,
        reference = "Sunan Abi Dawud 5072 & Jami' at-Tirmidhi 3389",
        benefit = "Allah has promised to please whoever recites this 3 times in the morning."
    ),
    AdhkarItem(
        title = "Sufficiency in Allah (Hasbiyallah)",
        arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
        transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azim.",
        translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
        count = 7,
        reference = "Surah At-Tawbah 9:129 & Sunan Abi Dawud 5081",
        benefit = "Whoever recites this 7 times, Allah will suffice him in all worldly and otherworldly affairs."
    ),
    AdhkarItem(
        title = "Glorification & Praise",
        arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
        transliteration = "Subhanallahi wa bihamdihi.",
        translation = "Glory be to Allah and His is the praise.",
        count = 100,
        reference = "Sahih Muslim 2692",
        benefit = "Whoever says this 100 times in the morning, his sins are forgiven even if they were like the foam of the sea."
    )
)

val eveningAdhkar = listOf(
    AdhkarItem(
        title = "Ayat al-Kursi (The Throne Verse)",
        arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatun wa la nawm. Lahu ma fis-samawati wa ma fil-ard. Man dhal-ladhi yashfa'u 'indahu illa bi-idhnih. Ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bi-shay'im-min 'ilmihi illa bima sha'. Wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim.",
        translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
        count = 1,
        reference = "Surah Al-Baqarah (2:255)",
        benefit = "Recited in the evening; an angelic protector guards the reciter until morning."
    ),
    AdhkarItem(
        title = "Sayyid al-Istighfar (Master of Forgiveness)",
        arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
        transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, abu'u laka bini'matika 'alay, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Ant.",
        translation = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
        count = 1,
        reference = "Sahih al-Bukhari 6306",
        benefit = "Whoever recites this with conviction in the evening and dies that night will enter Paradise."
    ),
    AdhkarItem(
        title = "Protection Against All Harm",
        arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shai'un fil-ardi wa la fis-sama'i, wa Huwas-Sami'ul-'Alim.",
        translation = "In the Name of Allah with Whose Name there is protection against every kind of harm in the earth or in the heaven, and He is the All-Hearing and All-Knowing.",
        count = 3,
        reference = "Sunan Abi Dawud 5088 & Jami' at-Tirmidhi 3388",
        benefit = "Recited 3 times in the evening; nothing will harm the reciter until morning."
    ),
    AdhkarItem(
        title = "Evening Gratitude & Sovereignty",
        arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا",
        transliteration = "Amsayna wa amsal-mulku lillah, walhamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir. Rabbi as'aluka khayra ma fi hadhihil-laylati wa khayra ma ba'daha, wa a'udhu bika min sharri ma fi hadhihil-laylati wa sharri ma ba'daha.",
        translation = "We have reached the evening and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, with no partner. To Him belongs sovereignty and praise, and He is over all things Omnipotent. My Lord, I ask You for the good of this night and the good of what comes after it, and I seek refuge in You from the evil of this night and the evil of what comes after it.",
        count = 1,
        reference = "Sahih Muslim 2723",
        benefit = "Supplication for divine protection, peace, and goodness throughout the night."
    ),
    AdhkarItem(
        title = "Seeking Refuge in Perfect Words of Allah",
        arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        transliteration = "A'udhu bikalimatil-lahit-tammati min sharri ma khalaq.",
        translation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
        count = 3,
        reference = "Sahih Muslim 2708",
        benefit = "Protection against poisonous creatures, illness, and harm during the night."
    ),
    AdhkarItem(
        title = "Pleased with Allah, Islam & the Prophet ﷺ",
        arabic = "رَضِيتُ بِاللَّهِ رَبًّـا وَبِالإِسْلَامِ دِينًـا وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّـا",
        transliteration = "Raditu billahi Rabban wa bil-Islami dinan wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
        translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
        count = 3,
        reference = "Sunan Abi Dawud 5072 & Jami' at-Tirmidhi 3389",
        benefit = "Allah has promised to please whoever recites this 3 times in the evening."
    ),
    AdhkarItem(
        title = "Sufficiency in Allah (Hasbiyallah)",
        arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
        transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azim.",
        translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
        count = 7,
        reference = "Surah At-Tawbah 9:129 & Sunan Abi Dawud 5081",
        benefit = "Whoever recites this 7 times, Allah will suffice him in all worldly and otherworldly affairs."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit,
    initialItemTitle: String? = null
) {
    val isDark = isAppInDarkTheme()
    val haptic = LocalHapticFeedback.current

    // PART 4: TIME-AWARE DEFAULT TAB WITH SESSION OVERRIDE
    val defaultTimeTab = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < 12) 0 else 1 // Morning before 12 PM, Evening 12 PM+
    }

    var selectedTab by remember {
        mutableStateOf(AdhkarSessionState.manualSelectedTab ?: defaultTimeTab)
    }

    val handleTabSelect = { index: Int ->
        AdhkarSessionState.manualSelectedTab = index
        selectedTab = index
    }

    val adhkarListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    LaunchedEffect(initialItemTitle) {
        if (initialItemTitle != null) {
            val isMorning = morningAdhkar.any { it.title.equals(initialItemTitle, ignoreCase = true) }
            val isEvening = eveningAdhkar.any { it.title.equals(initialItemTitle, ignoreCase = true) }
            if (isMorning) {
                handleTabSelect(0)
                val targetIndex = morningAdhkar.indexOfFirst { it.title.equals(initialItemTitle, ignoreCase = true) }
                if (targetIndex >= 0) {
                    adhkarListState.animateScrollToItem(targetIndex)
                }
            } else if (isEvening) {
                handleTabSelect(1)
                val targetIndex = eveningAdhkar.indexOfFirst { it.title.equals(initialItemTitle, ignoreCase = true) }
                if (targetIndex >= 0) {
                    adhkarListState.animateScrollToItem(targetIndex)
                }
            }
        }
    }

    val morningProgressMap = remember { mutableStateMapOf<Int, Int>() }
    val eveningProgressMap = remember { mutableStateMapOf<Int, Int>() }

    val activeProgressMap = if (selectedTab == 0) morningProgressMap else eveningProgressMap
    val adhkarList = if (selectedTab == 0) morningAdhkar else eveningAdhkar

    // PART 7: SECTION-COMPLETE ACKNOWLEDGMENT
    val isMorningComplete = remember(morningProgressMap.toMap()) {
        morningAdhkar.isNotEmpty() && morningAdhkar.indices.all { idx ->
            (morningProgressMap[idx] ?: 0) >= morningAdhkar[idx].count
        }
    }
    val isEveningComplete = remember(eveningProgressMap.toMap()) {
        eveningAdhkar.isNotEmpty() && eveningAdhkar.indices.all { idx ->
            (eveningProgressMap[idx] ?: 0) >= eveningAdhkar[idx].count
        }
    }
    val isCurrentSectionComplete = if (selectedTab == 0) isMorningComplete else isEveningComplete

    // PART 8: FOCUSED SWIPE-THROUGH MODE STATE
    var isFocusMode by remember { mutableStateOf(false) }

    if (isFocusMode) {
        AdhkarFocusModeScreen(
            title = if (selectedTab == 0) "Morning Adhkar" else "Evening Adhkar",
            items = adhkarList,
            progressMap = activeProgressMap,
            onClose = { isFocusMode = false },
            isDark = isDark
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.semanticBackground)
            .statusBarsPadding()
    ) {
        // Top Header with Focus Mode Entry (Part 8)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.semanticPrimaryText
                    )
                }
                Text(
                    text = "Daily Adhkar",
                    fontFamily = SerifHeaderFont,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.semanticPrimaryText
                )
            }

            // Focus Mode Button
            IconButton(
                onClick = { isFocusMode = true },
                modifier = Modifier.testTag("adhkar_focus_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CenterFocusStrong,
                    contentDescription = "Focus Mode",
                    tint = Color.semanticPrimaryText
                )
            }
        }

        // Segmented Tabs with Completion Checkmarks (Part 7)
        val morningLabel = if (isMorningComplete) "Morning ✓" else "Morning"
        val eveningLabel = if (isEveningComplete) "Evening ✓" else "Evening"
        val adhkarTabs = listOf(morningLabel, eveningLabel)

        AdhkarSegmentedTabs(
            tabs = adhkarTabs,
            selectedIndex = selectedTab,
            onTabSelected = handleTabSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // PART 7: SECTION-COMPLETE BANNER ACKNOWLEDGMENT
        AnimatedVisibility(
            visible = isCurrentSectionComplete,
            enter = fadeIn(tween(240)) + expandVertically(tween(240)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.semanticPrimaryAccent.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color.semanticPrimaryAccent.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (selectedTab == 0) "Morning Adhkar completed" else "Evening Adhkar completed",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText
                    )
                }
            }
        }

        LazyColumn(
            state = adhkarListState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(adhkarList.size, key = { index -> "${selectedTab}_${adhkarList[index].title}" }) { index ->
                val item = adhkarList[index]
                val currentCount = activeProgressMap[index] ?: 0

                AdhkarItemCard(
                    item = item,
                    currentCount = currentCount,
                    onIncrement = {
                        if (currentCount < item.count) {
                            activeProgressMap[index] = currentCount + 1
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onSwipeComplete = {
                        activeProgressMap[index] = item.count
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    isDark = isDark
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Adhkar Item Card with Progress Ring (Part 5) & Swipe-To-Complete (Part 6)
// ---------------------------------------------------------------------------
@Composable
fun AdhkarItemCard(
    item: AdhkarItem,
    currentCount: Int,
    onIncrement: () -> Unit,
    onSwipeComplete: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val isComplete = currentCount >= item.count
    var cardWidthPx by remember { mutableStateOf(1f) }
    var dragOffsetX by remember { mutableStateOf(0f) }

    val animatedTranslationX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "dhikrCardSwipe"
    )

    val cardBgColor = if (isDark) {
        if (isComplete) Color.semanticSurfaceElevated else Color.semanticSurface
    } else {
        if (isComplete) Color.semanticPrimaryAccent.copy(alpha = 0.08f) else Color.semanticSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat().coerceAtLeast(1f) }
    ) {
        // Background reveal under swipe
        if (dragOffsetX > 10f) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.semanticPrimaryAccent.copy(alpha = 0.15f),
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Complete",
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Foreground Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBgColor,
            border = BorderStroke(1.dp, Color.semanticBorder),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = animatedTranslationX
                }
                .pointerInput(isComplete) {
                    if (!isComplete) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragOffsetX = 0f },
                            onDragEnd = {
                                val threshold = cardWidthPx * 0.40f
                                if (dragOffsetX >= threshold) {
                                    onSwipeComplete()
                                }
                                dragOffsetX = 0f
                            },
                            onDragCancel = { dragOffsetX = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX = (dragOffsetX + dragAmount).coerceAtLeast(0f)
                            }
                        )
                    }
                }
                .clickable {
                    if (!isComplete) {
                        onIncrement()
                    }
                }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = item.title,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.semanticPrimaryText
                        )
                        if (item.reference.isNotEmpty()) {
                            Text(
                                text = item.reference,
                                fontFamily = SpaceGrotesk,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.semanticSecondaryText.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // PART 5: Progress ring with count readout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$currentCount / ${item.count}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isComplete) Color.semanticPrimaryAccent else Color.semanticSecondaryText
                        )

                        DhikrProgressRing(
                            currentCount = currentCount,
                            targetCount = item.count,
                            accentColor = Color.semanticPrimaryAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ArabicText(
                    text = item.arabic,
                    fontSize = 25.sp,
                    color = Color.semanticPrimaryText,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.transliteration.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.transliteration,
                        fontFamily = SpaceGrotesk,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.5.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 20.sp
                    )
                }

                if (item.translation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.translation,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.5.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 20.sp
                    )
                }

                if (item.benefit.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.semanticControl.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Benefit: ${item.benefit}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = Color.semanticSecondaryText,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PART 8: Focused Swipe-Through Mode
// ---------------------------------------------------------------------------
@Composable
fun AdhkarFocusModeScreen(
    title: String,
    items: List<AdhkarItem>,
    progressMap: MutableMap<Int, Int>,
    onClose: () -> Unit,
    isDark: Boolean
) {
    val haptic = LocalHapticFeedback.current
    var currentIndex by remember { mutableStateOf(0) }
    var slideDirection by remember { mutableStateOf(1) }
    var totalDragX by remember { mutableStateOf(0f) }

    val adhkarBackState = rememberPredictiveBackState()
    RegisterPredictiveBackHandler(
        enabled = true,
        backState = adhkarBackState,
        onBack = onClose
    )

    val safeIndex = if (items.isNotEmpty()) currentIndex.coerceIn(0, items.size - 1) else 0
    val currentItem = if (items.isNotEmpty()) items[safeIndex] else return
    val currentCount = progressMap[safeIndex] ?: 0
    val isComplete = currentCount >= currentItem.count

    Column(
        modifier = Modifier
            .fillMaxSize()
            .predictiveBackTransform(adhkarBackState.progress, adhkarBackState.swipeEdge)
            .background(Color.semanticBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("adhkar_focus_mode_screen")
    ) {
        // Top Navigation Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Exit Focus Mode",
                    tint = Color.semanticPrimaryText
                )
            }

            Text(
                text = "$title (${safeIndex + 1}/${items.size})",
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.semanticSecondaryText
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // Center Focused Content with Horizontal Swipe
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .pointerInput(items.size) {
                    if (items.size > 1) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragX = 0f },
                            onDragEnd = {
                                if (totalDragX < -40f && currentIndex < items.size - 1) {
                                    // Next item
                                    slideDirection = 1
                                    currentIndex++
                                } else if (totalDragX > 40f && currentIndex > 0) {
                                    // Prev item
                                    slideDirection = -1
                                    currentIndex--
                                }
                                totalDragX = 0f
                            },
                            onDragCancel = { totalDragX = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = safeIndex,
                transitionSpec = {
                    if (slideDirection > 0) {
                        slideInHorizontally(animationSpec = tween(220)) { it / 2 } + fadeIn(animationSpec = tween(220)) togetherWith
                                slideOutHorizontally(animationSpec = tween(180)) { -it / 2 } + fadeOut(animationSpec = tween(180))
                    } else {
                        slideInHorizontally(animationSpec = tween(220)) { -it / 2 } + fadeIn(animationSpec = tween(220)) togetherWith
                                slideOutHorizontally(animationSpec = tween(180)) { it / 2 } + fadeOut(animationSpec = tween(180))
                    }
                },
                label = "adhkarFocusItemTransition"
            ) { targetIdx ->
                val item = items[targetIdx.coerceIn(0, items.size - 1)]
                val itemCurrentCount = progressMap[targetIdx] ?: 0
                val itemIsComplete = itemCurrentCount >= item.count

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.semanticSurface,
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.title,
                            fontFamily = SerifHeaderFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticPrimaryText,
                            textAlign = TextAlign.Center
                        )

                        if (item.reference.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.reference,
                                fontFamily = SpaceGrotesk,
                                fontSize = 12.sp,
                                color = Color.semanticSecondaryText,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ArabicText(
                            text = item.arabic,
                            fontSize = 28.sp,
                            color = Color.semanticPrimaryText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (item.transliteration.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = item.transliteration,
                                fontFamily = SpaceGrotesk,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                color = Color.semanticSecondaryText,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (item.translation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = item.translation,
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.5.sp,
                                color = Color.semanticSecondaryText,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (item.benefit.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.semanticControl.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.benefit,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 12.sp,
                                    color = Color.semanticSecondaryText,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large interactive counter button
                        val buttonScale by animateFloatAsState(
                            targetValue = if (itemIsComplete) 1.0f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "focusButtonScale"
                        )

                        Surface(
                            shape = CircleShape,
                            color = if (itemIsComplete) Color.semanticPrimaryAccent.copy(alpha = 0.12f) else Color.semanticControl,
                            border = BorderStroke(
                                1.5.dp,
                                if (itemIsComplete) Color.semanticPrimaryAccent else Color.semanticBorder
                            ),
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    scaleX = buttonScale
                                    scaleY = buttonScale
                                }
                                .clickable {
                                    if (!itemIsComplete) {
                                        progressMap[targetIdx] = itemCurrentCount + 1
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                DhikrProgressRing(
                                    currentCount = itemCurrentCount,
                                    targetCount = item.count,
                                    accentColor = Color.semanticPrimaryAccent,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = "$itemCurrentCount/${item.count}",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (itemIsComplete) Color.semanticPrimaryAccent else Color.semanticPrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (itemIsComplete) "Completed ✓" else "Tap to count",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = if (itemIsComplete) Color.semanticPrimaryAccent else Color.semanticMutedText
                        )
                    }
                }
            }
        }

        // Bottom Carousel Indicators
        if (items.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            ) {
                items.indices.forEach { index ->
                    val isSelected = index == safeIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.semanticPrimaryAccent
                                else Color.semanticBorder
                            )
                    )
                }
            }
        }
    }
}
