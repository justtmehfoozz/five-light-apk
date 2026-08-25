const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/CalendarScreen.kt', 'utf-8');

content = content.replace(/val cardBorder = MaterialTheme\.colorScheme\.outline/g, 'val cardBorder = Color.semanticBorder');
content = content.replace(/if \(isDark\) Color\.semanticSurface else cardBg/g, 'Color.semanticSurface');
content = content.replace(/if \(isDark\) Color\.semanticBorder else cardBorder/g, 'Color.semanticBorder');
content = content.replace(/if \(isDark\) Color\.semanticSecondaryText else MaterialTheme\.colorScheme\.onSurfaceVariant/g, 'Color.semanticSecondaryText');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryAccent\.copy\(alpha = 0\.15f\) else MaterialTheme\.colorScheme\.primary\.copy\(alpha = 0\.12f\)/g, 'Color.semanticPrimaryAccent.copy(alpha = 0.15f)');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryAccent else MaterialTheme\.colorScheme\.primary/g, 'Color.semanticPrimaryAccent');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryText else MaterialTheme\.colorScheme\.onSurface/g, 'Color.semanticPrimaryText');
content = content.replace(/if \(isDark\) Color\.semanticSecondaryText else MaterialTheme\.colorScheme\.primary/g, 'Color.semanticSecondaryText');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryText else Color\.semanticPrimaryAccent/g, 'Color.semanticPrimaryText');
content = content.replace(/if \(isDark\) Color\.semanticBorder else MaterialTheme\.colorScheme\.outline/g, 'Color.semanticBorder');
content = content.replace(/if \(isDark\) Color\.semanticSurfaceElevated else MaterialTheme\.colorScheme\.surface/g, 'Color.semanticSurfaceElevated');
content = content.replace(/if \(isDark\) Color\.semanticBorder else MaterialTheme\.colorScheme\.outlineVariant/g, 'Color.semanticBorder');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryAccent\.copy\(alpha = 0\.08f\) else LightBorder\.copy\(alpha=0\.4f\)/g, 'Color.semanticPrimaryAccent.copy(alpha = 0.08f)');
content = content.replace(/if \(isDark\) Color\.semanticPrimaryAccent else MaterialTheme\.colorScheme\.onSurfaceVariant/g, 'Color.semanticPrimaryAccent');
content = content.replace(/if \(isDark\) Color\(0xFFFFFFFF\) else MaterialTheme\.colorScheme\.primary/g, 'Color(0xFFFFFFFF)');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/CalendarScreen.kt', content);
