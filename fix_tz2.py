import re

with open("app/src/main/java/com/example/data/util/PrayerCalc.kt", "r") as f:
    content = f.read()

old_code = """        val cityTz = java.util.SimpleTimeZone((timeZoneOffsetHours * 3600000).toInt(), "CityTZ")"""
new_code = """        val offsetMillis = (timeZoneOffsetHours * 3600000).toInt()
        val offsetHours = offsetMillis / 3600000
        val offsetMins = Math.abs((offsetMillis / 60000) % 60)
        val tzId = String.format(java.util.Locale.US, "GMT%+03d:%02d", offsetHours, offsetMins)
        val cityTz = java.util.SimpleTimeZone(offsetMillis, tzId)"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open("app/src/main/java/com/example/data/util/PrayerCalc.kt", "w") as f:
        f.write(content)
    print("Replaced tz in PrayerCalc successfully.")
else:
    print("Could not find tz code to replace in PrayerCalc")
