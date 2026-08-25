import re

def fix_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Regex to add colors to Switch( ... )
    # This is a bit risky but let's try
    content = content.replace(
"""                        Switch(
                            checked = isSmartEnabled,""",
"""                        Switch(
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = com.example.ui.theme.semanticPrimaryAccent,
                                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = com.example.ui.theme.semanticBorder,
                                uncheckedBorderColor = com.example.ui.theme.semanticStrongBorder
                            ),
                            checked = isSmartEnabled,"""
    )

    content = content.replace(
"""                                Switch(
                                    checked = isPrayerTimeEnabled,""",
"""                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        checkedTrackColor = com.example.ui.theme.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        uncheckedTrackColor = com.example.ui.theme.semanticBorder,
                                        uncheckedBorderColor = com.example.ui.theme.semanticStrongBorder
                                    ),
                                    checked = isPrayerTimeEnabled,"""
    )
    
    content = content.replace(
"""                                Switch(
                                    checked = isContextualEnabled,""",
"""                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        checkedTrackColor = com.example.ui.theme.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        uncheckedTrackColor = com.example.ui.theme.semanticBorder,
                                        uncheckedBorderColor = com.example.ui.theme.semanticStrongBorder
                                    ),
                                    checked = isContextualEnabled,"""
    )

    content = content.replace(
"""                                Switch(
                                    checked = isNaflEnabled,""",
"""                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        checkedTrackColor = com.example.ui.theme.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                        uncheckedTrackColor = com.example.ui.theme.semanticBorder,
                                        uncheckedBorderColor = com.example.ui.theme.semanticStrongBorder
                                    ),
                                    checked = isNaflEnabled,"""
    )

    content = content.replace(
"""                                        Switch(
                                            checked = enabled,""",
"""                                        Switch(
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                                checkedTrackColor = com.example.ui.theme.semanticPrimaryAccent,
                                                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                                uncheckedTrackColor = com.example.ui.theme.semanticBorder,
                                                uncheckedBorderColor = com.example.ui.theme.semanticStrongBorder
                                            ),
                                            checked = enabled,"""
    )


    with open(file_path, 'w') as f:
        f.write(content)

fix_file('./app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt')
