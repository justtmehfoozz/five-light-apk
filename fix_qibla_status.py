import re

filename = 'app/src/main/java/com/example/ui/screens/QiblaScreen.kt'
with open(filename, 'r') as f:
    content = f.read()

qibla_status_replacement = """@Composable
private fun QiblaStatus(
    statusText: String,
    orangeAccent: Color,
    successColor: Color
) {
    Crossfade(
        targetState = statusText,
        animationSpec = tween(180),
        label = "statusFade"
    ) { text ->
        val bgColor = if (text == "Facing Qibla") successColor else orangeAccent
        val textColor = if (text == "Facing Qibla") Color.White else Color.semanticAccentForeground
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = text,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 21.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}"""

content = re.sub(r'@Composable\nprivate fun QiblaStatus\(.*?\n\}', qibla_status_replacement, content, flags=re.DOTALL)

with open(filename, 'w') as f:
    f.write(content)
