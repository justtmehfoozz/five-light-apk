import re

with open("app/src/main/java/com/example/ui/viewmodel/AppViewModel.kt", "r") as f:
    content = f.read()

target = r"""    private fun startCountdownTicker\(\) \{.*?delay\(1000\)\n            \}\n        \}\n    \}"""

replacement = """    private fun startCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var lastHijriRefresh = 0L
            while (isActive) {
                // Check if calendar date changed (e.g. midnight rollover)
                val todayDate = repository.getTodayDateString()
                if (todayDate != _currentDateString.value) {
                    _currentDateString.value = todayDate
                    refreshPrayerTimes()
                }

                var next = _nextPrayer.value
                if (next != null) {
                    val now = System.currentTimeMillis()
                    var diffMillis = next.timeMillis - now
                    if (diffMillis < 0) {
                        refreshPrayerTimes()
                        next = _nextPrayer.value
                        diffMillis = if (next != null) {
                            (next.timeMillis - System.currentTimeMillis()).coerceAtLeast(0)
                        } else 0
                    }

                    // Periodically refresh the Hijri date to catch the Maghrib boundary in real-time
                    if (System.currentTimeMillis() - lastHijriRefresh > 60000) {
                        lastHijriRefresh = System.currentTimeMillis()
                        refreshHijriDate()
                    }

                    val totalSeconds = diffMillis / 1000
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60
                    _countdownString.value = String.format(java.util.Locale.US, "-%02d:%02d:%02d", hours, minutes, seconds)
                }

                delay(1000)
            }
        }
    }"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/AppViewModel.kt", "w") as f:
    f.write(new_content)
