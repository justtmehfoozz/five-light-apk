import re

files_to_fix = [
    './app/src/main/java/com/example/ui/components/HomeFeatureCards.kt',
    './app/src/main/java/com/example/ui/components/QuranPlannerTabContent.kt',
    './app/src/main/java/com/example/ui/components/KhatmPlannerSheet.kt'
]

for filepath in files_to_fix:
    with open(filepath, 'r') as f:
        content = f.read()

    # Find and remove duplicate imports
    imports = []
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if line.startswith("import androidx.compose.ui.graphics.Color"):
            if "import androidx.compose.ui.graphics.Color" not in imports:
                imports.append("import androidx.compose.ui.graphics.Color")
                new_lines.append(line)
            else:
                continue
        else:
            new_lines.append(line)
            
    # Also clean up the unresolved reference 'Color' issue which might be due to wildcard imports or conflicting names.
    # The error says "Conflicting import: imported name 'Color' is ambiguous."
    # Wait, maybe there's `import android.graphics.Color`?
    # Let's see if there are other `Color` imports.
    
    with open(filepath, 'w') as f:
        f.write('\n'.join(new_lines))

