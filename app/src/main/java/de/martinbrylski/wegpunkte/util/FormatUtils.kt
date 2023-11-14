package de.martinbrylski.wegpunkte.util

import com.joanzapata.iconify.fonts.MaterialCommunityIcons
import com.joanzapata.iconify.fonts.MaterialIcons

/**
 * Some formatting related utility functions.
 *
 * @author martin.brylski
 * @since 10.02.16
 */
object FormatUtils {
    object Type {
        val WAYPOINT = 0
        val ROCK = 1
        val HOUSE = 2
        val SHELTER = 3
        val CAVE_ENTRANCE = 4
        val MEMORIAL = 5
        val CAMPING = 6
        val HOSTEL = 7
        val GUEST_HOUSE = 8
        val RESTAURANT = 9
    }

    fun MaterialIcons.iconify(): String {
        return "{${this.key()}}"
    }

    fun MaterialCommunityIcons.iconify(): String {
        return "{${this.key()}}"
    }

    fun getIconifiedTextForWaypointType(type: Int): String {
        when (type) {
            Type.WAYPOINT -> return MaterialCommunityIcons.mdi_tag_text_outline.iconify()
            Type.ROCK -> return MaterialIcons.md_terrain.iconify()
            Type.HOUSE -> return MaterialIcons.md_store_mall_directory.iconify()
            Type.SHELTER -> return MaterialIcons.md_satellite.iconify()
            Type.CAMPING -> return MaterialCommunityIcons.mdi_tent.iconify()
            Type.CAVE_ENTRANCE -> return MaterialIcons.md_vignette.iconify()
            Type.MEMORIAL -> return MaterialIcons.md_local_activity.iconify()
            Type.GUEST_HOUSE -> return MaterialIcons.md_hotel.iconify()
            Type.RESTAURANT -> return MaterialIcons.md_local_dining.iconify()
            Type.HOSTEL -> return MaterialIcons.md_hotel.iconify()
            else -> return MaterialIcons.md_place.iconify()
        }
    }
}