package de.martinbrylski.waypoints.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class Waypoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @NonNull
    val area: String,
    @NonNull
    val name: String,
    @NonNull
    val description: String,
    @NonNull
    val latitude: String,
    @NonNull
    val longitude: String,
    @NonNull
    val type: String
)