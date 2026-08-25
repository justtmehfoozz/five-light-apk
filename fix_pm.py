with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "a") as f:
    f.write("""
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrayerModeModal(
    prayer: com.example.data.model.PrayerItem,
    onDismiss: () -> Unit,
    onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Prayer Mode: ${prayer.name.name.lowercase().replaceFirstChar { it.uppercase() }}",
                fontFamily = com.example.ui.theme.SerifHeaderFont,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Time: ${prayer.timeFormatted}",
                fontFamily = com.example.ui.theme.SpaceGrotesk,
                fontSize = 16.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = {
                    onQiblaClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Find Qibla", fontFamily = com.example.ui.theme.SpaceGrotesk)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
""")
