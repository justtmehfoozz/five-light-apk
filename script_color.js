const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/theme/Color.kt', 'utf-8');

content = content.replace(/val Color\.Companion\.dockActiveIndicatorLight: Color get\(\) = Color\(0xFF302F2B\)/g, 'val Color.Companion.dockActiveIndicatorLight: Color get() = Color(0xFF8D6B1E)');

fs.writeFileSync('app/src/main/java/com/example/ui/theme/Color.kt', content);

