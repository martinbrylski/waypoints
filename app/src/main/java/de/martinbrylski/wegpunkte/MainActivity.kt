package de.martinbrylski.wegpunkte

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import de.martinbrylski.wegpunkte.data.AppDatabase
import de.martinbrylski.wegpunkte.data.WaypointDao
import de.martinbrylski.wegpunkte.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var waypointAdapter: WaypointAdapter
    private lateinit var waypointDao: WaypointDao
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                showPermissionDeniedMessage()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "Die Standortberechtigung wurde nicht erteilt. Bitte aktiviere die Berechtigung in den Einstellungen.",
            Toast.LENGTH_LONG
        ).show()

        // Optional: Füge einen Intent hinzu, um die App-Einstellungen zu öffnen
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingsIntent.data = Settings.ACTION_APPLICATION_DETAILS_SETTINGS.toUri()
        startActivity(settingsIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (hasLocationPermission()) {
            // ignore
        } else {
            requestLocationPermission()
        }

        val database = AppDatabase.getDatabase(this)
        waypointDao = database.waypointDao()

        val waypoints = runBlocking {
            waypointDao.getAllWaypoints()
        }

        waypointAdapter = WaypointAdapter(waypoints, this)

        binding.recyclerViewWaypoints.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = waypointAdapter
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            performSearch(query)
        }
    }

    private fun performSearch(query: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            if (!query.isNullOrBlank()) {
                val waypoints = waypointDao.searchWaypoints("%$query%")
                runOnUiThread {
                    waypointAdapter.updateWaypoints(waypoints)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_searchable, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isIconifiedByDefault = false

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }

        })
        return true
    }
}