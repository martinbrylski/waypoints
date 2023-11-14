package de.martinbrylski.wegpunkte.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.martinbrylski.wegpunkte.Waypoint

@Dao
interface WaypointDao {

    @Insert
    suspend fun insertWaypoint(waypoint: Waypoint)

    @Query("SELECT * FROM waypoints")
    suspend fun getAllWaypoints(): List<Waypoint>

    @Query("SELECT * FROM waypoints WHERE id = :waypointId")
    suspend fun getWaypointById(waypointId: Long): Waypoint

    @Query("SELECT * FROM waypoints WHERE name LIKE :query")
    suspend fun searchWaypoints(query: String): List<Waypoint>

}
