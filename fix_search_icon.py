import re

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.ui.graphics.Color(0xFFB8A7E8)", "androidx.compose.ui.graphics.Color(0xFF494556)")

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'w') as f:
    f.write(content)
