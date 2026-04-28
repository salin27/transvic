package au.id.transvic

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val melbourneCentre = GeoPoint(-37.8136, 144.9631)
    var vehicles by remember { mutableStateOf(listOf<VehiclePosition>()) }

    // Auto-refresh every 15 seconds — LaunchedEffect keeps running while screen is visible
    LaunchedEffect(Unit) {
        while (true) {
            val result = withContext(Dispatchers.IO) {
                try {
                    val feed = GtfsRService.getMetroTrainPositions()
                    feed.entityList
                        .filter { it.hasVehicle() && it.vehicle.hasPosition() }
                        .map { entity ->
                            val v = entity.vehicle
                            VehiclePosition(
                                id = entity.id,
                                routeId = v.trip.routeId,
                                latitude = v.position.latitude.toDouble(),
                                longitude = v.position.longitude.toDouble(),
                                bearing = v.position.bearing,
                                type = VehicleType.TRAIN
                            )
                        }
                } catch (e: Exception) { emptyList() }
            }
            vehicles = result
            delay(15_000) // wait 15 seconds then fetch again
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            Configuration.getInstance().apply {
                load(context, android.preference.PreferenceManager
                    .getDefaultSharedPreferences(context))
                userAgentValue = "transvic/1.0 (au.id.transvic)"
            }
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(melbourneCentre)
            }
        },
        // update() runs every time 'vehicles' changes
        update = { mapView ->
            // Remove old vehicle markers
            mapView.overlays.removeIf { it is Marker }

            // Add a marker for each vehicle
            vehicles.forEach { vehicle ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(vehicle.latitude, vehicle.longitude)
                    title = "Route ${vehicle.routeId}"
                    snippet = vehicle.id
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate() // tells the map to redraw
        }
    )
}