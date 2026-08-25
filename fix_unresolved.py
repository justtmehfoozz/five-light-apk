import os
import re

directory = './app/src/main/java/com/example/ui'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            
            # Fix Color.semantic* accesses to Color.semantic* (since it's an extension property, it needs to be imported or used correctly)
            # Actually, since they are extension properties on Color.Companion, using Color.semanticPrimaryAccent works,
            # BUT we need to make sure `Color` is imported from `androidx.compose.ui.graphics.Color`.
            # Also, some replacements might have resulted in invalid syntax like `if (isDark) Color.semanticSurface else Color.semanticSurfaceElevated.copy(alpha=0.35f)`
            # Or `Color.Color.semanticPrimaryAccent`
            
            content = content.replace('Color.Color.semantic', 'Color.semantic')
            content = content.replace('Color.Color', 'Color')
            content = content.replace('com.example.ui.theme.Color.semantic', 'Color.semantic')
            
            # Syntax error in QuranScreen.kt:1097: `if (isNightMode) if (isDark) com.example.ui.theme.SurfaceDark else com.example.ui.theme.SurfaceLight else LightInactivePillBg.copy(alpha = 0.35f)`
            # That replacement didn't work properly or there's a malformed if statement.
            
            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Updated {file}")
