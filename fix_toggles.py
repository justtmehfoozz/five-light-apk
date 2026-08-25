import os
import re

directory = 'app/src/main/java/com/example/ui/screens/'

for filename in os.listdir(directory):
    if filename.endswith('.kt'):
        path = os.path.join(directory, filename)
        with open(path, 'r') as f:
            content = f.read()

        changed = False
        if "checkedThumbColor = androidx.compose.ui.graphics.Color.White" in content:
            content = content.replace("checkedThumbColor = androidx.compose.ui.graphics.Color.White", "checkedThumbColor = Color.semanticAccentForeground")
            changed = True
        
        # Unchecked thumb color is usually neutral. Let's see if we should change uncheckedThumbColor to semanticAccentForeground? No, unchecked thumb is usually a neutral color. Wait, in Light mode, unchecked thumb color is White, but maybe the track is neutral. We shouldn't necessarily change uncheckedThumbColor to semanticAccentForeground. 
        # Actually, let's leave uncheckedThumbColor = Color.White or neutral for now, unless the user specified otherwise. The user said ACTIVE TOGGLE foreground is #FFFFFF/#E6DEF6.
        
        if changed and "import com.example.ui.theme.semanticAccentForeground" not in content:
            if "import com.example.ui.theme.semanticPrimaryAccent" in content:
                content = content.replace("import com.example.ui.theme.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticAccentForeground")
            else:
                content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport com.example.ui.theme.semanticAccentForeground")

        with open(path, 'w') as f:
            f.write(content)
