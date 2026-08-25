import re

with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                    }
                )""",
"""                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.semanticPrimaryAccent,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = Color.semanticPrimaryAccent
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(
                            color = Color.semanticBorder,
                            thickness = 1.dp
                        )
                    }
                )"""
)

# Replace Tab selectedContentColor/unselectedContentColor
content = content.replace(
"""                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "All Surahs (114)",
                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )""",
"""                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        selectedContentColor = Color.semanticPrimaryAccent,
                        unselectedContentColor = textSecondary,
                        text = {
                            Text(
                                "All Surahs (114)",
                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )"""
)
content = content.replace(
"""                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Bookmarks (${bookmarks.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )""",
"""                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        selectedContentColor = Color.semanticPrimaryAccent,
                        unselectedContentColor = textSecondary,
                        text = {
                            Text(
                                "Bookmarks (${bookmarks.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )"""
)
content = content.replace(
"""                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Planner",
                                fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )""",
"""                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        selectedContentColor = Color.semanticPrimaryAccent,
                        unselectedContentColor = textSecondary,
                        text = {
                            Text(
                                "Planner",
                                fontWeight = if (selectedTab == 2) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )"""
)


with open('./app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'w') as f:
    f.write(content)
