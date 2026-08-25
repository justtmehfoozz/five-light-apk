with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()

content = content.replace("SEARCH(\"search\", \"Search\", Icons.Filled.Search, Icons.Outlined.Search)", "EXPLORE(\"explore\", \"Explore\", Icons.Filled.Explore, Icons.Outlined.Explore)")
content = content.replace("QIBLA(\"qibla\", \"Qibla\", Icons.Filled.Explore, Icons.Outlined.Explore)", "QIBLA(\"qibla\", \"Qibla\", Icons.Filled.CompassCalibration, Icons.Outlined.CompassCalibration)")

with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)
