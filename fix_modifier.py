import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # It's an extension function. It needs to be called ON a Modifier.
    # So it should be `androidx.compose.ui.Modifier.tabIndicatorOffset(...)`
    content = content.replace("androidx.compose.material3.TabRowDefaults.tabIndicatorOffset", "androidx.compose.ui.Modifier.tabIndicatorOffset")
    
    with open(filename, 'w') as f:
        f.write(content)

