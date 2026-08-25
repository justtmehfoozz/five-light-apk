import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

# Force activeIconColor to pure White
content = content.replace("val activeIconColor = Color.semanticDockIconActive", "val activeIconColor = androidx.compose.ui.graphics.Color.White")

with open(filename, 'w') as f:
    f.write(content)
