import os
import re

directory = 'app/src/main/java/com/example/ui/screens/'

for filename in os.listdir(directory):
    if filename.endswith('.kt'):
        path = os.path.join(directory, filename)
        with open(path, 'r') as f:
            content = f.read()

        changed = False
        
        # We need to find `androidx.compose.material3.Tab` and inject modifier background
        # But this might be tricky with regex. Let's just find Tab(
        
        # For now, let's look for selectedContentColor = Color.semanticPrimaryAccent
        if "selectedContentColor = Color.semanticPrimaryAccent" in content and "Tab(" in content:
            # Let's replace selectedContentColor with Color.semanticAccentForeground
            # And add modifier = Modifier.background(if (selectedTab == X) Color.semanticPrimaryAccent else Color.Transparent)
            pass

