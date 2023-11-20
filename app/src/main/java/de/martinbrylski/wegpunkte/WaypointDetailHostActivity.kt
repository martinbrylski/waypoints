package de.martinbrylski.wegpunkte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.permissionx.guolindev.PermissionX
import de.martinbrylski.wegpunkte.databinding.ActivityWaypointDetailBinding

class WaypointDetailHostActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Um die Entfernung und die Karte der Wegpunkte anzuzeigen sind bestimmte Berechtigungen notwendig.",
                    "OK",
                    "Abbrechen"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "Du musst alle Berechtigungen in den Einstellungen manuell freigeben.",
                    "OK",
                    "Abbrechen"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val binding = ActivityWaypointDetailBinding.inflate(layoutInflater)
                    setContentView(binding.root)

                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_waypoint_detail) as NavHostFragment
                    val navController = navHostFragment.navController
                    appBarConfiguration = AppBarConfiguration(navController.graph)
                    setupActionBarWithNavController(navController, appBarConfiguration)
                } else {
                    // nop
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_waypoint_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}
