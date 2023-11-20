package de.martinbrylski.wegpunkte

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
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import de.martinbrylski.wegpunkte.data.AppDatabase
import de.martinbrylski.wegpunkte.data.Waypoint
import de.martinbrylski.wegpunkte.databinding.FragmentWaypointDetailBinding
import de.martinbrylski.wegpunkte.util.NavigationUtils
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

    /**
     * The placeholder content this fragment is presenting.
     */
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWaypointDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout
        gpsLocationManager = GpsLocationManager(requireContext())
        gpsLocationManager?.setLocationObserver(this)
        gpsLocationManager?.startLocationUpdates()

        Configuration.getInstance().userAgentValue = requireContext().packageName

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateContent()
    }

    private fun updateContent() {
        //toolbarLayout?.title = item?.name
        (requireActivity() as AppCompatActivity).supportActionBar?.title = item?.name

        // Show the placeholder content as text in a TextView.
        item?.let {
            binding.txtDetailArea?.text = String.format("%s (%s)", it.description, it.area)
            binding.txtDetailCoordinates?.text = String.format("%s, %s°", it.latitude, it.longitude)

            binding.btShowAndroidMap?.setOnClickListener {
                Timber.d("Hello")
                val intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "geo:<${item!!.latitude}>,<${item!!.longitude}>?q=<${item!!.latitude}>,<${item!!.longitude}>(${item!!.name})"
                    )
                )

                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)
                } else {
                    var urlAddress =
                        "http://maps.google.com/maps?q=" + item!!.latitude + "," + item!!.longitude + "(" + item!!.name + ")&iwloc=A&hl=es"
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                    startActivity(intent)
                }
            }

            initMap(binding.activityMap!!, item!!)
        }
    }

    private fun initMap(map: MapView, waypoint: Waypoint): Unit {
        with(waypoint) {
            // configure map view
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.setMultiTouchControls(false)
            map.isTilesScaledToDpi = true

            val mapController = map.controller
            mapController.setZoom(18.0)
            val startPoint = GeoPoint(latitude.toDouble(), longitude.toDouble())
            mapController.setCenter(startPoint)

            val myLocationOverlayItem = OverlayItem(name, description, startPoint)
            val myCurrentLocationMarker =
                context?.resources?.getDrawable(R.drawable.ic_location_pointer)
            myLocationOverlayItem.setMarker(myCurrentLocationMarker)

            val items = ArrayList<OverlayItem>()
            items.add(myLocationOverlayItem)

            val currentLocationOverlay = ItemizedIconOverlay<OverlayItem>(context, items,
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
    }

    override fun onPause() {
        super.onPause()
        binding.activityMap?.onResume()
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
                binding.txtDetailDistance?.visibility = View.VISIBLE
                binding.txtDetailDistanceHelp?.visibility = View.VISIBLE
                binding.txtDetailDistance?.text = NavigationUtils.formatDistance(
                    NavigationUtils.distanceInMeter(
                        location.latitude,
                        location.longitude,
                        java.lang.Double.parseDouble(item?.latitude),
                        java.lang.Double.parseDouble(item?.longitude)
                    )
                )
            }
        } else {
            requireActivity().runOnUiThread {
                {
                    binding.txtDetailDistance?.visibility = View.GONE
                    binding.txtDetailDistanceHelp?.visibility = View.GONE
                }
            }
        }
    }
}