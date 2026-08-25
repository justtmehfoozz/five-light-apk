import re

for filename in ['app/src/main/java/com/example/ui/screens/AdhkarScreen.kt', 'app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt']:
    with open(filename, 'r') as f:
        content = f.read()

    # The error is `Unresolved reference 'tabIndicatorOffset'`.
    # `androidx.compose.material3.TabRowDefaults.tabIndicatorOffset` is an extension function on Modifier.
    # To use it properly without issues, let's just make it `Modifier.tabIndicatorOffset` and ensure the import `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset` is there.
    # Actually, we already did that! Let's check what exactly is there.
    # Let's replace `Modifier.tabIndicatorOffset` with `androidx.compose.material3.TabRowDefaults.tabIndicatorOffset`... wait, extension functions can be called statically if we know how, but in Kotlin it's better to just import it.
    
    # Wait! TabRowDefaults inside Material3 is an object, but `tabIndicatorOffset` might be an extension on Modifier, or maybe it's just `androidx.compose.material3.TabRowDefaults.tabIndicatorOffset(...)` in Compose 1.2+? No, it's `Modifier.tabIndicatorOffset`.
    # Let's see if we can do `androidx.compose.material3.TabRowDefaults.PrimaryIndicator` or `SecondaryIndicator`.
    # Let's replace the whole indicator with standard indicator for now if it's failing.
    
    replacement = """            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    modifier = androidx.compose.material3.TabRowDefaults.tabIndicatorOffset(androidx.compose.ui.Modifier, tabPositions[selectedTab]),
                    height = 3.dp,
                    color = Color.semanticPrimaryAccent
                )
            },"""
    
    if "AdhkarScreen" in filename:
        content = re.sub(r'indicator = \{ tabPositions ->\n.*?SecondaryIndicator\(\n.*?modifier = Modifier.tabIndicatorOffset\(tabPositions\[selectedTab\]\),\n.*?height = 3\.dp,\n.*?color = Color\.semanticPrimaryAccent\n.*?\)\n.*?\},',
        """            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                        modifier = androidx.compose.ui.Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Color.semanticPrimaryAccent
                    )
                }
            },""", content, flags=re.DOTALL)
            
    if "DuaLibrary" in filename:
        content = re.sub(r'indicator = \{ tabPositions ->\n.*?SecondaryIndicator\(\n.*?modifier = Modifier.tabIndicatorOffset\(tabPositions\[categories\.indexOf\(selectedCategory\)\]\),\n.*?height = 3\.dp,\n.*?color = Color\.semanticPrimaryAccent\n.*?\)\n.*?\},',
        """            indicator = { tabPositions ->
                val idx = categories.indexOf(selectedCategory)
                if (idx in tabPositions.indices) {
                    androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                        modifier = androidx.compose.ui.Modifier.tabIndicatorOffset(tabPositions[idx]),
                        height = 3.dp,
                        color = Color.semanticPrimaryAccent
                    )
                }
            },""", content, flags=re.DOTALL)
            
    # Wait, the compiler specifically says `Unresolved reference 'tabIndicatorOffset'`. 
    # If `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset` is present, it shouldn't fail.
    # What if it's NOT imported properly? Or what if it's `androidx.compose.material3.TabRowDefaults.tabIndicatorOffset` and I'm calling it as `androidx.compose.ui.Modifier.tabIndicatorOffset`?
    # Wait, `tabIndicatorOffset` in Material 3 is actually an extension function ON `Modifier` but inside the `TabRowDefaults` object?
    # In Compose Material 3: `fun Modifier.tabIndicatorOffset(currentTabPosition: TabPosition): Modifier` is NOT inside TabRowDefaults in older versions, but in newer versions it's `TabRowDefaults.tabIndicatorOffset`. Wait, in Material 3, it's `fun Modifier.tabIndicatorOffset` but we usually import it.
    # Actually, in Material 3, it's `Modifier.tabIndicatorOffset(currentTabPosition)` and we need `import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset`.
    
    with open(filename, 'w') as f:
        f.write(content)
