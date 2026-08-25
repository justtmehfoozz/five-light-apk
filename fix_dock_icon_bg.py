import re

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

content = re.sub(r'val DockIconActiveBg = .*', 'val DockIconActiveBg = Color(0xFF494556)', content)

with open('./app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
