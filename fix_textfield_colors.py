import os

directory = 'app/src/main/java/com/example/ui/screens/'

for filename in os.listdir(directory):
    if filename.endswith('.kt'):
        path = os.path.join(directory, filename)
        with open(path, 'r') as f:
            content = f.read()

        changed = False
        if "focusedContainerColor = MaterialTheme.colorScheme.surface," in content:
            content = content.replace("focusedContainerColor = MaterialTheme.colorScheme.surface,", "focusedContainerColor = Color.semanticSurface,")
            changed = True
        if "unfocusedContainerColor = MaterialTheme.colorScheme.surface," in content:
            content = content.replace("unfocusedContainerColor = MaterialTheme.colorScheme.surface,", "unfocusedContainerColor = Color.semanticSurface,")
            changed = True

        # Ensure semanticSurface is imported if it isn't
        if changed and "import com.example.ui.theme.semanticSurface" not in content:
            content = content.replace("import com.example.ui.theme.semanticPrimaryAccent", "import com.example.ui.theme.semanticPrimaryAccent\nimport com.example.ui.theme.semanticSurface")

        with open(path, 'w') as f:
            f.write(content)
