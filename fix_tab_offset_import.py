import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # If we have `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset` it works when we do Modifier.tabIndicatorOffset
    content = content.replace("androidx.compose.ui.Modifier.tabIndicatorOffset", "Modifier.tabIndicatorOffset")
    
    with open(filename, 'w') as f:
        f.write(content)
