import re

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'r') as f:
    content = f.read()

# We will replace from "val Color.Companion.semanticBackgroundLight" to "val Color.Companion.semanticWarning: Color"

start_idx = content.find("val Color.Companion.semanticBackgroundLight")
end_idx = content.find("val Color.Companion.semanticWarning: Color") + len("val Color.Companion.semanticWarning: Color @Composable get() = if (isAppInDarkTheme()) Color(0xFFFF9F0A) else Color(0xFFC93400)")

new_block = """val Color.Companion.semanticBackgroundLight: Color get() = Color(0xFFF5F3EC)
val Color.Companion.semanticSurfaceLight: Color get() = Color(0xFFFBFAF6)
val Color.Companion.semanticSurfaceElevatedLight: Color get() = Color(0xFFFFFDF8)
val Color.Companion.semanticControlLight: Color get() = Color(0xFFEFEBE2)
val Color.Companion.semanticPrimaryTextLight: Color get() = Color(0xFF1E1D1A)
val Color.Companion.semanticSecondaryTextLight: Color get() = Color(0xFF66635E)
val Color.Companion.semanticMutedTextLight: Color get() = Color(0xFF7A7771)
val Color.Companion.semanticBorderLight: Color get() = Color(0xFFD5D1C9)
val Color.Companion.semanticStrongBorderLight: Color get() = Color(0xFFC8C3B9)
val Color.Companion.semanticPrimaryAccentLight: Color get() = Color(0xFF8D6B1E)
val Color.Companion.semanticSuccessLight: Color get() = Color(0xFF248A3D)
val Color.Companion.semanticErrorLight: Color get() = Color(0xFFC62828)
val Color.Companion.semanticWarningLight: Color get() = Color(0xFFC93400)

val Color.Companion.semanticBackgroundDark: Color get() = Color(0xFF000000)
val Color.Companion.semanticSurfaceDark: Color get() = Color(0xFF1E1D1A)
val Color.Companion.semanticSurfaceElevatedDark: Color get() = Color(0xFF2C2C2E)
val Color.Companion.semanticControlDark: Color get() = Color(0xFF2C2C2E)
val Color.Companion.semanticPrimaryTextDark: Color get() = Color(0xFFFFFFFF)
val Color.Companion.semanticSecondaryTextDark: Color get() = Color(0xFF8E8E93)
val Color.Companion.semanticMutedTextDark: Color get() = Color(0xFF8E8E93)
val Color.Companion.semanticBorderDark: Color get() = Color(255, 255, 255, 20)
val Color.Companion.semanticStrongBorderDark: Color get() = Color(255, 255, 255, 30)
val Color.Companion.semanticPrimaryAccentDark: Color get() = Color(0xFF494556)
val Color.Companion.semanticSuccessDark: Color get() = Color(0xFF30D158)
val Color.Companion.semanticErrorDark: Color get() = Color(0xFFFF453A)
val Color.Companion.semanticWarningDark: Color get() = Color(0xFFFF9F0A)

fun getSemanticColor(isDark: Boolean, lightColor: Color, darkColor: Color): Color {
    return if (isDark) darkColor else lightColor
}

val Color.Companion.semanticBackground: Color @Composable get() = if (isAppInDarkTheme()) semanticBackgroundDark else semanticBackgroundLight
val Color.Companion.semanticSurface: Color @Composable get() = if (isAppInDarkTheme()) semanticSurfaceDark else semanticSurfaceLight
val Color.Companion.semanticSurfaceElevated: Color @Composable get() = if (isAppInDarkTheme()) semanticSurfaceElevatedDark else semanticSurfaceElevatedLight
val Color.Companion.semanticControl: Color @Composable get() = if (isAppInDarkTheme()) semanticControlDark else semanticControlLight
val Color.Companion.semanticPrimaryText: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryTextDark else semanticPrimaryTextLight
val Color.Companion.semanticSecondaryText: Color @Composable get() = if (isAppInDarkTheme()) semanticSecondaryTextDark else semanticSecondaryTextLight
val Color.Companion.semanticMutedText: Color @Composable get() = if (isAppInDarkTheme()) semanticMutedTextDark else semanticMutedTextLight
val Color.Companion.semanticBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticBorderDark else semanticBorderLight
val Color.Companion.semanticStrongBorder: Color @Composable get() = if (isAppInDarkTheme()) semanticStrongBorderDark else semanticStrongBorderLight
val Color.Companion.semanticPrimaryAccent: Color @Composable get() = if (isAppInDarkTheme()) semanticPrimaryAccentDark else semanticPrimaryAccentLight
val Color.Companion.semanticSuccess: Color @Composable get() = if (isAppInDarkTheme()) semanticSuccessDark else semanticSuccessLight
val Color.Companion.semanticError: Color @Composable get() = if (isAppInDarkTheme()) semanticErrorDark else semanticErrorLight
val Color.Companion.semanticWarning: Color @Composable get() = if (isAppInDarkTheme()) semanticWarningDark else semanticWarningLight"""

content = content[:start_idx] + new_block + content[end_idx:]

with open('app/src/main/java/com/example/ui/theme/Color.kt', 'w') as f:
    f.write(content)

