const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'utf-8');

content = content.replace(/val targetDockBorderColor = if \(isSearchActive\) Color\.semanticPrimaryAccent else Color\.semanticDockBorder/g, `val targetDockBorderColor = if (isSearchActive) { if (isDark) Color.semanticPrimaryAccent else Color.semanticDockBorder } else Color.semanticDockBorder`);
content = content.replace(/tint = if \(isDark\) androidx\.compose\.ui\.graphics\.Color\(0xFFE5E2DC\) else androidx\.compose\.ui\.graphics\.Color\.semanticPrimaryAccent/g, `tint = if (isDark) androidx.compose.ui.graphics.Color(0xFFE5E2DC) else Color.semanticDockBorder`);

fs.writeFileSync('app/src/main/java/com/example/ui/components/BottomNavBar.kt', content);
