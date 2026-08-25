import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # Make it use full package name inline
    content = content.replace("Modifier.tabIndicatorOffset", "androidx.compose.material3.TabRowDefaults.tabIndicatorOffset")
    content = content.replace("androidx.compose.ui.androidx.compose.material3", "androidx.compose.material3") # cleanup
    
    with open(filename, 'w') as f:
        f.write(content)

