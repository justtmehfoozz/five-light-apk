with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for idx, line in enumerate(lines):
    if "Canvas(modifier = Modifier.fillMaxSize())" in line and "val center = Offset(size.width / 2f, size.height / 2f)" in lines[idx+1]:
        new_lines.append("        val badgeBgColor = Color.semanticSurface\n")
        new_lines.append("        val kaabaBodyColor = Color.semanticPrimaryText\n")
    new_lines.append(line)

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.writelines(new_lines)
