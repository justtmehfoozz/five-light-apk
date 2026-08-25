import re
with open("app/src/main/java/com/example/data/repository/AppRepository.kt", "r") as f:
    content = f.read()

repo_qada = """
    // Qada Persistence
    fun getQadaCount(prayerName: com.example.data.model.PrayerName): Int {
        return prefs?.getInt("qada_${prayerName.name}", 0) ?: 0
    }

    fun setQadaCount(prayerName: com.example.data.model.PrayerName, count: Int) {
        val safeCount = Math.max(0, count)
        prefs?.edit()?.putInt("qada_${prayerName.name}", safeCount)?.apply()
    }
    
    // Quran Goal Persistence
    fun getDailyQuranGoal(): Int {
        return prefs?.getInt("daily_quran_goal", 0) ?: 0
    }
    
    fun setDailyQuranGoal(pages: Int) {
        val safePages = Math.max(0, pages)
        prefs?.edit()?.putInt("daily_quran_goal", safePages)?.apply()
    }

    fun getAllPrayerLogs(): kotlinx.coroutines.flow.Flow<List<com.example.data.db.PrayerLogEntity>> {
        return db.prayerLogDao().getAllPrayerLogs()
    }
"""

new_content = content.replace("fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>> {\n        return db.prayerLogDao().getPrayerLogsForDates(dates)\n    }", 
"fun getPrayerLogsForDates(dates: List<String>): Flow<List<PrayerLogEntity>> {\n        return db.prayerLogDao().getPrayerLogsForDates(dates)\n    }" + repo_qada)

with open("app/src/main/java/com/example/data/repository/AppRepository.kt", "w") as f:
    f.write(new_content)
print("Updated Repository")
