package com.example.widget.state

/**
 * Immutable snapshot of data required to render the Personal Log widget.
 * Decoupled from ViewModels and Compose screens.
 */
data class PersonalLogWidgetState(
    val todayDhikrCount: Int = 0,
    val quranStatusText: String = "",
    val hasQuranActivityToday: Boolean = false,
    val completedPrayersCount: Int = 0,
    val totalPrayersCount: Int = 5,
    val hasAnyActivity: Boolean = false,
    val dateFormatted: String = "",
    val reflectionSnippet: String? = null
)
