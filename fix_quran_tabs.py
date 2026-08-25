import re

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),""",
"""                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.semanticPrimaryAccent,
                        unfocusedBorderColor = Color.semanticBorder,
                        focusedTextColor = Color.semanticPrimaryText,
                        unfocusedTextColor = Color.semanticPrimaryText,
                        focusedLeadingIconColor = Color.semanticPrimaryAccent,
                        unfocusedLeadingIconColor = Color.semanticSecondaryText,
                        focusedPlaceholderColor = Color.semanticMutedText,
                        unfocusedPlaceholderColor = Color.semanticMutedText
                    ),"""
)

content = content.replace(
"""                    contentColor = Color.semanticPrimaryAccent,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = Color.semanticPrimaryAccent
                            )
                        }
                    },""",
"""                    contentColor = Color.semanticPrimaryText,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = Color.semanticPrimaryText
                            )
                        }
                    },"""
)
content = content.replace(
"""                        selectedContentColor = Color.semanticPrimaryAccent,
                        unselectedContentColor = Color.semanticMutedText,""",
"""                        selectedContentColor = Color.semanticPrimaryText,
                        unselectedContentColor = Color.semanticSecondaryText,"""
)
content = content.replace(
"""                        selectedContentColor = Color.semanticPrimaryAccent,
                        unselectedContentColor = Color.semanticSecondaryText,""",
"""                        selectedContentColor = Color.semanticPrimaryText,
                        unselectedContentColor = Color.semanticSecondaryText,"""
)


with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'w') as f:
    f.write(content)
