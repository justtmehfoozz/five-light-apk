package com.example.ui.components

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JourneyNodeType
import com.example.data.model.PrayerJourneyNode
import com.example.ui.theme.AmiriFont
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerJourneySheet(
    nodes: List<PrayerJourneyNode>,
    onTogglePrayer: (com.example.data.model.PrayerName) -> Unit = {},
    onSetPrayerStatus: (com.example.data.model.PrayerName, com.example.data.model.PrayerStatus) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prayer Journey",
                        fontFamily = SerifHeaderFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Timeline of your Islamic day",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Calculating prayer timeline...",
                        fontFamily = SpaceGrotesk,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(nodes) { index, node ->
                        PrayerJourneyNodeRow(
                            node = node,
                            isFirst = index == 0,
                            isLast = index == nodes.size - 1,
                            onTogglePrayer = onTogglePrayer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrayerJourneyNodeRow(
    node: PrayerJourneyNode,
    isFirst: Boolean,
    isLast: Boolean,
    onTogglePrayer: (com.example.data.model.PrayerName) -> Unit = {}
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val isFard = node.type == JourneyNodeType.FARD
    val isAdhkar = node.type == JourneyNodeType.ADHKAR
    val badgeColor = when {
        node.isMissed -> MaterialTheme.colorScheme.error
        node.isCurrentNow -> MaterialTheme.colorScheme.primary
        isFard -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        isAdhkar -> Color.semanticPrimaryAccent
        else -> MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Column (Dot + Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(14.dp)
                        .background(badgeColor.copy(alpha = if (node.isCurrentNow) 0.8f else 0.25f))
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Indicator Node
            Box(
                modifier = Modifier
                    .size(if (node.isCurrentNow) 20.dp else 14.dp)
                    .clip(CircleShape)
                    .background(
                        if (node.isCurrentNow) badgeColor else badgeColor.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (node.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                } else if (node.isMissed) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Missed",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(10.dp)
                    )
                } else if (node.isCurrentNow) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(badgeColor.copy(alpha = if (node.isCurrentNow) 0.8f else 0.25f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Card Content
        val isClickableFard = isFard && node.prayerName != null
        Surface(
            onClick = {
                val prayer = node.prayerName
                if (isClickableFard && prayer != null) {
                    onTogglePrayer(prayer)
                }
            },
            enabled = isClickableFard,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (node.isCurrentNow) badgeColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = if (node.isCurrentNow) 1.5.dp else 1.dp,
                color = if (node.isCurrentNow) badgeColor else MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = node.title,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Type Pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = when (node.type) {
                                    JourneyNodeType.FARD -> "Fard"
                                    JourneyNodeType.NAFL -> "Nafl"
                                    JourneyNodeType.ADHKAR -> "Adhkar"
                                },
                                fontFamily = SpaceGrotesk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (node.isCurrentNow) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "NOW",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (node.isMissed) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "MISSED",
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = node.subtitle,
                        fontFamily = SpaceGrotesk,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = node.timeFormatted,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (node.isCurrentNow) badgeColor else MaterialTheme.colorScheme.onSurface
                    )

                    if (node.arabicTitle != null) {
                        Text(
                            text = node.arabicTitle,
                            fontFamily = AmiriFont,
                            fontSize = 14.sp,
                            color = badgeColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}