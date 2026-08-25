import os
import re

directory = './app/src/main/java/com/example/ui'

# Complex patterns for if/else blocks:
# e.g. if (isDark) com.example.ui.theme.PrimaryAccentDark else com.example.ui.theme.LightAccentGold
# e.g. if (isNightMode) if (isDark) ... else ... else ...

patterns = [
    (r'if\s*\([^)]*\)\s*(?:com\.example\.ui\.theme\.)?PrimaryAccentDark\s*else\s*(?:com\.example\.ui\.theme\.)?(?:LightAccentGold|PrimaryAccentLight)', 'Color.semanticPrimaryAccent'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightAccentGold(?!\.copy)', 'Color.semanticPrimaryAccent'),
    (r'if\s*\([^)]*\)\s*(?:com\.example\.ui\.theme\.)?SuccessDark\s*else\s*(?:com\.example\.ui\.theme\.)?SuccessLight', 'Color.semanticSuccess'),
    (r'if\s*\([^)]*\)\s*(?:com\.example\.ui\.theme\.)?ErrorDark\s*else\s*(?:com\.example\.ui\.theme\.)?(?:ErrorLight|LightMissedRed)', 'Color.semanticError'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightCurrentBadgeText', 'Color.semanticPrimaryText'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightCurrentBadgeBg', 'Color.semanticSurfaceElevated'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightTodayRing', 'Color.semanticPrimaryAccent'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightBorder(?!\.copy)', 'Color.semanticBorder'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightSurface(?!\.copy)', 'Color.semanticSurface'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightPrimaryText(?!\.copy)', 'Color.semanticPrimaryText'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightMutedText(?!\.copy)', 'Color.semanticMutedText'),
    (r'if\s*\([^)]*\)\s*Color\([^)]*\)\s*else\s*(?:com\.example\.ui\.theme\.)?LightBackground(?!\.copy)', 'Color.semanticBackground'),
    
    # Text colors
    (r'if\s*\([^)]*\)\s*(?:if\s*\([^)]*\)\s*)?(?:com\.example\.ui\.theme\.)?TextPrimary\s*else\s*(?:com\.example\.ui\.theme\.)?TextPrimaryLight(?:\s*else\s*LightPrimaryText)?', 'Color.semanticPrimaryText'),
    (r'if\s*\([^)]*\)\s*(?:if\s*\([^)]*\)\s*)?(?:com\.example\.ui\.theme\.)?TextSecondary\s*else\s*(?:com\.example\.ui\.theme\.)?TextSecondaryLight(?:\s*else\s*LightMutedText)?', 'Color.semanticMutedText'),
    (r'if\s*\([^)]*\)\s*(?:if\s*\([^)]*\)\s*)?(?:com\.example\.ui\.theme\.)?BorderDark\s*else\s*(?:com\.example\.ui\.theme\.)?BorderLight(?:\s*else\s*LightBorder)?', 'Color.semanticBorder'),
    (r'if\s*\([^)]*\)\s*(?:if\s*\([^)]*\)\s*)?(?:com\.example\.ui\.theme\.)?SurfaceDark\s*else\s*(?:com\.example\.ui\.theme\.)?SurfaceLight(?:\s*else\s*LightSurface)?', 'Color.semanticSurface'),
]

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            for pattern, repl in patterns:
                content = re.sub(pattern, repl, content)
                
            # Direct replacements for any remaining isolated occurrences:
            content = content.replace('com.example.ui.theme.LightAccentGold', 'Color.semanticPrimaryAccent')
            content = content.replace('LightAccentGold', 'Color.semanticPrimaryAccent')
            content = content.replace('LightMissedRed', 'Color.semanticError')
            content = content.replace('LightCurrentBadgeBg', 'Color.semanticSurfaceElevated')
            content = content.replace('LightCurrentBadgeText', 'Color.semanticPrimaryText')
            content = content.replace('LightTodayRing', 'Color.semanticPrimaryAccent')
            content = content.replace('LightInactivePillBg', 'Color.semanticSurfaceElevated')
            content = content.replace('PrimaryAccentDark', 'Color.semanticPrimaryAccent')
            
            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Updated {file}")
