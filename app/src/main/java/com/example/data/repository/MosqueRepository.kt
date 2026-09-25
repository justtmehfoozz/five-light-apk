package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Mosque
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class MosqueRepository(private val context: Context) {

    private val tag = "MosqueRepository"

    init {
        ensurePlacesInitialized()
    }

    private fun ensurePlacesInitialized() {
        try {
            if (Places.isInitialized()) return

            val key = try {
                val fromBuildConfig = BuildConfig::class.java.getField("MAPS_API_KEY").get(null) as? String
                if (!fromBuildConfig.isNullOrBlank() && fromBuildConfig != "DEFAULT_MAPS_API_KEY") {
                    fromBuildConfig
                } else {
                    val appInfo = context.packageManager.getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA
                    )
                    val fromManifest = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
                    if (!fromManifest.isNullOrBlank() && fromManifest != "DEFAULT_MAPS_API_KEY") {
                        fromManifest
                    } else ""
                }
            } catch (_: Exception) {
                ""
            }

            if (key.isNotBlank()) {
                Places.initializeWithNewPlacesApiEnabled(context.applicationContext, key)
            }
        } catch (e: Exception) {
            Log.w(tag, "Places initialization note: ${e.message}")
        }
    }

    suspend fun getNearbyMosques(
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Double = 5000.0,
        searchQuery: String = ""
    ): List<Mosque> = withContext(Dispatchers.IO) {
        ensurePlacesInitialized()
        val liveResults = searchPlacesNearby(centerLat, centerLng, radiusMeters)

        val filtered = if (searchQuery.isNotBlank()) {
            liveResults.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true)
            }
        } else {
            liveResults
        }

        filtered.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
    }

    private suspend fun searchPlacesNearby(
        lat: Double,
        lng: Double,
        radiusMeters: Double
    ): List<Mosque> = suspendCancellableCoroutine { continuation ->
        try {
            if (!Places.isInitialized()) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val client: PlacesClient = Places.createClient(context)
            val center = LatLng(lat, lng)
            val circle = CircularBounds.newInstance(center, radiusMeters.coerceAtLeast(100.0))

            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.RATING,
                Place.Field.USER_RATING_COUNT
            )

            val includedTypesList = listOf("mosque")
            val maxCount = 20

            Log.d(tag, "--> SearchNearbyRequest INPUTS: lat=$lat, lng=$lng, radiusMeters=$radiusMeters, maxResultCount=$maxCount, includedTypes=$includedTypesList")

            val request = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(includedTypesList)
                .setMaxResultCount(maxCount)
                .build()

            client.searchNearby(request)
                .addOnSuccessListener { response ->
                    val list = response.places.mapNotNull { place ->
                        val latLng = place.latLng ?: return@mapNotNull null
                        val distance = FloatArray(1)
                        Location.distanceBetween(lat, lng, latLng.latitude, latLng.longitude, distance)
                        Mosque(
                            id = place.id ?: latLng.toString(),
                            name = place.name ?: "Masjid",
                            address = place.address ?: "Nearby Prayer Space",
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            distanceMeters = distance[0].toDouble(),
                            rating = place.rating,
                            userRatingsTotal = place.userRatingCount,
                            isOpenNow = true
                        )
                    }
                    Log.d(tag, "<-- SearchNearbyRequest SUCCESS: resultCount=${list.size}")
                    list.forEachIndexed { idx, m ->
                        Log.d(tag, "    #$idx [ID: ${m.id}] Name: ${m.name}, Dist: ${m.formattedDistance}, LatLng: (${m.latitude}, ${m.longitude})")
                    }
                    continuation.resume(list)
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "<-- SearchNearbyRequest FAILURE: exception=${e.javaClass.simpleName}, message=${e.message}", e)
                    continuation.resume(emptyList())
                }
        } catch (e: Exception) {
            Log.w(tag, "Places SDK invocation exception: ${e.message}")
            continuation.resume(emptyList())
        }
    }
}
