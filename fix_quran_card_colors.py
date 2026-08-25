import re

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (verse.verseNumber == 0) "Bismillah" else "Verse ${verse.verseNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }""",
"""                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (verse.verseNumber == 0) "Bismillah" else "Verse ${verse.verseNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.semanticPrimaryAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }"""
)

content = content.replace(
"""                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Quran Lens",
                                tint = MaterialTheme.colorScheme.primary
                            )""",
"""                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Quran Lens",
                                tint = textPrimary
                            )"""
)

content = content.replace(
"""                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play Audio",
                                tint = MaterialTheme.colorScheme.primary
                            )""",
"""                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play Audio",
                                tint = textPrimary
                            )"""
)

content = content.replace(
"""                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else textPrimary.copy(alpha = 0.6f)
                            )""",
"""                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color.semanticPrimaryAccent else Color.semanticMutedText
                            )"""
)

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'w') as f:
    f.write(content)
