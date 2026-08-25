import re
with open("app/src/main/java/com/example/ui/viewmodel/AppViewModel.kt", "r") as f:
    content = f.read()

qada_code = """
    val allPrayerLogs: StateFlow<List<PrayerLogEntity>> = repository.getAllPrayerLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _qadaCounts = MutableStateFlow<Map<PrayerName, Int>>(emptyMap())
    val qadaCounts: StateFlow<Map<PrayerName, Int>> = _qadaCounts

    private val _dailyQuranGoal = MutableStateFlow(0)
    val dailyQuranGoal: StateFlow<Int> = _dailyQuranGoal
    
    fun refreshQadaCounts() {
        val map = mutableMapOf<PrayerName, Int>()
        listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA).forEach {
            map[it] = repository.getQadaCount(it)
        }
        _qadaCounts.value = map
    }
    
    fun updateQadaCount(prayerName: PrayerName, count: Int) {
        repository.setQadaCount(prayerName, count)
        refreshQadaCounts()
    }
    
    fun setDailyQuranGoal(pages: Int) {
        repository.setDailyQuranGoal(pages)
        _dailyQuranGoal.value = repository.getDailyQuranGoal()
    }
"""

init_calls = """        refreshQadaCounts()
        _dailyQuranGoal.value = repository.getDailyQuranGoal()
"""

# Insert state flows
new_content = content.replace("val weeklyPrayerLogs: StateFlow<Map<String, PrayerLogEntity>> = repository.getPrayerLogsForDates(currentWeekDates)\n        .map { list -> list.associateBy { it.date } }\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())",
"val weeklyPrayerLogs: StateFlow<Map<String, PrayerLogEntity>> = repository.getPrayerLogsForDates(currentWeekDates)\n        .map { list -> list.associateBy { it.date } }\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())\n" + qada_code)

# Insert refresh calls into init block
new_content = new_content.replace("startCountdownTicker()\n        initSensors()",
"startCountdownTicker()\n        initSensors()\n" + init_calls)

with open("app/src/main/java/com/example/ui/viewmodel/AppViewModel.kt", "w") as f:
    f.write(new_content)
print("Updated ViewModel")
