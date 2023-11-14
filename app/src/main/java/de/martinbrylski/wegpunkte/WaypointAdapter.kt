package de.martinbrylski.wegpunkte

import GpsLocationManager
import LocationObserver
import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import de.martinbrylski.wegpunkte.databinding.ItemWaypointBinding
import de.martinbrylski.wegpunkte.util.FormatUtils
import de.martinbrylski.wegpunkte.util.NavigationUtils
import timber.log.Timber

class WaypointAdapter(private var waypoints: List<Waypoint>, private val context: Context) :
    RecyclerView.Adapter<WaypointAdapter.WaypointViewHolder>(), LocationObserver {

    private lateinit var gpsLocationManager: GpsLocationManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaypointViewHolder {
        val binding =
            ItemWaypointBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WaypointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaypointViewHolder, position: Int) {
        val currentWaypoint = waypoints[position]
        holder.bind(currentWaypoint)
    }

    override fun getItemCount(): Int {
        return waypoints.size
    }

    override fun onViewAttachedToWindow(holder: WaypointViewHolder) {
        super.onViewAttachedToWindow(holder)
        gpsLocationManager = GpsLocationManager(context)
        gpsLocationManager.setLocationObserver(this)
        gpsLocationManager.startLocationUpdates()
    }

    override fun onViewDetachedFromWindow(holder: WaypointViewHolder) {
        super.onViewDetachedFromWindow(holder)
        gpsLocationManager.stopLocationUpdates()
    }

    fun updateWaypoints(newWaypoints: List<Waypoint>) {
        waypoints = newWaypoints
        notifyDataSetChanged()
    }

    class WaypointViewHolder(private val binding: ItemWaypointBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            @Volatile
            var curLat = 0.0

            @Volatile
            var curLon = 0.0
        }

        fun bind(waypoint: Waypoint) {
            binding.txtName.text = waypoint.name
            binding.txtDescription.text = waypoint.description
            binding.txtType.text = FormatUtils.getIconifiedTextForWaypointType(waypoint.type.toInt())
            if (curLat != 0.0 && curLon != 0.0) {
                binding.txtDistance.visibility = View.VISIBLE
                binding.txtDistance.text = NavigationUtils.formatDistance(
                    NavigationUtils.distanceInMeter(
                        curLat,
                        curLon,
                        waypoint.latitude.toDouble(),
                        waypoint.longitude.toDouble()
                    )
                )
            } else {
                binding.txtDistance.visibility = View.GONE
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        WaypointViewHolder.curLat = location.latitude
        WaypointViewHolder.curLon = location.longitude

        if (WaypointViewHolder.curLat != 0.0 && WaypointViewHolder.curLon != 0.0) {
            notifyDataSetChanged()
        }
    }

}