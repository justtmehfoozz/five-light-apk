import re

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""    val dockBg = if (isDark) com.example.ui.theme.DockBackground else MaterialTheme.colorScheme.surface
    val dockBorderColor = if (isDark) com.example.ui.theme.BorderDark else com.example.ui.theme.LightBorder""",
"""    val baseDockBg = if (isDark) com.example.ui.theme.DockBackground else MaterialTheme.colorScheme.surface
    val searchBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF2C2C2E) else MaterialTheme.colorScheme.surface
    val dockBg = androidx.compose.ui.graphics.lerp(baseDockBg, searchBg, transformProgress)
    
    val baseDockBorder = if (isDark) com.example.ui.theme.DockBorder else com.example.ui.theme.LightBorder
    val searchBorder = if (isDark) androidx.compose.ui.graphics.Color(255, 255, 255, 20) else com.example.ui.theme.LightBorder
    val dockBorderColor = androidx.compose.ui.graphics.lerp(baseDockBorder, searchBorder, transformProgress)"""
)

# Search Close Button
# "Search close button: Dark Elevated Surface with visible icon."
# Let's find close button
content = content.replace(
"""                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    onDismissSearch()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) com.example.ui.theme.DockIconActiveBg else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isDark) Color.Transparent else com.example.ui.theme.LightBorder, CircleShape)
                                    .testTag("close_search_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close search",
                                    tint = activeIconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }""",
"""                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    onDismissSearch()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) androidx.compose.ui.graphics.Color(0xFF2C2C2E) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isDark) androidx.compose.ui.graphics.Color(255, 255, 255, 20) else com.example.ui.theme.LightBorder, CircleShape)
                                    .testTag("close_search_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close search",
                                    tint = if (isDark) androidx.compose.ui.graphics.Color.White else activeIconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }"""
)

# Search Placeholder
content = content.replace(
"""                                Text(
                                    text = "Search Surahs, Duas, Adhkar...",
                                    color = inactiveIconColor,""",
"""                                Text(
                                    text = "Search Surahs, Duas, Adhkar...",
                                    color = if (isDark) androidx.compose.ui.graphics.Color(0xFF8E8E93) else inactiveIconColor,"""
)

# Search Icon
content = content.replace(
"""                        // Leading Search Icon emerges naturally in place
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = activeIconColor,""",
"""                        // Leading Search Icon emerges naturally in place
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isDark) com.example.ui.theme.Color.semanticPrimaryAccent else activeIconColor,"""
)

with open('./app/src/main/java/com/example/ui/components/BottomNavBar.kt', 'w') as f:
    f.write(content)
