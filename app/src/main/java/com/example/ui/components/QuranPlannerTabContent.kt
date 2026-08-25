package com.example.ui.components

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import androidx.compose.ui.graphics.Color


import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.SpaceGrotesk

@Composable
fun QuranPlannerTabContent(
    dailyQuranGoal: Int,
    onSetDailyGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showKhatmPlanner by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Goal Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Quran Goal",
                    fontFamily = SerifHeaderFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay consistent with your daily recitation.",
                    fontFamily = SpaceGrotesk,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (dailyQuranGoal > 0) {
                    Text(
                        text = "Current Goal: $dailyQuranGoal pages/day",
                        fontFamily = SpaceGrotesk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Button(
                    onClick = { showKhatmPlanner = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = androidx.compose.ui.graphics.Color.semanticAccentForeground
                    )
                ) {
                    Text("Khatm Planner", fontFamily = SpaceGrotesk)
                }
            }
        }
    }

    if (showKhatmPlanner) {
        KhatmPlannerSheet(
            dailyGoalPages = dailyQuranGoal,
            onSetDailyGoal = onSetDailyGoal,
            onDismiss = { showKhatmPlanner = false }
        )
    }
}
