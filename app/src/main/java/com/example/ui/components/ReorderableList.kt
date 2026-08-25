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


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.isAppInDarkTheme

data class ReorderableItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val isEnabled: Boolean
)

@Composable
fun ReorderableSettingsList(
    items: List<ReorderableItemData>,
    onToggleItem: (id: String, isChecked: Boolean) -> Unit,
    onReorderComplete: (newOrderIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var listState by remember { mutableStateOf(items) }
    var draggingItemId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(items) {
        listState = items
    }

    val approxItemHeightPx = 80f
    val isDark = isAppInDarkTheme()

    fun moveItem(itemId: String, delta: Int) {
        val currIdx = listState.indexOfFirst { it.id == itemId }
        if (currIdx == -1) return
        val newIdx = (currIdx + delta).coerceIn(0, listState.lastIndex)
        if (newIdx != currIdx) {
            val mutable = listState.toMutableList()
            val moved = mutable.removeAt(currIdx)
            mutable.add(newIdx, moved)
            listState = mutable
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onReorderComplete(mutable.map { it.id })
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listState.forEachIndexed { index, item ->
            key(item.id) {
                val isBeingDragged = draggingItemId == item.id
                val elevation by animateDpAsState(if (isBeingDragged) 8.dp else 0.dp, label = "elevation")
                val scale by animateFloatAsState(if (isBeingDragged) 1.02f else 1.0f, label = "scale")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isBeingDragged) 10f else 1f)
                        .graphicsLayer {
                            translationY = if (isBeingDragged) dragOffsetY else 0f
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(elevation, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBeingDragged) {
                            if (isDark) Color(0xFF2C2C2E) else MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBeingDragged) (if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.primary) else (if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle on left
                        Box(
                            modifier = Modifier
                                .pointerInput(item.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingItemId = item.id
                                            dragOffsetY = 0f
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragEnd = {
                                            draggingItemId = null
                                            dragOffsetY = 0f
                                            onReorderComplete(listState.map { it.id })
                                        },
                                        onDragCancel = {
                                            draggingItemId = null
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y

                                            val currentId = draggingItemId ?: return@detectDragGestures
                                            val currentIndex = listState.indexOfFirst { it.id == currentId }
                                            if (currentIndex == -1) return@detectDragGestures

                                            val targetShift = (dragOffsetY / approxItemHeightPx).toInt()
                                            val newTargetIndex = (currentIndex + targetShift).coerceIn(0, listState.lastIndex)

                                            if (newTargetIndex != currentIndex) {
                                                val mutable = listState.toMutableList()
                                                val movedItem = mutable.removeAt(currentIndex)
                                                mutable.add(newTargetIndex, movedItem)
                                                listState = mutable

                                                dragOffsetY -= (newTargetIndex - currentIndex) * approxItemHeightPx
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        }
                                    )
                                }
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Reorder handle",
                                tint = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Up / Down arrow buttons for precise reordering by stable ID
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = { moveItem(item.id, -1) },
                                enabled = index > 0,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move ${item.title} up",
                                    tint = if (index > 0) (if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurfaceVariant) else (if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { moveItem(item.id, 1) },
                                enabled = index < listState.lastIndex,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move ${item.title} down",
                                    tint = if (index < listState.lastIndex) (if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurfaceVariant) else (if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                            Text(
                                text = item.title,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
                            )
                            if (item.subtitle.isNotEmpty()) {
                                Text(
                                    text = item.subtitle,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Switch(
                            checked = item.isEnabled,
                            onCheckedChange = { checked ->
                                onToggleItem(item.id, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}
