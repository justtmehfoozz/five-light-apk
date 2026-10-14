package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.data.util.DuaItem

enum class EmotionCategoryGroup(val label: String) {
    ALL("All"),
    DIFFICULT("Difficult feelings"),
    POSITIVE("Positive feelings"),
    RELATIONSHIPS("Relationships"),
    MOTIVATION("Motivation & uncertainty")
}

@Immutable
data class EmotionItem(
    val id: String,
    val name: String,
    val categoryGroup: EmotionCategoryGroup,
    val colorLight: Color,
    val colorDark: Color,
    val isDarkText: Boolean = false,
    val duaItem: DuaItem,
    val isSensitive: Boolean = false
)
