package com.example.data.model

import java.util.Locale

data class Mosque(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val isOpenNow: Boolean? = null,
    val phoneNumber: String? = null,
    val websiteUri: String? = null
) {
    val formattedDistance: String
        get() {
            val dist = distanceMeters ?: return ""
            return if (dist < 1000) {
                "${dist.toInt()} m"
            } else {
                String.format(Locale.US, "%.1f km", dist / 1000.0)
            }
        }
}
