import re

with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()

old_block = """                                // Dismiss Playback
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .graphicsLayer {
                                            alpha = closeAlpha.value
                                            scaleX = closeScale.value
                                            scaleY = closeScale.value
                                        }
                                        .clip(CircleShape)
                                        .background(dockBorderColor.copy(alpha = 0.2f))
                                        .clickable { onStopAudio() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss audio bar",
                                        tint = controlIconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }"""

new_block = """                                // Dismiss Playback
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .graphicsLayer {
                                            alpha = closeAlpha.value
                                            scaleX = closeScale.value
                                            scaleY = closeScale.value
                                        }
                                        .clip(CircleShape)
                                        .background(dockBorderColor.copy(alpha = 0.2f))
                                        .clickable { onStopAudio() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss audio bar",
                                        tint = controlIconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            } // Close the new Box wrapper
                        }
                    }"""

content = content.replace(old_block, new_block)

with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)
