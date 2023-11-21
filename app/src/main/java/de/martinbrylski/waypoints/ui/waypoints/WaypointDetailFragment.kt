package de.martinbrylski.waypoints.ui.waypoints

import GpsLocationManager
import LocationObserver
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.CollapsingToolbarLayout
import de.martinbrylski.waypoints.R
import de.martinbrylski.waypoints.data.AppDatabase
import de.martinbrylski.waypoints.data.Waypoint
import de.martinbrylski.waypoints.databinding.FragmentWaypointDetailBinding
import de.martinbrylski.waypoints.util.NavigationUtils
import de.martinbrylski.waypoints.util.PermissionHelper
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import timber.log.Timber

/**
 * A fragment representing a single Waypoint detail screen.
 * This fragment is either contained in a [WaypointListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class WaypointDetailFragment : Fragment(), LocationObserver {

    private var item: Waypoint? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null
    private var _binding: FragmentWaypointDetailBinding? = null
    private var gpsLocationManager: GpsLocationManager? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val waypointDao = database.waypointDao()

        runBlocking {
            arguments?.let {
                if (it.containsKey(ARG_ITEM_ID)) {
                    // Load the placeholder content specified by the fragment
                    // arguments. In a real-world scenario, use a Loader
                    // to load content from a content provider.
                    item = waypointDao.getWaypointById(it.getLong(ARG_ITEM_ID))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWaypointDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout

        handleLocationUpdates()

        Configuration.getInstance().userAgentValue = requireContext().packageName

        return rootView
    }

    private fun handleLocationUpdates() {
        gpsLocationManager = GpsLocationManager(requireContext())
        gpsLocationManager?.setLocationObserver(this)
        if (PermissionHelper.hasLocationPermission) {
            gpsLocationManager?.startLocationUpdates()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateContent()
    }

    private fun updateContent() {
        if (item != null) {
            item!!.let { waypoint ->
                //toolbarLayout?.title = item?.name
                (requireActivity() as AppCompatActivity).supportActionBar?.title = item?.name

                binding.txtDetailArea.text =
                    String.format("%s (%s)", waypoint.description, waypoint.area)
                binding.txtDetailCoordinates.text =
                    String.format("%s, %s°", waypoint.latitude, waypoint.longitude)

                binding.btShowAndroidMap.setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            String.format(
                                "geo:<%s>,<%s>?q=<%s>,<%s>(%s)",
                                waypoint.longitude,
                                waypoint.latitude,
                                waypoint.longitude,
                                waypoint.latitude,
                                waypoint.name
                            )
                        )
                    )

                    startActivity(intent)
                }
                binding.btShowLocalMap.setOnClickListener({
                    val bundle = Bundle()
                    bundle.putLong(
                        MapsFragment.ARG_ITEM_ID,
                        waypoint.id
                    )
                    Timber.d("hoho")
                    this.findNavController()
                        .navigate(R.id.nav_mapview, bundle)
                })
                initMap(binding.activityMap, waypoint)
            }
        }
    }

    private fun initMap(map: MapView, waypoint: Waypoint): Unit {
        // configure map view
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(false)
        map.isTilesScaledToDpi = true

        val mapController = map.controller
        mapController.setZoom(18.0)
        val startPoint = GeoPoint(waypoint.latitude.toDouble(), waypoint.longitude.toDouble())
        mapController.setCenter(startPoint)

        // create overlays
        val items = ArrayList<OverlayItem>()

        val myLocationOverlayItem = OverlayItem(waypoint.name, waypoint.description, startPoint)
        val myCurrentLocationMarker =
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_location_pointer)
        myLocationOverlayItem.setMarker(myCurrentLocationMarker)
        items.add(myLocationOverlayItem)

        val currentLocationOverlay = ItemizedIconOverlay<OverlayItem>(
            context,
            items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemLongPress(p0: Int, p1: OverlayItem?): Boolean {
                    // do nothing
                    return false
                }

                override fun onItemSingleTapUp(p0: Int, p1: OverlayItem?): Boolean {
                    // do nothing
                    return false
                }

            })
        map.overlays.add(currentLocationOverlay)
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.activityMap.onResume()
        gpsLocationManager?.setLocationObserver(this)
        if (PermissionHelper.hasLocationPermission) {
            gpsLocationManager?.stopLocationUpdates()
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLocationChanged(location: Location) {
        if (location.latitude != 0.0 && location.longitude != 0.0) {
            requireActivity().runOnUiThread {
                binding.txtDetailDistance.visibility = View.VISIBLE
                binding.txtDetailDistanceHelp.visibility = View.VISIBLE
                binding.txtDetailDistance.text = NavigationUtils.formatDistance(
                    NavigationUtils.distanceInMeter(
                        location.latitude,
                        location.longitude,
                        java.lang.Double.parseDouble(item?.latitude ?: "0"),
                        java.lang.Double.parseDouble(item?.longitude ?: "0")
                    )
                )
            }
        } else {
            requireActivity().runOnUiThread {
                binding.txtDetailDistance.visibility = View.GONE
                binding.txtDetailDistanceHelp.visibility = View.GONE
            }
        }
    }
}