package de.martinbrylski.waypoints.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
        return when (type) {
            Type.WAYPOINT -> MaterialCommunityIcons.mdi_tag_text_outline.iconify()
            Type.ROCK -> MaterialIcons.md_terrain.iconify()
            Type.HOUSE -> MaterialIcons.md_store_mall_directory.iconify()
            Type.SHELTER -> MaterialIcons.md_satellite.iconify()
            Type.CAMPING -> MaterialCommunityIcons.mdi_tent.iconify()
            Type.CAVE_ENTRANCE -> MaterialIcons.md_vignette.iconify()
            Type.MEMORIAL -> MaterialIcons.md_local_activity.iconify()
            Type.GUEST_HOUSE -> MaterialIcons.md_hotel.iconify()
            Type.RESTAURANT -> MaterialIcons.md_local_dining.iconify()
            Type.HOSTEL -> MaterialIcons.md_hotel.iconify()
            else -> MaterialIcons.md_place.iconify()
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap()
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}