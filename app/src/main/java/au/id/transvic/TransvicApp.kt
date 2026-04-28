package au.id.transvic

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransvicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Import GTFS data in the background when the app first launches
        CoroutineScope(Dispatchers.IO).launch {
            GtfsImporter.importIfNeeded(this@TransvicApplication)
        }
    }
}