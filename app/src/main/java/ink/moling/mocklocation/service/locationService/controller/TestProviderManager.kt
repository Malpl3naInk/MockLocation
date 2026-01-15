package ink.moling.mocklocation.service.locationService.controller

import android.location.Criteria
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build

class TestProviderManager(
    private val locationManager: LocationManager
) {

    fun setup() {
        removeGps()
        addGps()

        removeNetwork()
        addNetwork()
    }

    fun teardown() {
        removeGps()
        removeNetwork()
    }

    // -------- GPS --------

    private fun addGps() {
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
        } catch (e: Exception) {
            throw RuntimeException("GPS test provider setup failed", e)
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

    private fun addNetwork() {
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
        } catch (e: Exception) {
            throw RuntimeException("Network test provider setup failed", e)
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
}
