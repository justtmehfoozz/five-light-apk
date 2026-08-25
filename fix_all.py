with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()
content = content.replace("QIBLA(\"qibla\", \"Qibla\", Icons.Filled.CompassCalibration, Icons.Outlined.CompassCalibration)", "QIBLA(\"qibla\", \"Qibla\", Icons.Filled.Explore, Icons.Outlined.Explore)")
content = content.replace("EXPLORE(\"explore\", \"Explore\", Icons.Filled.Explore, Icons.Outlined.Explore)", "EXPLORE(\"explore\", \"Explore\", Icons.Filled.Apps, Icons.Outlined.Apps)")
with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/AdhkarScreen.kt", "r") as f:
    content = f.read()
adh_old = """                        Text(
                            text = item.arabic,
                            fontFamily = ArabicText,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 40.sp
                        )"""
adh_new = """                        ArabicText(
                            text = item.arabic,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )"""
content = content.replace(adh_old, adh_new)
with open("app/src/main/java/com/example/ui/screens/AdhkarScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt", "r") as f:
    content = f.read()
dua_old = """                        Text(
                            text = dua.arabic,
                            fontFamily = ArabicText,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 40.sp
                        )"""
dua_new = """                        ArabicText(
                            text = dua.arabic,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )"""
content = content.replace(dua_old, dua_new)
with open("app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt", "w") as f:
    f.write(content)
