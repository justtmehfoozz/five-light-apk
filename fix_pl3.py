with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

content = content.replace("@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\n@Composable\nenum class PersonalLogTab { LOG, INSIGHTS, QADA }", "enum class PersonalLogTab { LOG, INSIGHTS, QADA }")
content = content.replace("@Composable\nenum class PersonalLogTab", "enum class PersonalLogTab")

with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "w") as f:
    f.write(content)
