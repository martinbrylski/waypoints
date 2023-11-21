package de.martinbrylski.waypoints.ui.waypoints

import GpsLocationManager
import LocationObserver
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.martinbrylski.waypoints.R
import de.martinbrylski.waypoints.data.AppDatabase
import de.martinbrylski.waypoints.data.Waypoint
import de.martinbrylski.waypoints.data.WaypointDao
import de.martinbrylski.waypoints.databinding.FragmentWaypointListBinding
import de.martinbrylski.waypoints.databinding.WaypointListContentBinding
import de.martinbrylski.waypoints.util.FormatUtils
import de.martinbrylski.waypoints.util.NavigationUtils
import de.martinbrylski.waypoints.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link WaypointDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class WaypointListFragment : Fragment() {

    private lateinit var waypointAdapter: WaypointAdapter
    private lateinit var waypointDao: WaypointDao
    private var _binding: FragmentWaypointListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWaypointListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }

        })

        val recyclerView: RecyclerView = binding.waypointList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? =
            view.findViewById(R.id.waypoint_detail_container)

        setupRecyclerView(recyclerView, itemDetailFragmentContainer)
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {
        val database = AppDatabase.getDatabase(requireContext())
        waypointDao = database.waypointDao()

        val waypoints = runBlocking {
            waypointDao.getAllWaypoints()
        }

        waypointAdapter = WaypointAdapter(waypoints, requireContext(), itemDetailFragmentContainer)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = waypointAdapter
        }
    }

    private fun performSearch(query: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            var waypoints: List<Waypoint>
            if (!query.isNullOrBlank()) {
                waypoints = waypointDao.searchWaypoints("%$query%")
            } else
                waypoints = waypointDao.getAllWaypoints()
            requireActivity().runOnUiThread {
                waypointAdapter.updateWaypoints(waypoints)
            }
        }
    }

    class WaypointAdapter(
        private var waypoints: List<Waypoint>,
        private val context: Context,
        private val itemDetailFragmentContainer: View?
    ) :
        RecyclerView.Adapter<WaypointAdapter.WaypointViewHolder>(), LocationObserver {

        private lateinit var gpsLocationManager: GpsLocationManager

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaypointViewHolder {
            val binding =
                WaypointListContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return WaypointViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WaypointViewHolder, position: Int) {
            val currentWaypoint = waypoints[position]
            holder.bind(currentWaypoint)

            with(holder.itemView) {
                tag = currentWaypoint
                setOnClickListener { itemView ->
                    val item = itemView.tag as Waypoint
                    val bundle = Bundle()
                    bundle.putLong(
                        WaypointDetailFragment.ARG_ITEM_ID,
                        item.id
                    )
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.waypoint_detail_container, bundle)
                    } else {
                        itemView.findNavController().navigate(R.id.show_waypoint_detail, bundle)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return waypoints.size
        }

        override fun onViewAttachedToWindow(holder: WaypointViewHolder) {
            super.onViewAttachedToWindow(holder)

            handleLocationUpdates()
        }

        private fun handleLocationUpdates() {
            gpsLocationManager = GpsLocationManager(context)
            gpsLocationManager.setLocationObserver(this)
            if (PermissionHelper.hasLocationPermission) {
                gpsLocationManager.startLocationUpdates()
            }
        }

        override fun onViewDetachedFromWindow(holder: WaypointViewHolder) {
            super.onViewDetachedFromWindow(holder)
            gpsLocationManager.stopLocationUpdates()
        }

        fun updateWaypoints(newWaypoints: List<Waypoint>) {
            waypoints = newWaypoints
            notifyDataSetChanged()
        }

        class WaypointViewHolder(private val binding: WaypointListContentBinding) :
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
                binding.txtType.text =
                    FormatUtils.getIconifiedTextForWaypointType(waypoint.type.toInt())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}