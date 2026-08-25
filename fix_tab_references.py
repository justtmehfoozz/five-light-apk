import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # Replace com.example.ui.theme.semantic... with androidx.compose.ui.graphics.Color.semantic...
    content = content.replace("com.example.ui.theme.semanticPrimaryAccent", "androidx.compose.ui.graphics.Color.semanticPrimaryAccent")
    content = content.replace("com.example.ui.theme.semanticSecondaryText", "androidx.compose.ui.graphics.Color.semanticSecondaryText")
    content = content.replace("com.example.ui.theme.semanticBorder", "androidx.compose.ui.graphics.Color.semanticBorder")

    # The tabIndicatorOffset is an extension on Modifier, but needs to be imported or explicitly called on TabRowDefaults.
    # Wait, tabIndicatorOffset is in androidx.compose.material3.TabRowDefaults, so:
    # androidx.compose.material3.TabRowDefaults.tabIndicatorOffset(...) does not exist like that. It's an extension on Modifier:
    # Modifier.tabIndicatorOffset(...) which requires the import `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset`
    # We already added the import in the previous script. Let's just use `androidx.compose.ui.Modifier.tabIndicatorOffset`... wait, extension functions can't be called like that if they are not imported or if the receiver is explicitly typed.
    
    # Let's fix the tabIndicatorOffset call to use the imported extension properly.
    content = content.replace("androidx.compose.ui.Modifier.tabIndicatorOffset(", "androidx.compose.ui.Modifier.tabIndicatorOffset(")

    with open(filename, 'w') as f:
        f.write(content)
