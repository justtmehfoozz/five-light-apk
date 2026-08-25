import os
import re

directory = './app/src/main/java/com/example/ui'

replacements = [
    # Background
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF000000\)\s*else\s*LightBackground', 'Color.semanticBackground'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF000000\)\s*else\s*AppBackgroundLight', 'Color.semanticBackground'),
    
    # Surface
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF151515\)\s*else\s*LightSurface', 'Color.semanticSurface'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF151515\)\s*else\s*SurfaceLight', 'Color.semanticSurface'),
    
    # Surface Elevated
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF191919\)\s*else\s*LightSurface', 'Color.semanticSurfaceElevated'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF1A1A1A\)\s*else\s*LightSurface', 'Color.semanticSurfaceElevated'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF151515\)\s*else\s*LightInactivePillBg(?:\.copy\(alpha\s*=\s*[0-9.]+[fF]?\))?', 'Color.semanticSurfaceElevated'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF191919\)\s*else\s*LightInactivePillBg(?:\.copy\(alpha\s*=\s*[0-9.]+[fF]?\))?', 'Color.semanticSurfaceElevated'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF1A1A1A\)\s*else\s*LightInactivePillBg(?:\.copy\(alpha\s*=\s*[0-9.]+[fF]?\))?', 'Color.semanticSurfaceElevated'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\.Transparent\s*else\s*LightInactivePillBg(?:\.copy\(alpha\s*=\s*[0-9.]+[fF]?\))?', 'Color.Transparent'),
    
    # Primary Text
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFF5F2EA\)\s*else\s*LightPrimaryText', 'Color.semanticPrimaryText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFF5F2EA\)\s*else\s*TextPrimaryLight', 'Color.semanticPrimaryText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF111111\)\s*else\s*LightCurrentBadgeText', 'Color.semanticPrimaryText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFF5F2EA\)\s*else\s*LightCurrentBadgeText', 'Color.semanticPrimaryText'),
    
    # Muted Text
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF8F8B84\)\s*else\s*LightMutedText', 'Color.semanticMutedText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF66635E\)\s*else\s*LightMutedText', 'Color.semanticMutedText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF8F8B84\)\s*else\s*TextSecondaryLight', 'Color.semanticMutedText'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF66635E\)\s*else\s*TextTertiaryLight', 'Color.semanticMutedText'),
    
    # Border
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF3A3A3A\)\s*else\s*LightBorder', 'Color.semanticBorder'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF3A3A3A\)\s*else\s*BorderLight', 'Color.semanticBorder'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF3A3A3A\)\s*else\s*LightInactivePillBg', 'Color.semanticBorder'),
    
    # Accent
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFF5F2EA\)\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF5C526F\)\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF8F8B84\)\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFD4AF37\)\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*PrimaryAccentDark\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*PrimaryAccentDark\s*else\s*PrimaryAccentLight', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF1A1816\)\s*else\s*LightAccentGold', 'Color.semanticPrimaryAccent'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFFF5F2EA\)\s*else\s*PrimaryAccentLight', 'Color.semanticPrimaryAccent'),
    
    # Success
    (r'if\s*\(\w*isDarkTheme\w*\)\s*Color\(0xFF81C784\)\s*else\s*LightAccentGold', 'Color.semanticSuccess'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*SuccessDark\s*else\s*SuccessLight', 'Color.semanticSuccess'),
    
    # Error
    (r'if\s*\(\w*isDarkTheme\w*\)\s*ErrorDark\s*else\s*ErrorLight', 'Color.semanticError'),
    (r'if\s*\(\w*isDarkTheme\w*\)\s*ErrorDark\s*else\s*LightMissedRed', 'Color.semanticError'),
]

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            for pattern, repl in replacements:
                content = re.sub(pattern, repl, content)
                
            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Updated {file}")
