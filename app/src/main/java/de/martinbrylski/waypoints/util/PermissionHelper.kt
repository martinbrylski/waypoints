package de.martinbrylski.waypoints.util

import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import de.martinbrylski.waypoints.R

object PermissionHelper {

    var hasLocationPermission: Boolean = false
        private set

    fun checkPermission(activity: FragmentActivity) {
        PermissionX.init(activity)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    activity.getString(R.string.permission_explanation_1),
                    activity.getString(R.string.ok),
                    activity.getString(R.string.cancel)
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    activity.getString(R.string.permission_explanation_2),
                    activity.getString(R.string.ok),
                    activity.getString(R.string.cancel)
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    hasLocationPermission = true
                } else {
                    hasLocationPermission = false
                }
            }
    }

}

