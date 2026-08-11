package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

enum class NavItem(val route: String, val label: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    QIBLA("qibla", "Qibla", Icons.Filled.Explore, Icons.Outlined.Explore),
    QURAN("quran", "Quran", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    TASBEEH("tasbeeh", "Tasbeeh", Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked),
    MORE("calendar", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

@Composable
fun SereneBottomNavBar(
    currentRoute: String,
    onNavigate: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }

    // Floating Glass Surface Styling
    val dockBg = if (isDark) Color(0xD91C1C1E) else Color(0xD9F5F5F7) // ~85% translucent neutral glass
    val dockBorder = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000) // subtle 1px border
    val activeHighlightBg = if (isDark) Color(0x26FFFFFF) else Color(0x1F000000) // ~15% / 12% subtle active pill
    val activeIconTint = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)
    val inactiveIconTint = if (isDark) Color(0x80FFFFFF) else Color(0x73000000)

    val selectedIndex = NavItem.entries.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    // Smooth spring active indicator movement across items
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dockIndicatorPosition"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Standalone Glass Capsule (Overlays screen content)
        Box(
            modifier = Modifier
                .width(310.dp)
                .height(62.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.12f)
                )
                .clip(CircleShape)
                .background(dockBg)
                .border(
                    border = BorderStroke(1.dp, dockBorder),
                    shape = CircleShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val totalWidth = maxWidth
                val segmentWidth = totalWidth / NavItem.entries.size.toFloat()
                val leftPos = (animatedIndex * segmentWidth.value).dp

                // Active item background highlight
                Box(
                    modifier = Modifier
                        .offset(x = leftPos)
                        .width(segmentWidth)
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(activeHighlightBg)
                )

                // 5 Icon navigation items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem.entries.forEach { item ->
                        val isSelected = currentRoute == item.route

                        val animatedTint by animateColorAsState(
                            targetValue = if (isSelected) activeIconTint else inactiveIconTint,
                            animationSpec = tween(durationMillis = 200),
                            label = "dockIconTint"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("nav_${item.route}")
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onNavigate(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                                contentDescription = item.label,
                                tint = animatedTint,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



