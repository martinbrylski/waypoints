import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import timber.log.Timber

class GpsLocationManager(private val context: Context) {

    private var locationManager: LocationManager? = null
    private var locationObserver: LocationObserver? = null

    fun setLocationObserver(observer: LocationObserver) {
        this.locationObserver = observer
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000,
            1.0f,
            locationListener
        )
    }

    fun stopLocationUpdates() {
        locationManager?.removeUpdates(locationListener)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationObserver?.onLocationChanged(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // GPS provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // GPS provider disabled
        }
    }
}

interface LocationObserver {
    fun onLocationChanged(location: Location)
}
