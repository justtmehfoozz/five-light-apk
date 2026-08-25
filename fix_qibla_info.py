import re

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""        Text(
            text = formattedDistance,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = textColor
        )

        Text(
            text = "   •   ",
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.4f)
        )

        Text(
            text = formattedElevation,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = textColor
        )""",
"""        Text(
            text = formattedDistance,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = Color.semanticPrimaryText
        )

        Text(
            text = "   •   ",
            fontFamily = SpaceGrotesk,
            fontSize = 12.sp,
            color = Color.semanticMutedText
        )

        Text(
            text = formattedElevation,
            fontFamily = SpaceGrotesk,
            fontSize = 13.5.sp,
            color = Color.semanticMutedText
        )"""
)

# And the QiblaTechnicalData below it
content = content.replace(
"""private fun QiblaTechnicalData(
    qiblaHeading: Float,
    compassHeading: Float,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TechnicalValueCol(label = "Qibla", value = "${qiblaHeading.toInt()}°", color = textColor)
        TechnicalValueCol(label = "Heading", value = "${compassHeading.toInt()}°", color = textColor)
    }
}

@Composable
private fun TechnicalValueCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontFamily = SpaceGrotesk, fontSize = 11.sp, color = color.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
        Text(text = value, fontFamily = SpaceGrotesk, fontSize = 15.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}""",
"""private fun QiblaTechnicalData(
    qiblaHeading: Float,
    compassHeading: Float,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TechnicalValueCol(label = "Qibla", value = "${qiblaHeading.toInt()}°", color = Color.semanticPrimaryText, labelColor = Color.semanticMutedText)
        TechnicalValueCol(label = "Heading", value = "${compassHeading.toInt()}°", color = Color.semanticPrimaryText, labelColor = Color.semanticMutedText)
    }
}

@Composable
private fun TechnicalValueCol(label: String, value: String, color: Color, labelColor: Color = color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontFamily = SpaceGrotesk, fontSize = 11.sp, color = labelColor, fontWeight = FontWeight.Medium)
        Text(text = value, fontFamily = SpaceGrotesk, fontSize = 15.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}"""
)

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.write(content)
