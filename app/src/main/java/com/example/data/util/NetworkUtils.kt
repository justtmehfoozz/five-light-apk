package com.example.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    data class NetworkDiagnosticInfo(
        val isAvailable: Boolean,
        val activeNetworkExists: Boolean,
        val hasInternet: Boolean,
        val hasValidated: Boolean,
        val hasWifi: Boolean,
        val hasCellular: Boolean,
        val hasEthernet: Boolean,
        val hasVpn: Boolean
    ) {
        fun toFormattedLog(): String {
            return "NETWORK CHECK:\n" +
                   "- NetworkUtils result: $isAvailable\n" +
                   "- activeNetwork exists: $activeNetworkExists\n" +
                   "- activeNetwork capabilities:\n" +
                   "  INTERNET: $hasInternet\n" +
                   "  VALIDATED: $hasValidated\n" +
                   "  WIFI: $hasWifi\n" +
                   "  CELLULAR: $hasCellular\n" +
                   "  ETHERNET: $hasEthernet\n" +
                   "  VPN: $hasVpn"
        }
    }

    fun getDiagnosticInfo(context: Context): NetworkDiagnosticInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkDiagnosticInfo(
                isAvailable = false,
                activeNetworkExists = false,
                hasInternet = false,
                hasValidated = false,
                hasWifi = false,
                hasCellular = false,
                hasEthernet = false,
                hasVpn = false
            )
        val activeNetwork = connectivityManager.activeNetwork
            ?: return NetworkDiagnosticInfo(
                isAvailable = false,
                activeNetworkExists = false,
                hasInternet = false,
                hasValidated = false,
                hasWifi = false,
                hasCellular = false,
                hasEthernet = false,
                hasVpn = false
            )
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkDiagnosticInfo(
                isAvailable = false,
                activeNetworkExists = true,
                hasInternet = false,
                hasValidated = false,
                hasWifi = false,
                hasCellular = false,
                hasEthernet = false,
                hasVpn = false
            )

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        val hasVpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        val hasValidTransport = hasWifi || hasCellular || hasEthernet || hasVpn
        val isAvailable = hasInternet && (hasValidTransport || hasValidated)

        return NetworkDiagnosticInfo(
            isAvailable = isAvailable,
            activeNetworkExists = true,
            hasInternet = hasInternet,
            hasValidated = hasValidated,
            hasWifi = hasWifi,
            hasCellular = hasCellular,
            hasEthernet = hasEthernet,
            hasVpn = hasVpn
        )
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        return hasInternet && (hasTransport || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }
}
