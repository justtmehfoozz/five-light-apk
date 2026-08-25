import re

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                        Text(
                            text = "${selectedSurah.nameArabic} • ${selectedSurah.versesCount} Verses",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )""",
"""                        Text(
                            text = "${selectedSurah.nameArabic} • ${selectedSurah.versesCount} Verses",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.semanticMutedText
                        )"""
)

content = content.replace(
"""                        IconButton(onClick = onToggleEnglish) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Toggle Translation",
                                tint = if (showEnglishTranslation) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = { showFontSizeControls = !showFontSizeControls }) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Text Size",
                                tint = if (showFontSizeControls) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.5f)
                            )
                        }""",
"""                        IconButton(onClick = onToggleEnglish) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Toggle Translation",
                                tint = if (showEnglishTranslation) Color.semanticPrimaryAccent else Color.semanticMutedText
                            )
                        }
                        IconButton(onClick = { showFontSizeControls = !showFontSizeControls }) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Text Size",
                                tint = if (showFontSizeControls) Color.semanticPrimaryAccent else Color.semanticMutedText
                            )
                        }"""
)

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'w') as f:
    f.write(content)
