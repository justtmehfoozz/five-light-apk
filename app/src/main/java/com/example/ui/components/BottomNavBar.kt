package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpaceGrotesk

enum class NavItem(val route: String, val label: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    HOME("home", "Prayer", Icons.Filled.Home, Icons.Outlined.Home),
    QIBLA("qibla", "Qibla", Icons.Filled.Explore, Icons.Outlined.Explore),
    QURAN("quran", "Quran", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    TASBEEH("tasbeeh", "Tasbeeh", Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked),
    CALENDAR("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
}

@Composable
fun SereneBottomNavBar(
    currentRoute: String,
    onNavigate: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }
    val barBg = if (isDark) Color(0x99181818) else Color(0x99FFFFFF)
    val barBorder = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000)
    val activePillBg = if (isDark) Color(0xFFF2EFE9) else Color(0xFF1A1815)
    val activePillText = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val inactiveText = if (isDark) Color(0xFF7A7568) else Color(0xFF6B6558)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 22.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Liquid Glass Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f)
                )
                .clip(CircleShape)
                .background(barBg)
                .border(
                    border = BorderStroke(1.dp, barBorder),
                    shape = CircleShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem.entries.forEach { item ->
                    val isSelected = currentRoute == item.route

                    val animatedIconTint by animateColorAsState(
                        targetValue = if (isSelected) activePillText else inactiveText,
                        animationSpec = tween(durationMillis = 200),
                        label = "navIconTint"
                    )

                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) activePillBg else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "navBg"
                    )

                    Box(
                        modifier = Modifier
                            .testTag("nav_${item.route}")
                            .clip(CircleShape)
                            .background(animatedBg)
                            .clickable { onNavigate(item) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.iconSelected else item.iconUnselected,
                                contentDescription = item.label,
                                tint = animatedIconTint,
                                modifier = Modifier.size(18.dp)
                            )

                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.label,
                                    color = activePillText,
                                    fontFamily = SpaceGrotesk,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

