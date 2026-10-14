package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EmotionCategoryGroup
import com.example.data.model.EmotionItem
import com.example.data.util.EmotionData
import com.example.ui.components.PageHeader
import com.example.ui.components.RegisterPredictiveBackHandler
import com.example.ui.components.rememberPredictiveBackState
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.util.LocalVolumeKeyDispatcher
import com.example.ui.util.VolumeKeyEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// =========================================================================
// HEXAGONAL SPIRAL COORDINATE GENERATOR
// =========================================================================

private data class HexCoord(val q: Int, val r: Int)

private fun generateHexCoords(count: Int): List<HexCoord> {
    if (count <= 0) return emptyList()
    val coords = mutableListOf<HexCoord>()
    coords.add(HexCoord(0, 0))

    val directions = listOf(
        HexCoord(1, 0), HexCoord(0, 1), HexCoord(-1, 1),
        HexCoord(-1, 0), HexCoord(0, -1), HexCoord(1, -1)
    )

    var radius = 1
    while (coords.size < count) {
        var curr = HexCoord(-radius, radius)
        for (i in 0 until 6) {
            val dir = directions[i]
            for (step in 0 until radius) {
                if (coords.size < count) {
                    coords.add(curr)
                }
                curr = HexCoord(curr.q + dir.q, curr.r + dir.r)
            }
        }
        radius++
    }
    return coords
}

// =========================================================================
// MAIN HONEYCOMB EMOTION EXPLORER SCREEN
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneycombEmotionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val volumeDispatcher = LocalVolumeKeyDispatcher.current
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryGroup by remember { mutableStateOf(EmotionCategoryGroup.ALL) }
    var selectedEmotionForDetail by remember { mutableStateOf<EmotionItem?>(null) }

    // Canvas Pan and Zoom state
    var targetScale by remember { mutableFloatStateOf(1.0f) }
    var targetPanX by remember { mutableFloatStateOf(0f) }
    var targetPanY by remember { mutableFloatStateOf(0f) }

    var showZoomIndicator by remember { mutableStateOf(false) }

    // Auto-hide zoom indicator after 1.5 seconds
    LaunchedEffect(showZoomIndicator, targetScale) {
        if (showZoomIndicator) {
            delay(1500L)
            showZoomIndicator = false
        }
    }

    val triggerZoomChange: (Float) -> Unit = { factor ->
        targetScale = (targetScale * factor).coerceIn(0.6f, 2.2f)
        showZoomIndicator = true
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Register Volume Up / Down Key Listener for physical button zooming
    DisposableEffect(volumeDispatcher) {
        val listener = object : VolumeKeyEventListener {
            override fun onVolumeKeyDown(keyCode: Int): Boolean {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    triggerZoomChange(1.12f)
                    return true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    triggerZoomChange(0.89f)
                    return true
                }
                return false
            }

            override fun onVolumeKeyUp(keyCode: Int): Boolean {
                return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
            }
        }
        volumeDispatcher?.setVolumeKeyEventListener(listener)
        onDispose {
            volumeDispatcher?.setVolumeKeyEventListener(null)
        }
    }

    // Predictive back
    val predictiveBackState = rememberPredictiveBackState()
    RegisterPredictiveBackHandler(
        enabled = true,
        backState = predictiveBackState,
        onBack = onBack
    )

    // Filter emotions
    val filteredEmotions = remember(searchQuery, selectedCategoryGroup) {
        EmotionData.ALL_EMOTIONS.filter { emotion ->
            val matchesCategory = when (selectedCategoryGroup) {
                EmotionCategoryGroup.ALL -> true
                else -> emotion.categoryGroup == selectedCategoryGroup
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                emotion.name.contains(searchQuery, ignoreCase = true) ||
                        emotion.duaItem.title.contains(searchQuery, ignoreCase = true) ||
                        emotion.duaItem.translation.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    // Precompute hex grid layout coordinates
    val hexCoords = remember(filteredEmotions.size) {
        generateHexCoords(filteredEmotions.size)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f),
        label = "honeycombScale"
    )
    val animatedPanX by animateFloatAsState(
        targetValue = targetPanX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f),
        label = "honeycombPanX"
    )
    val animatedPanY by animateFloatAsState(
        targetValue = targetPanY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f),
        label = "honeycombPanY"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .testTag("honeycomb_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color.semanticPrimaryText
                        )
                    }
                    Text(
                        text = "I am feeling...",
                        fontFamily = SerifHeaderFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryText,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search emotions (e.g. Sad, Hope, Anxious)...",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = Color.semanticSecondaryText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.semanticSecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.semanticPrimaryAccent,
                        unfocusedBorderColor = Color.semanticBorder,
                        focusedContainerColor = Color.semanticSurface,
                        unfocusedContainerColor = Color.semanticSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(52.dp)
                        .testTag("honeycomb_search_bar")
                )

                // Filter Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmotionCategoryGroup.entries.forEach { group ->
                        val isSelected = selectedCategoryGroup == group
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryGroup = group
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            label = {
                                Text(
                                    text = group.label,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.semanticPrimaryAccent,
                                selectedLabelColor = Color.White,
                                containerColor = Color.semanticSurface,
                                labelColor = Color.semanticPrimaryText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.semanticBorder,
                                selectedBorderColor = Color.semanticPrimaryAccent
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive 2D Canvas Container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            targetScale = (targetScale * zoom).coerceIn(0.6f, 2.2f)
                            targetPanX += pan.x
                            targetPanY += pan.y
                            showZoomIndicator = true
                        }
                    }
                    .testTag("honeycomb_canvas")
            ) {
                val viewportWidthPx = constraints.maxWidth.toFloat()
                val viewportHeightPx = constraints.maxHeight.toFloat()
                val centerX = viewportWidthPx / 2f
                val centerY = viewportHeightPx / 2f

                val baseDiameterPx = with(density) { 82.dp.toPx() }
                val colSpacingPx = baseDiameterPx * 1.08f
                val rowSpacingPx = baseDiameterPx * 0.94f

                if (filteredEmotions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No emotions found",
                                fontFamily = SerifHeaderFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticPrimaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search or category filter",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Render Hexagonal Honeycomb Bubbles
                    filteredEmotions.forEachIndexed { index, emotion ->
                        val coord = hexCoords.getOrNull(index) ?: HexCoord(0, 0)

                        val hexX = colSpacingPx * (coord.q.toFloat() + coord.r.toFloat() / 2.0f)
                        val hexY = rowSpacingPx * coord.r.toFloat()

                        val scaledX = (hexX * animatedScale) + animatedPanX + centerX
                        val scaledY = (hexY * animatedScale) + animatedPanY + centerY
                        val currentSizePx = baseDiameterPx * animatedScale
                        val currentSizeDp = (82f * animatedScale).dp

                        val leftOffset = (scaledX - currentSizePx / 2f).roundToInt()
                        val topOffset = (scaledY - currentSizePx / 2f).roundToInt()

                        val bubbleColor = if (isDark) emotion.colorDark else emotion.colorLight

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(leftOffset, topOffset) }
                                .size(currentSizeDp)
                                .shadow(
                                    elevation = (4 * animatedScale).dp,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(bubbleColor)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedEmotionForDetail = emotion
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Subtle glossy reflection ring
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.28f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            val fontScaleSp = when {
                                emotion.name.length <= 6 -> (13 * animatedScale).coerceIn(9f, 16f).sp
                                emotion.name.length <= 10 -> (11 * animatedScale).coerceIn(8f, 14f).sp
                                else -> (9.5f * animatedScale).coerceIn(7.5f, 12f).sp
                            }

                            Text(
                                text = emotion.name,
                                fontFamily = SpaceGrotesk,
                                fontSize = fontScaleSp,
                                fontWeight = FontWeight.Bold,
                                color = if (emotion.isDarkText) Color(0xFF1F2937) else Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = fontScaleSp * 1.15f,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Zoom Floating Indicator & Touch Controls Overlay
            AnimatedVisibility(
                visible = showZoomIndicator || targetScale != 1.0f,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.semanticSurfaceElevated,
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { triggerZoomChange(0.88f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.semanticPrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${(animatedScale * 100).roundToInt()}%",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.semanticPrimaryText,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { triggerZoomChange(1.14f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Zoom In",
                                tint = Color.semanticPrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Emotion Detail Bottom Sheet
    if (selectedEmotionForDetail != null) {
        EmotionDetailBottomSheet(
            emotion = selectedEmotionForDetail!!,
            onDismiss = { selectedEmotionForDetail = null }
        )
    }
}

// =========================================================================
// EMOTION DUA DETAIL BOTTOM SHEET
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionDetailBottomSheet(
    emotion: EmotionItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }

    val dua = emotion.duaItem
    var isBookmarked by remember { mutableStateOf(getSavedDuaBookmarks(context).contains(dua.id)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.semanticSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            // Emotion Badge Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) emotion.colorDark.copy(alpha = 0.25f) else emotion.colorLight.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, if (isDark) emotion.colorDark else emotion.colorLight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "FEELING: ${emotion.name.uppercase()}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) emotion.colorDark else emotion.colorLight
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.semanticSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = dua.title,
                fontFamily = SerifHeaderFont,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            // Sensitive / Suicidal Compassionate Support Card
            if (emotion.isSensitive) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF991B1B).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF991B1B).copy(alpha = 0.40f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "You are deeply valued, loved, and not alone.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "In Islam, your life is sacred. If you are experiencing overwhelming emotional distress, please reach out for compassionate support immediately.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = Color.semanticPrimaryText,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneInTalk,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Crisis Helpline (988)",
                                fontFamily = SpaceGrotesk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic Supplication Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.semanticSurfaceElevated,
                border = BorderStroke(1.dp, Color.semanticBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = dua.arabic,
                        fontFamily = SerifHeaderFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.semanticPrimaryAccent,
                        textAlign = TextAlign.Right,
                        lineHeight = 36.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = dua.transliteration,
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"${dua.translation}\"",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 19.sp
                    )

                    if (!dua.reference.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Source: ${dua.reference} (${dua.authenticityGrade})",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryAccent
                        )
                    }

                    if (!dua.benefitOrNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: ${dua.benefitOrNotes}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 11.sp,
                            color = Color.semanticSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Bookmark, Copy, Share
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isBookmarked = toggleDuaBookmark(context, dua.id)
                        Toast.makeText(
                            context,
                            if (isBookmarked) "Saved to Bookmarks" else "Removed from Bookmarks",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticPrimaryText
                    ),
                    border = BorderStroke(1.dp, if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBookmarked) "Saved" else "Bookmark",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        clipboardManager.setText(AnnotatedString("${dua.title}\n\n${dua.arabic}\n\n${dua.translation}\n\nReference: ${dua.reference ?: ""}"))
                        Toast.makeText(context, "Dua copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.semanticPrimaryText),
                    border = BorderStroke(1.dp, Color.semanticBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        shareDuaText(context, dua)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
