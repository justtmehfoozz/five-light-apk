const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', 'utf-8');

content = content.replace(/val cardBg = if \(isDarkTheme\) Color\(0xFF20201E\) else Color\(0xFFFBFAF6\)/g, 'val cardBg = if (isDarkTheme) Color(0xFF20201E) else Color.semanticSurface');
content = content.replace(/val cardBg = if \(isDarkTheme\) Color\(0xFF1B1A19\) else MaterialTheme\.colorScheme\.surface/g, 'val cardBg = if (isDarkTheme) Color(0xFF1B1A19) else Color.semanticSurface');
content = content.replace(/val activeTrackColor = if \(isDarkTheme\) Color\(0xFF494556\) else Color\(0xFFEFEBE2\)/g, 'val activeTrackColor = if (isDarkTheme) Color(0xFF494556) else Color.semanticControl');

fs.writeFileSync('app/src/main/java/com/example/ui/components/HomeFeatureCards.kt', content);
