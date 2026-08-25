import re

with open('./app/src/main/java/com/example/ui/components/DockSearchOverlay.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${surah.number}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            }""",
"""                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.semanticSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${surah.number}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.semanticPrimaryText
                                )
                            }"""
)

content = content.replace(
"""                            text = if (searchQuery.isBlank()) "POPULAR SURAHS" else "QURAN SURAHS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = accentGold,""",
"""                            text = if (searchQuery.isBlank()) "POPULAR SURAHS" else "QURAN SURAHS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color.semanticMutedText,"""
)

with open('./app/src/main/java/com/example/ui/components/DockSearchOverlay.kt', 'w') as f:
    f.write(content)
