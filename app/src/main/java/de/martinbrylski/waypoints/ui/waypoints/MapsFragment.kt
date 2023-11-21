package de.martinbrylski.waypoints.ui.waypoints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import de.martinbrylski.waypoints.R
import de.martinbrylski.waypoints.data.AppDatabase
import de.martinbrylski.waypoints.data.Waypoint
import de.martinbrylski.waypoints.databinding.FragmentMapsBinding
import de.martinbrylski.waypoints.util.FormatUtils
import de.martinbrylski.waypoints.util.PermissionHelper
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapsFragment : Fragment() {
    private var waypoint: Waypoint? = null
    private var _binding: FragmentMapsBinding? = null
    private var gpsOverlay: MyLocationNewOverlay? = null

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
                    waypoint = waypointDao.getWaypointById(it.getLong(ARG_ITEM_ID))
                }
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        Configuration.getInstance().userAgentValue = requireContext().packageName

        (requireActivity() as AppCompatActivity).supportActionBar?.title = waypoint?.name

        if (PermissionHelper.hasLocationPermission) {
            gpsOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.mapview)

            val bitmapNotMoving = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_map_person, null
            )
            val bitmapMoving = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_map_direction, null
            )

            gpsOverlay?.setPersonIcon(FormatUtils.drawableToBitmap(bitmapNotMoving!!))
            gpsOverlay?.setDirectionIcon(FormatUtils.drawableToBitmap(bitmapMoving!!))
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        if (PermissionHelper.hasLocationPermission)
            gpsOverlay?.enableMyLocation()
        gpsOverlay?.isDrawAccuracyEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateContent()
    }

    private fun updateContent() {
        if (waypoint != null) {
            initMap(binding.mapview, waypoint!!)
        }
    }

    private fun initMap(map: MapView, waypoint: Waypoint): Unit {
        // configure map view
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
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

        val compassOverlay = CompassOverlay(
            requireContext(),
            InternalCompassOrientationProvider(requireContext()),
            map
        )
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        val scaleOverlay = ScaleBarOverlay(map)
        scaleOverlay.unitsOfMeasure = ScaleBarOverlay.UnitsOfMeasure.metric
        scaleOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 50)
        scaleOverlay.setCentred(true)
        map.overlays.add(scaleOverlay)

        val rotationOverlay = RotationGestureOverlay(map)
        rotationOverlay.isEnabled = true
        map.overlays.add(rotationOverlay)

        map.overlays.add(currentLocationOverlay)
        if (PermissionHelper.hasLocationPermission)
            map.overlays.add(gpsOverlay)
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onResume()
        if (PermissionHelper.hasLocationPermission)
            gpsOverlay?.disableMyLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}