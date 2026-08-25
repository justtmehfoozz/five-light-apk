with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

content = content.replace('Icon(Icons.Default.Close, contentDescription = "Decrease"', 'Icon(androidx.compose.material.icons.Icons.Default.Remove, contentDescription = "Decrease"')
content = content.replace('Icon(Icons.Default.Check, contentDescription = "Increase"', 'Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Increase"')

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
    f.write(content)
