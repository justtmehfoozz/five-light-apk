const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', 'utf-8');

content = content.replace(/val textPrimary = if \(isDark\) Color\.semanticPrimaryText else MaterialTheme\.colorScheme\.onSurface/g, 'val textPrimary = Color.semanticPrimaryText');
content = content.replace(/val textSecondary = if \(isDark\) Color\.semanticSecondaryText else MaterialTheme\.colorScheme\.onSurfaceVariant/g, 'val textSecondary = Color.semanticSecondaryText');
content = content.replace(/val sectionHeaderColor = if \(isDark\) Color\.semanticMutedText else Color\(0xFF8B8881\)/g, 'val sectionHeaderColor = Color.semanticMutedText');
content = content.replace(/val cardBg = if \(isDark\) Color\.semanticSurface else Color\.semanticSurfaceLight/g, 'val cardBg = Color.semanticSurface');
content = content.replace(/val cardBorder = if \(isDark\) Color\.semanticBorder else Color\.semanticBorderLight/g, 'val cardBorder = Color.semanticBorder');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt', content);

