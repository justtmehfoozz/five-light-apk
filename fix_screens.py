import os
import re

directory = "app/src/main/java/com/example/ui/screens/"

# We will look for Color(0xFF...) and MaterialTheme usages that seem to be light-mode fallbacks
# Actually, the user says "search the entire codebase for hard-coded color values used by Light-mode UI components."
# And "Remove/consolidate one-off colors wherever they duplicate an existing semantic role."

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # We need to make sure we import the colors
    if "import com.example.ui.theme.*" not in content and "import com.example.ui.theme.Light" not in content:
        # Just add wildcard import to be safe if there are any com.example.ui.theme imports
        if "import com.example.ui.theme." in content:
            content = re.sub(r'import com.example.ui.theme\.[a-zA-Z0-9_]+', r'import com.example.ui.theme.*', content, count=1)
        else:
            content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport com.example.ui.theme.*")

    # The user wants us to replace any Light-mode specific hardcoded colors with tokens.
    # What are the common ones?
    # Color.White -> LightSurface (usually)
    # Color.Black -> LightPrimaryText (usually)
    # Color(0xFFF2EFE9) in light mode?
    # Let's just find all Color(...) and see which are in light mode branches.

    # TasbeehScreen specific:
    content = content.replace("Color(0xFF81C784) else MaterialTheme.colorScheme.primary", "Color(0xFF81C784) else LightAccentGold")
    content = content.replace("else MaterialTheme.colorScheme.primaryContainer", "else LightInactivePillBg")
    content = content.replace("else MaterialTheme.colorScheme.onPrimaryContainer", "else LightPrimaryText")
    content = content.replace("else MaterialTheme.colorScheme.primary", "else LightAccentGold")
    content = content.replace("else MaterialTheme.colorScheme.onPrimary", "else LightCurrentBadgeText")
    content = content.replace("else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)", "else LightMutedText.copy(alpha = 0.7f)")
    content = content.replace("else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)", "else LightMutedText.copy(alpha = 0.3f)")
    content = content.replace("else MaterialTheme.colorScheme.onSurfaceVariant", "else LightMutedText")
    content = content.replace("else MaterialTheme.colorScheme.onSurface", "else LightPrimaryText")
    content = content.replace("else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)", "else LightInactivePillBg.copy(alpha = 0.6f)")
    content = content.replace("else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)", "else LightInactivePillBg.copy(alpha = 0.5f)")
    content = content.replace("else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)", "else LightInactivePillBg.copy(alpha = 0.45f)")
    content = content.replace("else MaterialTheme.colorScheme.surfaceVariant", "else LightInactivePillBg")
    content = content.replace("else MaterialTheme.colorScheme.surface", "else LightSurface")
    content = content.replace("else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)", "else LightBorder.copy(alpha = 0.35f)")
    content = content.replace("else MaterialTheme.colorScheme.outline", "else LightBorder")

    # SettingsBottomSheet:
    content = content.replace("if (isDark) Color(0xFFF2EFE9) else Color(0xFF1A1815)", "if (isDark) Color(0xFFF2EFE9) else LightCurrentBadgeBg")
    content = content.replace("if (isDark) Color(0xFF1A1815) else Color(0xFFF2EFE9)", "if (isDark) Color(0xFF1A1815) else LightCurrentBadgeText")

    with open(filepath, 'w') as f:
        f.write(content)

for filename in os.listdir(directory):
    if filename.endswith(".kt"):
        process_file(os.path.join(directory, filename))

