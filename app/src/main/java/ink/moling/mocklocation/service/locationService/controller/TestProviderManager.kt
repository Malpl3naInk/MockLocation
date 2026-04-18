package ink.moling.mocklocation.service.locationService.controller

import android.location.Criteria
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import ink.moling.mocklocation.utils.extensions.isProviderAvailable
import java.io.PrintWriter
import java.io.StringWriter

class TestProviderManager(
    private val locationManager: LocationManager,
    private val onError: ((title: String, message: String, stackTrace: String) -> Unit)? = null
) {

    fun setup(): Boolean {
        if (locationManager.isProviderAvailable(LocationManager.GPS_PROVIDER)) {
            removeGps()
            if (!addGps()) return false
        }
        if (locationManager.isProviderAvailable(LocationManager.NETWORK_PROVIDER)) {
            removeNetwork()
            if (!addNetwork()) return false
        }

        return true
    }

    fun teardown() {
        removeGps()
        removeNetwork()
    }

    // -------- GPS --------

    private fun addGps(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, true, false, false,
                    true, true, true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, true, false, false,
                    true, true, true,
                    Criteria.POWER_HIGH,
                    Criteria.ACCURACY_FINE
                )
            }

            locationManager.setTestProviderEnabled(
                LocationManager.GPS_PROVIDER, true
            )
            return true
        } catch (e: Exception) {
            onError?.invoke(
                "GPS Provider Error",
                "Failed to setup GPS test provider: ${e.message}",
                getStackTraceString(e)
            ) ?: throw RuntimeException("GPS test provider setup failed", e)
            return false
        }
    }

    private fun removeGps() {
        try {
            locationManager.setTestProviderEnabled(
                LocationManager.GPS_PROVIDER, false
            )
            locationManager.removeTestProvider(
                LocationManager.GPS_PROVIDER
            )
        } catch (_: Exception) {}
    }

    // -------- Network --------

    private fun addNetwork(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER,
                    true, false, true, true,
                    true, true, true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                locationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER,
                    true, false, true, true,
                    true, true, true,
                    Criteria.POWER_HIGH,
                    Criteria.ACCURACY_FINE
                )
            }

            locationManager.setTestProviderEnabled(
                LocationManager.NETWORK_PROVIDER, true
            )
            return true
        } catch (e: Exception) {
            onError?.invoke(
                "Network Provider Error",
                "Failed to setup Network test provider: ${e.message}",
                getStackTraceString(e)
            ) ?: throw RuntimeException("Network test provider setup failed", e)
            return false
        }
    }

    private fun removeNetwork() {
        try {
            locationManager.setTestProviderEnabled(
                LocationManager.NETWORK_PROVIDER, false
            )
            locationManager.removeTestProvider(
                LocationManager.NETWORK_PROVIDER
            )
        } catch (_: Exception) {}
    }
    
    private fun getStackTraceString(e: Exception): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        return sw.toString()
    }
}
