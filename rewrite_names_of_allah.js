const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/NamesOfAllahScreen.kt', 'utf-8');

content = content.replace(/val borderStrokeColor = Color.semanticBorder/g, `val borderStrokeColor = Color.semanticBorder
    val searchBorderW = if (isDark) 1.dp else 1.5.dp
    val searchBorderC = if (isDark) borderStrokeColor else com.example.ui.theme.Color.semanticDockBorder
    val searchIconC = if (isDark) textSecondary else com.example.ui.theme.Color.semanticDockBorder`);

content = content.replace(/\.border\(\s*width = 1\.dp,\s*color = borderStrokeColor,\s*shape = RoundedCornerShape\(14\.dp\)\s*\)/g, `.border(
                    width = searchBorderW,
                    color = searchBorderC,
                    shape = RoundedCornerShape(14.dp)
                )`);

content = content.replace(/tint = textSecondary,\s*modifier = Modifier\.size\(20\.dp\)/g, `tint = searchIconC,
                    modifier = Modifier.size(20.dp)`);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/NamesOfAllahScreen.kt', content);
