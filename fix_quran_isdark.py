import re

with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "r") as f:
    content = f.read()

# Fix QuranVerseCard
content = re.sub(
    r"(val cardBg = if \(isNightMode\) if \(isDark\) com\.example\.ui\.theme\.SurfaceDark else com\.example\.ui\.theme\.SurfaceLight else MaterialTheme\.colorScheme\.surface\n\s*val highlightOverlay = if \(isVerseActive\) if \(isDark\).*?\n\s*val textPrimary = if \(isNightMode\) if \(isDark\).*?\n\s*)(val isDark = isNightMode \|\| MaterialTheme\.colorScheme\.background\.run \{ \(red \+ green \+ blue\) < 1\.5f \})",
    r"\2\n    \1",
    content
)

# Fix BismillahHeader (Needs isDark added)
content = re.sub(
    r"(modifier: Modifier = Modifier\n\) {\n)(\s*)(val cardBg = if \(isNightMode\) if \(isDark\).*?)",
    r"\1\2val isDark = isNightMode || androidx.compose.material3.MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }\n\2\3",
    content
)

# Fix SurahSkeletonCard
content = re.sub(
    r"(val cardBg = if \(isNightMode\) if \(isDark\).*?\n\s*)(val isDark = isNightMode \|\| androidx\.compose\.material3\.MaterialTheme\.colorScheme\.background\.run \{ \(red \* 0\.299f \+ green \* 0\.587f \+ blue \* 0\.114f\) < 0\.5f \})",
    r"\2\n    \1",
    content
)

with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "w") as f:
    f.write(content)
