import re

filename = 'app/src/main/java/com/example/ui/components/BottomNavBar.kt'
with open(filename, 'r') as f:
    content = f.read()

content = re.sub(r'val targetDockBg = if \(isDark\) \{.*?\} else \{.*?\}', 
                 'val targetDockBg = if (isSearchActive) Color.semanticControl else Color.semanticDockBackground', 
                 content, flags=re.DOTALL)

content = re.sub(r'val targetDockBorderColor = if \(isDark\) \{.*?\} else \{.*?\}', 
                 'val targetDockBorderColor = if (isSearchActive) Color.semanticPrimaryAccent else Color.semanticDockBorder', 
                 content, flags=re.DOTALL)

content = content.replace("val activeHighlightBg = if (isDark) Color.semanticDockIconActiveBg else Color.semanticDockIconActiveBg",
                          "val activeHighlightBg = Color.semanticDockIconActiveBg")

content = content.replace("val activeIconColor = if (isDark) Color.semanticDockIconActive else Color.semanticDockBackground",
                          "val activeIconColor = Color.semanticDockIconActive")

content = content.replace("val inactiveIconColor = if (isDark) Color.semanticDockIconInactive else Color.semanticDockIconInactive",
                          "val inactiveIconColor = Color.semanticDockIconInactive")

with open(filename, 'w') as f:
    f.write(content)
