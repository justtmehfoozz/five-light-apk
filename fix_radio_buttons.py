import re

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = {""",
"""                                                RadioButton(
                                                    selected = isSelected,
                                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                        selectedColor = Color.semanticPrimaryAccent,
                                                        unselectedColor = Color.semanticStrongBorder
                                                    ),
                                                    onClick = {"""
)
content = content.replace(
"""                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {""",
"""                                        RadioButton(
                                            selected = isSelected,
                                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                                        selectedColor = Color.semanticPrimaryAccent,
                                                        unselectedColor = Color.semanticStrongBorder
                                            ),
                                            onClick = {"""
)

with open('./app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'w') as f:
    f.write(content)
