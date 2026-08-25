import re

with open('./app/src/main/java/com/example/ui/theme/Theme.kt', 'r') as f:
    content = f.read()

# For DarkColorScheme
content = re.sub(r'primary = TextPrimary,', 'primary = PrimaryAccentDark,', content)
content = re.sub(r'primaryContainer = SurfaceVariantDark,', 'primaryContainer = PrimaryAccentDark,', content) # or maybe keep SurfaceVariantDark? But usually primaryContainer is a tinted surface. Wait, they use primaryContainer for some cards. Let's keep it as is, or we can use the accent. Actually I'll leave primaryContainer alone if it's meant to be a surface.

# Let's check where primaryContainer is used.
# "HomeScreen.kt:2227: color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)"
# "AdhkarScreen.kt:106: color = if (isComplete) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else LightSurface," -> Wait, primaryContainer in Dark mode was SurfaceVariantDark. In Light mode it was SurfaceVariantLight (InactivePillBg #F2F0E9).
# I'll just change primary.

# For LightColorScheme
content = re.sub(r'primary = TextPrimaryLight,', 'primary = PrimaryAccentLight,', content)

with open('./app/src/main/java/com/example/ui/theme/Theme.kt', 'w') as f:
    f.write(content)
