import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # Make it just Modifier.tabIndicatorOffset(...)
    content = content.replace("androidx.compose.ui.Modifier.tabIndicatorOffset", "androidx.compose.ui.Modifier.tabIndicatorOffset")
    # Wait, that's not right. I need it to be exactly Modifier.tabIndicatorOffset(...) and have the import.
    content = content.replace("androidx.compose.ui.Modifier.tabIndicatorOffset", "Modifier.tabIndicatorOffset")

    # Ensure import is present
    if "import androidx.compose.ui.Modifier" not in content:
        content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.ui.Modifier\nimport androidx.compose.runtime.Composable")
    if "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset" not in content:
        content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset\nimport androidx.compose.runtime.Composable")

    with open(filename, 'w') as f:
        f.write(content)
