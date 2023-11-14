package de.martinbrylski.wegpunkte.util

/**
 * Provides some helper methods for navigation and GPS use cases.
 *
 */
object NavigationUtils {

    val KILOMETER_TO_METER_FACTOR = 1000.0
    private val EARTH_RADIUS = 6378.1370
    private val DIST_TO_RAD = Math.PI / 180

    /**
     * Computes the distance from one GPS coordinate to another. Uses haversine method.

     * @param lat1  - latitude of first coordinate
     * @param long1 - longitude of first coordinate
     * @param lat2  - latitude of first coordinate
     * @param long2 - longitude of first coordinate
     *
     * @return the distance in meter
     */
    fun distanceInMeter(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val dlong = (long2 - long1) * DIST_TO_RAD
        val dlat = (lat2 - lat1) * DIST_TO_RAD
        val a = Math.pow(
            Math.sin(dlat / 2.0),
            2.0
        ) + Math.cos(lat1 * DIST_TO_RAD) * Math.cos(lat2 * DIST_TO_RAD) * Math.pow(
            Math.sin(dlong / 2.0),
            2.0
        )
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val d = EARTH_RADIUS * c

        return d * KILOMETER_TO_METER_FACTOR
    }

    fun formatDistance(distance: Double): String {
        if (distance > 1000)
            return String.format(
                "%.2f km",
                Math.round(distance) / KILOMETER_TO_METER_FACTOR
            )
        else
            return String.format("%d m", Math.round(distance))
    }

}
