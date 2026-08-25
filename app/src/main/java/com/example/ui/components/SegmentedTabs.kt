package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSecondaryText

@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "tab"
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
            label = "segmentedTabIndicator"
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) }
                        .semantics {
                            role = Role.Tab
                            this.selected = isSelected
                            stateDescription = if (isSelected) "$label, selected" else "$label, not selected"
                        }
                        .testTag("${testTagPrefix}_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SpaceGrotesk,
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
