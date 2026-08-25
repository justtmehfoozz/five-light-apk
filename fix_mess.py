import re

with open('app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'r') as f:
    content = f.read()

bad_block = """        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = com.example.ui.theme.semanticPrimaryAccent,
            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    modifier = androidx.compose.material3.TabRowDefaults.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = com.example.ui.theme.semanticPrimaryAccent
                )
            },
            divider = { androidx.compose.material3.HorizontalDivider(color = com.example.ui.theme.semanticBorder) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                text = { Text("Morning") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                text = { Text("Evening") }
            )
        },
                text = { Text("Morning") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Evening") }
            )
        }"""

good_block = """        androidx.compose.material3.TabRow(
            selectedTabIndex = selectedTab,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = com.example.ui.theme.semanticPrimaryAccent,
            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    modifier = androidx.compose.ui.Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = com.example.ui.theme.semanticPrimaryAccent
                )
            },
            divider = { androidx.compose.material3.HorizontalDivider(color = com.example.ui.theme.semanticBorder) }
        ) {
            androidx.compose.material3.Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                text = { Text("Morning") }
            )
            androidx.compose.material3.Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                text = { Text("Evening") }
            )
        }"""

content = content.replace(bad_block, good_block)

# Add import for tabIndicatorOffset if missing
if "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset\nimport androidx.compose.runtime.Composable")

with open('app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', 'r') as f:
    content2 = f.read()

bad_block2 = """        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = com.example.ui.theme.semanticPrimaryAccent,
            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    modifier = androidx.compose.material3.TabRowDefaults.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                    height = 3.dp,
                    color = com.example.ui.theme.semanticPrimaryAccent
                )
            },
            divider = { androidx.compose.material3.HorizontalDivider(color = com.example.ui.theme.semanticBorder) }
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                    unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                    text = { Text(category) }
                )
            }
        }
                )
            }
        }"""

good_block2 = """        androidx.compose.material3.ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = com.example.ui.theme.semanticPrimaryAccent,
            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    modifier = androidx.compose.ui.Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                    height = 3.dp,
                    color = com.example.ui.theme.semanticPrimaryAccent
                )
            },
            divider = { androidx.compose.material3.HorizontalDivider(color = com.example.ui.theme.semanticBorder) }
        ) {
            categories.forEachIndexed { index, category ->
                androidx.compose.material3.Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    selectedContentColor = com.example.ui.theme.semanticPrimaryAccent,
                    unselectedContentColor = com.example.ui.theme.semanticSecondaryText,
                    text = { Text(category) }
                )
            }
        }"""

content2 = content2.replace(bad_block2, good_block2)

if "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset" not in content2:
    content2 = content2.replace("import androidx.compose.runtime.Composable", "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset\nimport androidx.compose.runtime.Composable")

with open('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', 'w') as f:
    f.write(content2)

