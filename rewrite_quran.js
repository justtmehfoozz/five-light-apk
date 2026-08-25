const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/QuranScreen.kt', 'utf-8');

content = content.replace(/focusedBorderColor = Color\.semanticPrimaryAccent,/g, `focusedBorderColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticPrimaryAccent else Color.semanticDockBorder,`);
content = content.replace(/unfocusedBorderColor = Color\.semanticBorder,/g, `unfocusedBorderColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticBorder else Color.semanticDockBorder,`);
content = content.replace(/focusedLeadingIconColor = Color\.semanticPrimaryAccent,/g, `focusedLeadingIconColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticPrimaryAccent else Color.semanticDockBorder,`);
content = content.replace(/unfocusedLeadingIconColor = Color\.semanticSecondaryText,/g, `unfocusedLeadingIconColor = if (com.example.ui.theme.isAppInDarkTheme()) Color.semanticSecondaryText else Color.semanticDockBorder,`);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/QuranScreen.kt', content);
