import re

filename = 'app/src/main/java/com/example/ui/theme/Color.kt'
with open(filename, 'r') as f:
    content = f.read()

# Fix Dock Icon Active Background
content = re.sub(r'val Color\.Companion\.semanticDockIconActiveBgLight: Color get\(\) = Color\(0xFF8D6B1E\)',
                 'val Color.Companion.semanticDockIconActiveBgLight: Color get() = Color(0xFF1E1D1A)', content)

content = re.sub(r'val Color\.Companion\.semanticDockIconActiveBgDark: Color get\(\) = Color\(0xFF494556\)',
                 'val Color.Companion.semanticDockIconActiveBgDark: Color get() = Color(0xFF5A5566)', content) # Soft tinted neutral

# Ensure we have dockActiveIndicator explicitly as requested
if "dockActiveIndicatorLight" not in content:
    content = content.replace("val Color.Companion.semanticDockIconActiveBgDark",
                              "val Color.Companion.dockActiveIndicatorLight: Color get() = Color(0xFF1E1D1A)\nval Color.Companion.dockActiveIndicatorDark: Color get() = Color(0xFFD9D6DF)\nval Color.Companion.semanticDockIconActiveBgDark")

with open(filename, 'w') as f:
    f.write(content)
