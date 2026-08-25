import re

def fix_quran_tab():
    with open('app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'r') as f:
        content = f.read()
    
    content = content.replace("selectedContentColor = Color.semanticPrimaryText,", "selectedContentColor = Color.semanticPrimaryAccent,")
    content = content.replace("color = Color.semanticPrimaryText\n                            )", "color = Color.semanticPrimaryAccent\n                            )")
    
    with open('app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'w') as f:
        f.write(content)

def fix_dua_tab():
    with open('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', 'r') as f:
        content = f.read()

    replacement = """        ScrollableTabRow(
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
        }"""
    
    # We will regex replace the ScrollableTabRow block
    content = re.sub(r'ScrollableTabRow\(.*?\) \{(?:.*?Tab\(.*?\).*?)+\}', replacement, content, flags=re.DOTALL)
    
    with open('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', 'w') as f:
        f.write(content)

def fix_adhkar_tab():
    with open('app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'r') as f:
        content = f.read()

    replacement = """        TabRow(
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
        }"""
    
    content = re.sub(r'TabRow\(selectedTabIndex = selectedTab\) \{.*?\}', replacement, content, flags=re.DOTALL)
    
    with open('app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'w') as f:
        f.write(content)


fix_quran_tab()
fix_dua_tab()
fix_adhkar_tab()
