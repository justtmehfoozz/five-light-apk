import re

files = [
    'app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 
    'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt'
]

imports_to_add = [
    "import com.example.ui.theme.semanticPrimaryAccent",
    "import com.example.ui.theme.semanticSecondaryText",
    "import com.example.ui.theme.semanticBorder",
    "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset"
]

for filename in files:
    with open(filename, 'r') as f:
        content = f.read()

    for imp in imports_to_add:
        if imp not in content:
            content = content.replace("import androidx.compose.runtime.Composable", imp + "\nimport androidx.compose.runtime.Composable")
    
    # We replaced 'androidx.compose.ui.graphics.Color.semanticPrimaryAccent' earlier to the same thing, but it's supposed to be `Color.semanticPrimaryAccent`
    content = content.replace("androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "Color.semanticPrimaryAccent")
    content = content.replace("androidx.compose.ui.graphics.Color.semanticSecondaryText", "Color.semanticSecondaryText")
    content = content.replace("androidx.compose.ui.graphics.Color.semanticBorder", "Color.semanticBorder")
    
    with open(filename, 'w') as f:
        f.write(content)
