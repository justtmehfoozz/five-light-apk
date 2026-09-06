package com.example.data.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import java.util.Locale
import java.util.TimeZone

data class RegionalPrayerSettings(
    val calcMethod: CalcMethod,
    val hijriDateMethod: HijriDateMethod,
    val madhab: Madhab,
    val calcMethodRecommendation: String,
    val hijriMethodRecommendation: String,
    val madhabRecommendation: String
)

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
            val defaultSettings = determineRegionalPrayerSettings(city, country, lat, lon)
            CityLocation(
                cityName = city,
                countryName = country,
                latitude = lat,
                longitude = lon,
                timezoneOffsetHours = tzHours,
                defaultCalcMethod = defaultSettings.calcMethod
            )
        } catch (_: Exception) {
            val defaultSettings = determineRegionalPrayerSettings("Device Location", "", lat, lon)
            CityLocation(
                cityName = "Device Location",
                countryName = "",
                latitude = lat,
                longitude = lon,
                timezoneOffsetHours = tzHours,
                defaultCalcMethod = defaultSettings.calcMethod
            )
        }
    }

    fun determineRegionalPrayerSettings(cityLocation: CityLocation): RegionalPrayerSettings {
        return determineRegionalPrayerSettings(
            cityName = cityLocation.cityName,
            countryName = cityLocation.countryName,
            latitude = cityLocation.latitude,
            longitude = cityLocation.longitude
        )
    }

    fun determineRegionalPrayerSettings(
        cityName: String,
        countryName: String,
        latitude: Double,
        longitude: Double
    ): RegionalPrayerSettings {
        val country = countryName.lowercase(Locale.ROOT)
        val city = cityName.lowercase(Locale.ROOT)

        // 1. South Asia (India, Pakistan, Bangladesh, Sri Lanka, Afghanistan, Nepal)
        if (country.contains("india") || country.contains("pakistan") || country.contains("bangladesh") ||
            country.contains("sri lanka") || country.contains("afghanistan") || country.contains("nepal") ||
            country.contains("asia/kolkata") || country.contains("asia/karachi") ||
            city.contains("bhiwandi") || city.contains("mumbai") || city.contains("delhi") ||
            city.contains("karachi") || city.contains("kolkata") || city.contains("lahore") ||
            city.contains("dhaka") || city.contains("hyderabad") || city.contains("thane") ||
            city.contains("bengaluru") || city.contains("bangalore") || city.contains("pune") ||
            city.contains("chennai") || city.contains("ahmedabad")
        ) {
            return RegionalPrayerSettings(
                calcMethod = CalcMethod.KARACHI,
                hijriDateMethod = HijriDateMethod.REGIONAL_INDIA,
                madhab = Madhab.HANAFI,
                calcMethodRecommendation = "Recommended for your region",
                hijriMethodRecommendation = "Regional Moon-Sighting (India / South Asia)",
                madhabRecommendation = "Hanafi (Predominant in South Asia)"
            )
        }

        // 2. Arabian Peninsula & Gulf (Saudi Arabia, UAE, Qatar, Kuwait, Bahrain, Oman, Yemen)
        if (country.contains("saudi") || country.contains("arabia") || country.contains("uae") ||
            country.contains("emirates") || country.contains("qatar") || country.contains("kuwait") ||
            country.contains("bahrain") || country.contains("oman") || country.contains("yemen") ||
            country.contains("dubai") || country.contains("abu dhabi") ||
            city.contains("mecca") || city.contains("makkah") || city.contains("medina") ||
            city.contains("riyadh") || city.contains("dubai") || city.contains("doha") ||
            city.contains("jeddah") || city.contains("muscat") || city.contains("kuwait")
        ) {
            return RegionalPrayerSettings(
                calcMethod = CalcMethod.UMM_AL_QURA,
                hijriDateMethod = HijriDateMethod.SAUDI_UMM_AL_QURA,
                madhab = Madhab.STANDARD,
                calcMethodRecommendation = "Recommended for Arabian Peninsula",
                hijriMethodRecommendation = "Saudi Arabia (Umm al-Qura)",
                madhabRecommendation = "Shafi'i, Maliki, Hanbali (Standard)"
            )
        }

        // 3. Egypt, Levant, & North Africa (Egypt, Sudan, Libya, Jordan, Lebanon, Syria, Palestine)
        if (country.contains("egypt") || country.contains("sudan") || country.contains("libya") ||
            country.contains("jordan") || country.contains("lebanon") || country.contains("syria") ||
            country.contains("palestine") || country.contains("algeria") || country.contains("morocco") ||
            country.contains("tunisia") ||
            city.contains("cairo") || city.contains("alexandria") || city.contains("amman") ||
            city.contains("beirut") || city.contains("damascus") || city.contains("jerusalem")
        ) {
            return RegionalPrayerSettings(
                calcMethod = CalcMethod.EGYPTIAN,
                hijriDateMethod = HijriDateMethod.GLOBAL_ASTRONOMICAL,
                madhab = Madhab.STANDARD,
                calcMethodRecommendation = "Recommended for Egypt & Levant",
                hijriMethodRecommendation = "Global Astronomical Calendar",
                madhabRecommendation = "Shafi'i, Maliki, Hanbali (Standard)"
            )
        }

        // 4. Southeast Asia (Singapore, Malaysia, Indonesia, Brunei)
        if (country.contains("singapore") || country.contains("malaysia") || country.contains("indonesia") ||
            country.contains("brunei") || city.contains("singapore") || city.contains("kuala lumpur") ||
            city.contains("jakarta") || city.contains("bandung") || city.contains("surabaya")
        ) {
            return RegionalPrayerSettings(
                calcMethod = CalcMethod.SINGAPORE,
                hijriDateMethod = HijriDateMethod.GLOBAL_ASTRONOMICAL,
                madhab = Madhab.STANDARD,
                calcMethodRecommendation = "Recommended for Southeast Asia (MUIS)",
                hijriMethodRecommendation = "Global Astronomical Calendar",
                madhabRecommendation = "Shafi'i, Maliki, Hanbali (Shafi'i standard)"
            )
        }

        // 5. North America (USA, Canada, Mexico)
        if (country.contains("usa") || country.contains("united states") || country.contains("canada") ||
            country.contains("mexico") || city.contains("new york") || city.contains("toronto") ||
            city.contains("los angeles") || city.contains("chicago") || city.contains("houston") ||
            city.contains("dallas") || city.contains("san francisco") || city.contains("vancouver")
        ) {
            return RegionalPrayerSettings(
                calcMethod = CalcMethod.ISNA,
                hijriDateMethod = HijriDateMethod.GLOBAL_ASTRONOMICAL,
                madhab = Madhab.STANDARD,
                calcMethodRecommendation = "Recommended for North America (ISNA)",
                hijriMethodRecommendation = "Global Astronomical Calendar",
                madhabRecommendation = "Shafi'i, Maliki, Hanbali (Standard)"
            )
        }

        // 6. Default / International (Europe, UK, Turkey, Central Asia, Australia, Rest of World)
        val isHanafiRegion = country.contains("turkey") || country.contains("türkiye") ||
                country.contains("uzbekistan") || country.contains("kazakhstan") ||
                country.contains("turkmenistan") || country.contains("tajikistan") ||
                country.contains("kyrgyzstan") || city.contains("istanbul") || city.contains("ankara")

        return RegionalPrayerSettings(
            calcMethod = CalcMethod.MWL,
            hijriDateMethod = HijriDateMethod.GLOBAL_ASTRONOMICAL,
            madhab = if (isHanafiRegion) Madhab.HANAFI else Madhab.STANDARD,
            calcMethodRecommendation = "Muslim World League (Standard International)",
            hijriMethodRecommendation = "Global Astronomical Calendar",
            madhabRecommendation = if (isHanafiRegion) "Hanafi (Predominant in region)" else "Shafi'i, Maliki, Hanbali (Standard)"
        )
    }
}

