import re

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'r') as f:
    content = f.read()

# Let's find where drawKaabaTargetMarker is called and inject variables before the Canvas block that contains it
match = re.search(r'(Canvas\([^\)]*\)\s*\{)([\s\S]*?drawKaabaTargetMarker)', content)
if match:
    # Actually, we need them inside the composable, before the Canvas.
    # Where does Canvas start?
    canvas_idx = content.rfind('Canvas', 0, match.end())
    before = content[:canvas_idx]
    after = content[canvas_idx:]
    content = before + "val badgeBgColor = Color.semanticSurface\n        val kaabaBodyColor = Color.semanticPrimaryText\n        " + after

with open('./app/src/main/java/com/example/ui/screens/QiblaScreen.kt', 'w') as f:
    f.write(content)
