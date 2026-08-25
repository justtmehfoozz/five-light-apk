with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.screens.CalendarScreen", "import com.example.ui.screens.CalendarScreen\nimport com.example.ui.screens.ExploreScreen")

block_old = """                            4 -> {
                                val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()

                                CalendarScreen(
                                    hijriDate = hijriDate
                                )
                            }"""

block_new = """                            4 -> {
                                val hijriDate by viewModel.hijriDate.collectAsStateWithLifecycle()

                                ExploreScreen(
                                    hijriDate = hijriDate
                                )
                            }"""

content = content.replace(block_old, block_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
