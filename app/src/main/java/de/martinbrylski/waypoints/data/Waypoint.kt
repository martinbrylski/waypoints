package de.martinbrylski.waypoints.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class Waypoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val area: String,

    val name: String,

    val description: String,

    val latitude: String,

    val longitude: String,

    val type: String
)