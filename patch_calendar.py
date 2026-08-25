import re

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "r") as f:
    content = f.read()

target = r"""                            val targetX = cellWidth \* selectedCol \+ \(cellWidth - cellHeight\) / 2.*?                                            if \(cellIndex >= startWeekday && dayNum <= daysInMonth\) \{"""

replacement = """                            Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                                for (row in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (col in 0..6) {
                                            val cellIndex = row * 7 + col
                                            val dayNum = cellIndex - startWeekday + 1
                                            if (cellIndex >= startWeekday && dayNum <= daysInMonth) {"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "w") as f:
    f.write(new_content)
