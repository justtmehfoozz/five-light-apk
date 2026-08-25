import re

filename = 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt'
with open(filename, 'r') as f:
    content = f.read()

# Make indicator transparent
content = content.replace("color = Color.semanticPrimaryAccent\n                        )\n                    }\n                }",
"color = Color.Transparent\n                        )\n                    }\n                }")

# Fix Tab loop inside categories.forEachIndexed
# Search for the Tab block
content = re.sub(r'Tab\(\s*selected = selectedTab == index,\s*onClick = \{ selectedTab = index \},\s*selectedContentColor = Color\.semanticPrimaryAccent,',
"""Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    selectedContentColor = Color.semanticAccentForeground,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(if (selectedTab == index) Color.semanticPrimaryAccent else Color.Transparent),""", content)

with open(filename, 'w') as f:
    f.write(content)
