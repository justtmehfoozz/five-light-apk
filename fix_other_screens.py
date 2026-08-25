import os
import re

def replace_in_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()

    # Ensure theme is imported
    if "import com.example.ui.theme.*" not in content and "import com.example.ui.theme.Light" not in content:
        if "import com.example.ui.theme." in content:
            content = re.sub(r'import com.example.ui.theme\.[a-zA-Z0-9_]+', r'import com.example.ui.theme.*', content, count=1)
        else:
            content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.ui.theme.*', content, count=1)

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)

# DockSearchOverlay.kt
replace_in_file("app/src/main/java/com/example/ui/components/DockSearchOverlay.kt", [
    ("Color(0xFDF8F3EC)", "LightBackground"),
    ("Color(0x3D231F1A)", "LightBorder.copy(alpha=0.35f)"),
    ("Color(0xFF1E1A17)", "LightPrimaryText"),
    ("Color(0x8C231F1A)", "LightMutedText.copy(alpha=0.8f)"),
    ("val accentGold = Color(0xFFD4AF37)", "val accentGold = if (isDark) com.example.ui.theme.PrimaryAccentDark else LightAccentGold")
])

# CalendarScreen.kt
replace_in_file("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", [
    ("val amberGold = Color(0xFFD4AF37)", "val amberGold = if (isDark) com.example.ui.theme.PrimaryAccentDark else LightAccentGold"),
    ("if (isDark) Color.White else Color(0xFF1E1A17)", "if (isDark) Color.White else LightCurrentBadgeBg"),
    ("if (isDark) Color(0xFF141312) else Color.White", "if (isDark) Color(0xFF141312) else LightCurrentBadgeText"),
    ("if (isDark) amberGold else Color(0xFFB8860B)", "if (isDark) amberGold else LightTodayRing"),
    ("Color(0xFFD6CFC2)", "LightBorder"),
    ("if (isDark) Color(0xFF141312) else Color(0xFFFFFFFF)", "if (isDark) Color(0xFF141312) else LightSurface"),
    ("if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)", "if (isDark) com.example.ui.theme.SuccessDark else com.example.ui.theme.SuccessLight"),
    ("if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000)", "if (isDark) Color(0x1AFFFFFF) else LightBorder.copy(alpha=0.4f)")
])

# QiblaScreen.kt
replace_in_file("app/src/main/java/com/example/ui/screens/QiblaScreen.kt", [
    ("val orangeAccent = Color(0xFFE5A126)", "val orangeAccent = if (isDark) com.example.ui.theme.PrimaryAccentDark else LightAccentGold"),
    ("if (isDark) Color(0xFFF5EFE6) else Color(0xFF231E18)", "if (isDark) Color(0xFFF5EFE6) else LightPrimaryText"),
    ("if (isDark) Color(0xFFAEA599) else Color(0xFF786F63)", "if (isDark) Color(0xFFAEA599) else LightMutedText"),
    ("val glowColor = if (isDark) orangeAccent else Color(0xFFD97706)", "val glowColor = if (isDark) orangeAccent else LightAccentGold"),
    ("if (isDark) primaryColor.copy(alpha = 0.95f) else Color(0xFF231E18).copy(alpha = 0.95f)", "if (isDark) primaryColor.copy(alpha = 0.95f) else LightPrimaryText.copy(alpha = 0.95f)"),
    ("val glowColor = Color(0xFFE5A126)", "val glowColor = if (isDark) com.example.ui.theme.PrimaryAccentDark else LightAccentGold"),
    ("val badgeBgColor = if (isDark) Color(0xFF1E1A16) else Color.White", "val badgeBgColor = if (isDark) Color(0xFF1E1A16) else LightSurface"),
    ("val kaabaBodyColor = if (isDark) Color(0xFFF3ECE2) else Color(0xFF231E18)", "val kaabaBodyColor = if (isDark) Color(0xFFF3ECE2) else LightPrimaryText"),
    ("val kiswahColor = Color(0xFFE5A126)", "val kiswahColor = if (isDark) com.example.ui.theme.PrimaryAccentDark else LightAccentGold")
])
