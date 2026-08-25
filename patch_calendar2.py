import re

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "r") as f:
    content = f.read()

target = r"""                                                Box\(
                                                    modifier = Modifier
                                                        \.weight\(1f\)
                                                        \.pointerInput\(month, year, dayNum\) \{.*?                                                            \.clip\(CircleShape\)
                                                            \.then\(
                                                                if \(isActualToday\) \{
                                                                    Modifier\.border\(
                                                                        width = 1\.5\.dp,
                                                                        color = amberGold,
                                                                        shape = CircleShape
                                                                    \)
                                                                \} else \{
                                                                    Modifier
                                                                \}
                                                            \),
                                                        contentAlignment = Alignment\.Center
                                                    \) \{"""

replacement = """                                                val isSelectedAnim by animateFloatAsState(targetValue = if (isSelected) 1f else 0f, animationSpec = tween(180))
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .pointerInput(month, year, dayNum) {
                                                            detectTapGestures(
                                                                onTap = {
                                                                    selectedDay = dayNum
                                                                    selectedMonth = month
                                                                    selectedYear = year
                                                                    if (showPopup) {
                                                                        showPopup = false
                                                                        timerJob?.cancel()
                                                                    }
                                                                },
                                                                onLongPress = {
                                                                    selectedDay = dayNum
                                                                    selectedMonth = month
                                                                    selectedYear = year
                                                                    if (eventsOnDay.isNotEmpty()) {
                                                                        timerJob?.cancel()
                                                                        popupEvents = eventsOnDay
                                                                        showPopup = true
                                                                        timerJob = coroutineScope.launch {
                                                                            delay(3500)
                                                                            showPopup = false
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(cellHeight)
                                                            .drawBehind {
                                                                if (isKeyDate) {
                                                                    drawCircle(
                                                                        color = amberGold.copy(alpha = if (isDark) 0.70f else 0.55f),
                                                                        radius = (size.minDimension / 2f) - 0.5.dp.toPx(),
                                                                        style = Stroke(
                                                                            width = 1.2.dp.toPx(),
                                                                            pathEffect = PathEffect.dashPathEffect(
                                                                                floatArrayOf(4.5.dp.toPx(), 3.5.dp.toPx()),
                                                                                0f
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                                if (isActualToday) {
                                                                    drawCircle(
                                                                        color = amberGold,
                                                                        radius = (size.minDimension / 2f) - 0.75.dp.toPx(),
                                                                        style = Stroke(width = 1.5.dp.toPx())
                                                                    )
                                                                }
                                                                if (isSelectedAnim > 0.01f) {
                                                                    val radiusOffset = if (isActualToday) 4.dp.toPx() else 0f
                                                                    drawCircle(
                                                                        color = selectedFillColor.copy(alpha = isSelectedAnim),
                                                                        radius = (size.minDimension / 2f) - radiusOffset
                                                                    )
                                                                }
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "w") as f:
    f.write(new_content)
