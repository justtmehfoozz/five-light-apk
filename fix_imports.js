const fs = require('fs');
['app/src/main/java/com/example/ui/screens/NamesOfAllahScreen.kt', 'app/src/main/java/com/example/ui/screens/QuranScreen.kt'].forEach(file => {
    let content = fs.readFileSync(file, 'utf-8');
    if (content.startsWith('import com.example.ui.theme.semanticDockBorder\npackage com.example.ui.screens')) {
        content = content.replace('import com.example.ui.theme.semanticDockBorder\npackage com.example.ui.screens', 'package com.example.ui.screens\nimport com.example.ui.theme.semanticDockBorder');
        fs.writeFileSync(file, content);
    }
});
