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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatmPlannerSheet(
    dailyGoalPages: Int,
    onSetDailyGoal: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var targetDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    val totalPages = 604 // standard Medina Mushaf pages
    
    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), targetDate).coerceAtLeast(1)
    val pagesPerDay = (totalPages / daysRemaining.toFloat()).let { Math.ceil(it.toDouble()).toInt() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Khatm Planner & Goals",
                fontFamily = SerifHeaderFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Set a goal to complete the Quran.",
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target Date", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(targetDate.toString(), fontFamily = SpaceGrotesk, fontSize = 16.sp)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { targetDate = targetDate.plusDays(7) }, modifier = Modifier.weight(1f)) {
                    Text("+1 Week")
                }
                OutlinedButton(onClick = { targetDate = targetDate.plusMonths(1) }, modifier = Modifier.weight(1f)) {
                    Text("+1 Month")
                }
                OutlinedButton(onClick = { targetDate = targetDate.minusDays(7).coerceAtLeast(LocalDate.now().plusDays(1)) }, modifier = Modifier.weight(1f)) {
                    Text("-1 Week")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Required Reading", fontFamily = SpaceGrotesk, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = "$pagesPerDay pages/day",
                        fontFamily = SerifHeaderFont,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    onSetDailyGoal(pagesPerDay)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.semanticPrimaryAccent,
                    contentColor = androidx.compose.ui.graphics.Color.semanticAccentForeground
                )
            ) {
                Text("Set as Daily Goal", fontFamily = SpaceGrotesk)
            }
        }
    }
}
