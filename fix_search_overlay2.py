import re

with open('./app/src/main/java/com/example/ui/components/DockSearchOverlay.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                            text = surah.nameArabic,
                            fontSize = 16.sp,
                            color = accentGold""",
"""                            text = surah.nameArabic,
                            fontSize = 16.sp,
                            color = textPrimary"""
)

content = content.replace(
"""                            text = "DUAS & SUPPLICATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = accentGold,""",
"""                            text = "DUAS & SUPPLICATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = textSecondary,"""
)

content = content.replace(
"""                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = accentGold,""",
"""                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = textSecondary,"""
)

content = content.replace(
"""                            text = "ADHKAR & TASBEEH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = accentGold,""",
"""                            text = "ADHKAR & TASBEEH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = textSecondary,"""
)

content = content.replace(
"""                            Icon(
                                imageVector = Icons.Filled.RadioButtonChecked,
                                contentDescription = null,
                                tint = accentGold,""",
"""                            Icon(
                                imageVector = Icons.Filled.RadioButtonChecked,
                                contentDescription = null,
                                tint = textSecondary,"""
)

content = content.replace(
"""                            text = dhikr.nameArabic,
                            fontSize = 16.sp,
                            color = accentGold""",
"""                            text = dhikr.nameArabic,
                            fontSize = 16.sp,
                            color = textPrimary"""
)

with open('./app/src/main/java/com/example/ui/components/DockSearchOverlay.kt', 'w') as f:
    f.write(content)
