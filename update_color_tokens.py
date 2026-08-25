import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

# Update semantic properties
content = re.sub(r'val Color.Companion.semanticBackgroundLight: Color get\(\) = Color\(0xFFF7F6F1\)',
                 'val Color.Companion.semanticBackgroundLight: Color get() = Color(0xFFF5F3EC)', content)

content = re.sub(r'val Color.Companion.semanticSurfaceLight: Color get\(\) = Color\(0xFFFFFFFF\)',
                 'val Color.Companion.semanticSurfaceLight: Color get() = Color(0xFFFBFAF6)', content)

# Update semantic text colors
content = re.sub(r'val Color.Companion.semanticPrimaryTextLight: Color get\(\) = Color\(0xFF1E1D1A\)',
                 'val Color.Companion.semanticPrimaryTextLight: Color get() = Color(0xFF1E1D1A)', content)

content = re.sub(r'val Color.Companion.semanticSecondaryTextLight: Color get\(\) = Color\(0xFF66635E\)',
                 'val Color.Companion.semanticSecondaryTextLight: Color get() = Color(0xFF66635E)', content)

content = re.sub(r'val Color.Companion.semanticMutedTextLight: Color get\(\) = Color\(0xFF66635E\)',
                 'val Color.Companion.semanticMutedTextLight: Color get() = Color(0xFF7A7771)', content)

# semanticBorderLight -> #D5D1C9 (already)
# We will add semanticStrongBorderLight -> #C8C3B9

content = re.sub(r'val Color.Companion.semanticBorderLight: Color get\(\) = Color\(0xFFD5D1C9\)',
                 'val Color.Companion.semanticBorderLight: Color get() = Color(0xFFD5D1C9)\nval Color.Companion.semanticStrongBorderLight: Color get() = Color(0xFFC8C3B9)\nval Color.Companion.semanticSurfaceElevatedLight: Color get() = Color(0xFFFFFDF8)\nval Color.Companion.semanticControlLight: Color get() = Color(0xFFEFEBE2)', content)

# update semantic getters
content = re.sub(r'val Color.Companion.semanticBackground: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(0xFF000000\) else Color\(0xFFF7F6F1\)',
                 'val Color.Companion.semanticBackground: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF000000) else Color.semanticBackgroundLight', content)

content = re.sub(r'val Color.Companion.semanticSurface: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(0xFF1E1D1A\) else Color\(0xFFFFFFFF\)',
                 'val Color.Companion.semanticSurface: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF1E1D1A) else Color.semanticSurfaceLight', content)

content = re.sub(r'val Color.Companion.semanticPrimaryText: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(0xFFFFFFFF\) else Color\(0xFF1E1D1A\)',
                 'val Color.Companion.semanticPrimaryText: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFFFFFFFF) else Color.semanticPrimaryTextLight', content)

content = re.sub(r'val Color.Companion.semanticSecondaryText: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(0xFF8E8E93\) else Color\(0xFF66635E\)',
                 'val Color.Companion.semanticSecondaryText: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF8E8E93) else Color.semanticSecondaryTextLight', content)

content = re.sub(r'val Color.Companion.semanticMutedText: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(0xFF8E8E93\) else Color\(0xFF66635E\)',
                 'val Color.Companion.semanticMutedText: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF8E8E93) else Color.semanticMutedTextLight', content)

content = re.sub(r'val Color.Companion.semanticBorder: Color @Composable get\(\) = if \(isAppInDarkTheme\(\)\) Color\(255, 255, 255, 20\) else Color\(0xFFD5D1C9\)',
                 'val Color.Companion.semanticBorder: Color @Composable get() = if (isAppInDarkTheme()) Color(255, 255, 255, 20) else Color.semanticBorderLight', content)

# Check and fix SurfaceElevated
if "semanticSurfaceElevated" not in content:
    content = content.replace("val Color.Companion.semanticStrongBorder",
                              "val Color.Companion.semanticSurfaceElevated: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF2C2C2E) else Color.semanticSurfaceElevatedLight\nval Color.Companion.semanticControl: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFF2C2C2E) else Color.semanticControlLight\nval Color.Companion.semanticStrongBorder")

# Update Light Mode Semantic Tokens (SINGLE SOURCE OF TRUTH)
content = content.replace('val LightBackground = Color(0xFFF7F6F1)', 'val LightBackground = Color(0xFFF5F3EC)')
content = content.replace('val LightSurface = Color(0xFFFFFFFF)', 'val LightSurface = Color(0xFFFBFAF6)')
content = content.replace('val LightPrimaryText = Color(0xFF1E1D1A)', 'val LightPrimaryText = Color(0xFF1E1D1A)')
content = content.replace('val LightMutedText = Color(0xFF66635E)', 'val LightMutedText = Color(0xFF7A7771)')
content = content.replace('val LightInactivePillBg = Color(0xFFF2F0E9)', 'val LightInactivePillBg = Color(0xFFEFEBE2)')
# keep LightBorder #D5D1C9

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)
