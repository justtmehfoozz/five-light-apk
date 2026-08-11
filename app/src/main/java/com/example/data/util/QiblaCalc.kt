package com.example.data.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object QiblaCalc {
    // Kaaba Coordinates
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the Qibla direction in degrees clockwise from True North (0° = North, 90° = East, etc.)
     */
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val deltaLngRad = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLngRad)
        val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(deltaLngRad)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad).toFloat()

        if (qiblaDeg < 0) {
            qiblaDeg += 360f
        }
        return qiblaDeg
    }
}
