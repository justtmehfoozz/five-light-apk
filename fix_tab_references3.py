import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # Replace usages with just Color.semantic...
    content = content.replace("androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "androidx.compose.ui.graphics.Color.semanticPrimaryAccent")
    
    # Wait, the error is at import statements:
    # `Unresolved reference 'semanticPrimaryAccent'` at `com.example.ui.theme.semanticPrimaryAccent` (probably an import)
    content = content.replace("import androidx.compose.ui.graphics.Color.semanticPrimaryAccent", "")
    content = content.replace("import androidx.compose.ui.graphics.Color.semanticBorder", "")
    content = content.replace("import androidx.compose.ui.graphics.Color.semanticSecondaryText", "")

    # Also fix tabIndicatorOffset: 
    # Let's import it properly.
    if "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset" not in content:
        content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset\nimport androidx.compose.runtime.Composable")
    
    with open(filename, 'w') as f:
        f.write(content)
