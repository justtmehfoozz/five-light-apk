package com.example.data.model

/**
 * Standard 8 astronomical lunar phases.
 */
enum class MoonPhaseType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    NEW_MOON("New Moon", "🌑", "The Moon is positioned between Earth and Sun; disc unilluminated."),
    WAXING_CRESCENT("Waxing Crescent", "🌒", "A slender crescent of light grows on the right limb."),
    FIRST_QUARTER("First Quarter", "🌓", "The right half of the lunar disc is illuminated."),
    WAXING_GIBBOUS("Waxing Gibbous", "🌔", "More than half of the disc is illuminated, growing toward Full Moon."),
    FULL_MOON("Full Moon", "🌕", "The Earth-facing lunar disc is completely illuminated."),
    WANING_GIBBOUS("Waning Gibbous", "🌖", "More than half of the disc is illuminated, decreasing after Full Moon."),
    LAST_QUARTER("Last Quarter", "🌗", "The left half of the lunar disc is illuminated."),
    WANING_CRESCENT("Waning Crescent", "🌘", "A slender crescent of light wanes on the left limb before New Moon.")
}

/**
 * Astronomical Moon Phase data representation.
 *
 * Distinct and independent from the official Islamic calendar methodology.
 *
 * @param phaseType The categorized 8-phase astronomical type.
 * @param phaseName The human-readable name of the phase (e.g. "Waxing Crescent").
 * @param illuminationFraction Fraction of illuminated disc (0.0f to 1.0f).
 * @param illuminationPercent Illumination rounded to nearest percentage (0 to 100).
 * @param phaseAngle Progress along the synodic month cycle (0.0f = New Moon, 0.5f = Full Moon, 1.0f = New Moon).
 * @param ageDays Estimated age in days within the mean 29.53-day synodic cycle.
 * @param emoji Representative Unicode moon phase emoji.
 * @param accessibleDescription TalkBack / accessibility description string.
 */
data class MoonPhase(
    val phaseType: MoonPhaseType = MoonPhaseType.NEW_MOON,
    val phaseName: String = "New Moon",
    val illuminationFraction: Float = 0.0f,
    val illuminationPercent: Int = 0,
    val phaseAngle: Float = 0.0f,
    val ageDays: Double = 0.0,
    val emoji: String = "🌑",
    val accessibleDescription: String = "New Moon, approximately 0 percent illuminated."
)
