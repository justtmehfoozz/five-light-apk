import re

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "r") as f:
    content = f.read()

target = r"""                            val isSelectedInCurrentMonth = month == selectedMonth && year == selectedYear && selectedDay in 1\.\.daysInMonth.*?                            // Shared Moving Date Indicator Circle.*?                            \}"""

replacement = """                            val isSelectedInCurrentMonth = month == selectedMonth && year == selectedYear && selectedDay in 1..daysInMonth"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/CalendarScreen.kt", "w") as f:
    f.write(new_content)
