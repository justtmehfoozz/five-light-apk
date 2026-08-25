import re

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    content = f.read()

# Make QiblaStatus use the appropriate color. If facing qibla, use success color.
# statusText is passed to QiblaStatus.
content = content.replace(
"""                QiblaStatus(
                    statusText = statusText,
                    orangeAccent = orangeAccent
                )""",
"""                QiblaStatus(
                    statusText = statusText,
                    orangeAccent = orangeAccent,
                    successColor = Color.semanticSuccess
                )"""
)

content = content.replace(
"""private fun QiblaStatus(
    statusText: String,
    orangeAccent: Color
)""",
"""private fun QiblaStatus(
    statusText: String,
    orangeAccent: Color,
    successColor: Color
)"""
)

content = content.replace(
"""        Text(
            text = text,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 21.sp,
            color = orangeAccent,""",
"""        Text(
            text = text,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 21.sp,
            color = if (text == "Facing Qibla") successColor else orangeAccent,"""
)

# QiblaInformation
# The distance and elevation
content = content.replace(
"""private fun QiblaInformation(
    distanceKm: Int,
    elevationMeters: Double,
    textColor: Color
) {
    Text(
        text = "$distanceKm km to Kaaba",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = textColor,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = "${elevationMeters.toInt()} m above sea level",
        style = MaterialTheme.typography.bodyMedium,
        color = textColor.copy(alpha = 0.65f),
        textAlign = TextAlign.Center
    )
}""",
"""private fun QiblaInformation(
    distanceKm: Int,
    elevationMeters: Double,
    textColor: Color
) {
    Text(
        text = "$distanceKm km to Kaaba",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = Color.semanticPrimaryText,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = "${elevationMeters.toInt()} m above sea level",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.semanticMutedText,
        textAlign = TextAlign.Center
    )
}"""
)

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.write(content)
