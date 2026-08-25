with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

content = content.replace("Icon(androidx.compose.material.icons.Icons.Default.Remove, contentDescription = \"Decrease\", modifier = Modifier.size(20.dp))", 'Text("-", fontSize = 24.sp)')
content = content.replace("Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = \"Increase\", modifier = Modifier.size(20.dp))", 'Text("+", fontSize = 24.sp)')

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
    f.write(content)
