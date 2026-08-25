const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf-8');

content = content.replace(/val LightBackground = Color\(0xFFF5F3EC\)/g, 'val LightBackground = Color(0xFFF4F1EA)');
content = content.replace(/val LightSurface = Color\(0xFFFBFAF6\)/g, 'val LightSurface = Color(0xFFFDFBF7)');
content = content.replace(/val LightInactivePillBg = Color\(0xFFEFEBE2\)/g, 'val LightInactivePillBg = Color(0xFFE8E4DA)');
content = content.replace(/val LightBorder = Color\(0xFFD5D1C9\)/g, 'val LightBorder = Color(0xFFD6D2C8)');

content = content.replace(/val Color\.Companion\.semanticSurfaceLight: Color get\(\) = Color\(0xFFFBFAF6\)/g, 'val Color.Companion.semanticSurfaceLight: Color get() = Color(0xFFFDFBF7)');
content = content.replace(/val Color\.Companion\.semanticSurfaceElevatedLight: Color get\(\) = Color\(0xFFFFFDF8\)/g, 'val Color.Companion.semanticSurfaceElevatedLight: Color get() = Color(0xFFFFFFFF)');
content = content.replace(/val Color\.Companion\.semanticControlLight: Color get\(\) = Color\(0xFFEFEBE2\)/g, 'val Color.Companion.semanticControlLight: Color get() = Color(0xFFE8E4DA)');
content = content.replace(/val Color\.Companion\.semanticBackgroundLight: Color get\(\) = Color\(0xFFF5F3EC\)/g, 'val Color.Companion.semanticBackgroundLight: Color get() = Color(0xFFF4F1EA)');
content = content.replace(/val Color\.Companion\.semanticBorderLight: Color get\(\) = Color\(0xFFD5D1C9\)/g, 'val Color.Companion.semanticBorderLight: Color get() = Color(0xFFD6D2C8)');

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);
