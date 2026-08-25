import re

filename = 'app/src/main/java/com/example/ui/screens/AdhkarScreen.kt'
with open(filename, 'r') as f:
    content = f.read()

# Make indicator transparent
content = content.replace("color = Color.semanticPrimaryAccent\n                    )\n                }\n            }",
"color = Color.Transparent\n                    )\n                }\n            }")

# Fix Tab 1
content = re.sub(r'Tab\(\s*selected = selectedTab == 0,\s*onClick = \{ selectedTab = 0 \},\s*selectedContentColor = Color\.semanticPrimaryAccent,',
"""Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                selectedContentColor = Color.semanticAccentForeground,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .background(if (selectedTab == 0) Color.semanticPrimaryAccent else Color.Transparent),""", content)

# Fix Tab 2
content = re.sub(r'Tab\(\s*selected = selectedTab == 1,\s*onClick = \{ selectedTab = 1 \},\s*selectedContentColor = Color\.semanticPrimaryAccent,',
"""Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                selectedContentColor = Color.semanticAccentForeground,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .background(if (selectedTab == 1) Color.semanticPrimaryAccent else Color.Transparent),""", content)

with open(filename, 'w') as f:
    f.write(content)
