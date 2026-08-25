package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NameOfAllah
import com.example.data.model.NamesOfAllahData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesOfAllahScreen(
    onBack: () -> Unit,
    initialNameNumber: Int? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    val bgColor = MaterialTheme.colorScheme.background
    val textPrimary = if (isDark) Color(0xFFF2F2EE) else Color.semanticPrimaryText
    val textSecondary = if (isDark) Color(0xFFA8A8A2) else Color.semanticSecondaryText
    val accentColor = Color.semanticPrimaryAccent
    val borderStrokeColor = if (isDark) MaterialTheme.colorScheme.outline else Color.semanticBorder

    val initialIndex = remember(initialNameNumber) {
        if (initialNameNumber != null) {
            NamesOfAllahData.NAMES.indexOfFirst { it.number == initialNameNumber }.coerceAtLeast(0)
        } else {
            0
        }
    }
    val namesListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState(firstVisibleItemIndex = initialIndex) }

    LaunchedEffect(initialNameNumber) {
        if (initialNameNumber != null) {
            val targetIndex = NamesOfAllahData.NAMES.indexOfFirst { it.number == initialNameNumber }
            if (targetIndex >= 0) {
                namesListState.animateScrollToItem(targetIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .testTag("names_of_allah_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("names_of_allah_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Names of Allah",
                    fontFamily = SerifHeaderFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "The 99 Beautiful Names (Asma-ul-Husna)",
                    fontFamily = SpaceGrotesk,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }

        // List
        LazyColumn(
            state = namesListState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("names_of_allah_list")
        ) {
            items(
                items = NamesOfAllahData.NAMES,
                key = { it.number }
            ) { item ->
                NameOfAllahCard(
                    item = item,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    borderStrokeColor = borderStrokeColor
                )
            }
        }
    }
}

@Composable
fun NameOfAllahCard(
    item: NameOfAllah,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    borderStrokeColor: Color
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.semanticControl
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outline else borderStrokeColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("name_card_${item.number}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF2C2C2E).copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    )
                    Text(
                        text = "${item.number}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDark) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // English & Transliteration
                Column {
                    Text(
                        text = item.nameTransliteration,
                        fontFamily = if (isDark) null else SpaceGrotesk,
                        style = if (isDark) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGrotesk, fontSize = 17.sp),
                        fontWeight = if (isDark) FontWeight.SemiBold else FontWeight.Bold,
                        color = if (isDark) Color(0xFFF2F2EE) else textPrimary
                    )
                    if (!isDark) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = item.englishMeaning,
                        fontFamily = if (isDark) null else SpaceGrotesk,
                        style = if (isDark) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceGrotesk, fontSize = 13.sp),
                        color = if (isDark) Color(0xFFA8A8A2) else textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arabic Name & Share Action (Part 2 — 99 Names Share)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.nameArabic,
                    style = ArabicTextStyle.copy(fontSize = 22.sp),
                    color = if (isDark) Color(0xFFB0B0AA) else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val shareText = "${item.nameTransliteration}\n\n${item.nameArabic}\n\n${item.englishMeaning}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_TITLE, item.nameTransliteration)
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Name of Allah")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("name_share_${item.number}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share ${item.nameTransliteration}",
                        tint = textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
