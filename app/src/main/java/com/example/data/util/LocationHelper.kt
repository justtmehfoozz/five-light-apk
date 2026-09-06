package com.example.data.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.data.model.CityLocation
import java.util.Locale
import java.util.TimeZone

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun getLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        var bestLocation: Location? = null
        val providers = lm.getProviders(true)
        for (provider in providers) {
            try {
                val l = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            } catch (_: SecurityException) {}
        }
        return bestLocation
    }

    fun resolveCityLocation(context: Context, location: Location): CityLocation {
        val lat = location.latitude
        val lon = location.longitude
        val tzHours = TimeZone.getDefault().rawOffset / 3600000.0

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull()
            val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "My Location"
            val country = addr?.countryName ?: ""
            CityLocation(
                cityName = city,
                countryName = country,
                latitude = lat,
                longitude = lon,
                timezoneOffsetHours = tzHours
            )
        } catch (_: Exception) {
            CityLocation(
                cityName = "Device Location",
                countryName = "",
                latitude = lat,
                longitude = lon,
                timezoneOffsetHours = tzHours
            )
        }
    }
}
