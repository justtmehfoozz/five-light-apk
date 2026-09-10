package com.example.widget.core

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Constants and sizing configuration for FiveLight Home Screen Widgets.
 */
object WidgetConstants {
    // Breakpoint definitions for Glance responsive sizing
    val SIZE_SMALL = DpSize(110.dp, 80.dp)
    val SIZE_MEDIUM = DpSize(200.dp, 100.dp)
    val SIZE_LARGE = DpSize(260.dp, 180.dp)

    // Deep-linking and Intent Actions
    const val ACTION_OPEN_PERSONAL_LOG = "com.example.action.OPEN_PERSONAL_LOG"
    const val EXTRA_OPEN_PERSONAL_LOG = "extra_open_personal_log"

    const val ACTION_OPEN_PRAYER = "com.example.action.OPEN_PRAYER"
    const val EXTRA_OPEN_PRAYER = "extra_open_prayer"

    const val ACTION_OPEN_QURAN = "com.example.action.OPEN_QURAN"
    const val EXTRA_OPEN_QURAN = "extra_open_quran"

    /**
     * Determines the active layout size category based on current widget container dimensions.
     */
    fun classifySize(size: DpSize): WidgetSizeClass {
        return when {
            size.width < 180.dp || size.height < 110.dp -> WidgetSizeClass.SMALL
            size.height < 160.dp -> WidgetSizeClass.MEDIUM
            else -> WidgetSizeClass.LARGE
        }
    }
}

enum class WidgetSizeClass {
    SMALL,
    MEDIUM,
    LARGE
}
