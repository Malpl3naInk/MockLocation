package ink.moling.mocklocation.service.locationService.controller

import android.Manifest
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.utils.KalmanFilter
import ink.moling.mocklocation.utils.logger.Logger

class RealLocationController(
    context: Context,
    private val kf: KalmanFilter,
    private val isMocking: () -> Boolean,
    private val output: (CandidateLocation) -> Unit
) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val listener = LocationListener { location ->
        if (isMocking()) return@LocationListener

        val source = when (location.provider) {
            LocationManager.GPS_PROVIDER -> Source.GPS
            LocationManager.NETWORK_PROVIDER -> Source.NETWORK
            else -> return@LocationListener
        }

        val (lat, lng) =
            if (source == Source.GPS) {
                kf.update(
                    location.latitude,
                    location.longitude,
                    location.accuracy,
                    location.time
                )
            } else {
                location.latitude to location.longitude
            }

        Logger.d(
            "RealLocationController",
            "${if (source == Source.GPS) "Filtered" else "Original" }: Lat=$lat, Lng=$lng"
        )

        output(
            CandidateLocation(
                lat = lat,
                lng = lng,
                accuracy = location.accuracy,
                time = location.time,
                alt = location.altitude,
                source = source
            )
        )
    }

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun start() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000, 0f, listener
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 2000, 0f, listener
        )
    }

    fun stop() {
        locationManager.removeUpdates(listener)
    }
}
