import re

def fix_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Find Switch( ... ) without colors and add colors
    # Since this is tricky with regex, we can just replace Switch( with Switch( colors = SwitchDefaults.colors(...),
    # Let's do it carefully.
    
    # Simple replace for existing ones:
    content = content.replace(
"""            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )""",
"""            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = Color.semanticPrimaryAccent,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                uncheckedTrackColor = Color.semanticBorder,
                uncheckedBorderColor = Color.semanticStrongBorder
            )"""
    )
    
    content = content.replace(
"""Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = { isVibrationEnabled = it },
                            modifier = Modifier.testTag("vibration_switch")
                        )""",
"""Switch(
                            checked = isVibrationEnabled,
                            onCheckedChange = { isVibrationEnabled = it },
                            modifier = Modifier.testTag("vibration_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = Color.semanticBorder,
                                uncheckedBorderColor = Color.semanticStrongBorder
                            )
                        )"""
    )
    content = content.replace(
"""Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { isSoundEnabled = it },
                            modifier = Modifier.testTag("sound_switch")
                        )""",
"""Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { isSoundEnabled = it },
                            modifier = Modifier.testTag("sound_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = Color.semanticBorder,
                                uncheckedBorderColor = Color.semanticStrongBorder
                            )
                        )"""
    )
    content = content.replace(
"""Switch(
                            checked = isAutoCountEnabled,
                            onCheckedChange = { isAutoCountEnabled = it },
                            modifier = Modifier.testTag("autocount_switch")
                        )""",
"""Switch(
                            checked = isAutoCountEnabled,
                            onCheckedChange = { isAutoCountEnabled = it },
                            modifier = Modifier.testTag("autocount_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = Color.semanticBorder,
                                uncheckedBorderColor = Color.semanticStrongBorder
                            )
                        )"""
    )

    content = content.replace(
"""Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = onToggleVibration,
                            modifier = Modifier.testTag("pref_vibration_switch")
                        )""",
"""Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = onToggleVibration,
                            modifier = Modifier.testTag("pref_vibration_switch"),
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = Color.semanticBorder,
                                uncheckedBorderColor = Color.semanticStrongBorder
                            )
                        )"""
    )

    with open(file_path, 'w') as f:
        f.write(content)

fix_file('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt')
fix_file('./app/src/main/java/com/example/ui/screens/TasbeehScreen.kt')
