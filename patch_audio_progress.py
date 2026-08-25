import re

with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "r") as f:
    content = f.read()

old_block = """                            // Playback Controls Row
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {"""

new_block = """                            Box(modifier = Modifier.fillMaxSize()) {
                                // Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 32.dp)
                                        .offset(y = (-6).dp)
                                        .clip(CircleShape)
                                        .background(inactiveIconColor.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = audioProgress.coerceIn(0f, 1f))
                                            .background(activeIconColor)
                                    )
                                }

                                // Playback Controls Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {"""

content = content.replace(old_block, new_block)

with open("app/src/main/java/com/example/ui/components/BottomNavBar.kt", "w") as f:
    f.write(content)
