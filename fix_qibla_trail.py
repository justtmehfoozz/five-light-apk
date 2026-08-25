import re

filename = 'app/src/main/java/com/example/ui/screens/QiblaScreen.kt'
with open(filename, 'r') as f:
    content = f.read()

# Fix drawMotionTrail
content = content.replace("color = if (isDark) Color.White else orangeAccent", "color = orangeAccent")
content = content.replace("color = if (isDark) Color.White else primaryTextColor", "color = orangeAccent")

# Fix compassHeading in QiblaLiveHeadingRow to use accent?
# OdometerNumber uses primaryColor, which is fine. But wait, maybe the 'Calibrate compass' uses accentColor.
# We'll leave primary text as primary.

with open(filename, 'w') as f:
    f.write(content)
