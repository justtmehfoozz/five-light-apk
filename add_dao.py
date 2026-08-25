import re
with open("app/src/main/java/com/example/data/db/AppDaos.kt", "r") as f:
    content = f.read()

new_content = content.replace("fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>>", 
"fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>>\n    @Query(\"SELECT * FROM prayer_logs ORDER BY date DESC\")\n    fun getAllPrayerLogs(): Flow<List<PrayerLogEntity>>")

with open("app/src/main/java/com/example/data/db/AppDaos.kt", "w") as f:
    f.write(new_content)
print("Updated DAO")
