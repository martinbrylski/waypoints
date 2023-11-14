package de.martinbrylski.wegpunkte

import android.app.Application
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.MaterialCommunityModule
import com.joanzapata.iconify.fonts.MaterialModule
import timber.log.Timber

class WaypointsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initIcons()
        initLogging()
    }

    private fun initLogging() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initIcons() {
        Iconify.with(MaterialModule()).with(MaterialCommunityModule())
    }
}