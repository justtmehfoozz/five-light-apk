import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = r"""                                HomeScreen\(
                                    nextPrayer = nextPrayer,
                                    prayerTimes = prayerTimes,"""

replacement = """                                HomeScreen(
                                    nextPrayer = nextPrayer,
                                    prayerTimes = prayerTimes,
                                    qadaCounts = qadaCounts,
                                    onUpdateQadaCount = { p, c -> viewModel.updateQadaCount(p, c) },"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(new_content)
