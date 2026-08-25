import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # The actual way to fix it is to ensure the import is exactly right.
    # We should have `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset`
    if "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset" not in content:
        content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.material3.TabRowDefaults.tabIndicatorOffset")
    
    with open(filename, 'w') as f:
        f.write(content)

