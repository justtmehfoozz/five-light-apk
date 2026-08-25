with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()
content = content.replace("EXPLORE(\"explore\", \"Explore\", Icons.Filled.Apps, Icons.Outlined.Apps)", "EXPLORE(\"explore\", \"Explore\", Icons.Filled.Menu, Icons.Outlined.Menu)")
with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)
