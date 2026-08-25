with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()
content = content.replace("import androidx.compose.material.icons.filled.Home\n", "import androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.Menu\n")
content = content.replace("import androidx.compose.material.icons.outlined.Home\n", "import androidx.compose.material.icons.outlined.Home\nimport androidx.compose.material.icons.outlined.Menu\n")
with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)
