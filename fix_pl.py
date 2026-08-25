with open("app/src/main/java/com/example/ui/components/HomeFeatureCards.kt", "r") as f:
    content = f.read()

content = content.replace("@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)\nenum class PersonalLogTab", "enum class PersonalLogTab")

# fix unresolved reference `dayOfWeekShort`.
# Let's check Models.kt to see what `day` properties are.
