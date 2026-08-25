import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

# Remove the line 16 one.
content = content.replace("val Color.Companion.semanticSurfaceElevatedLight: Color get() = Color(0xFFF2F0E9)\n", "")

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
