package au.id.transvic

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GtfsImporter {

    /**
     * Call this on app launch. If the database is empty, reads stops.txt
     * and routes.txt from assets and imports them into Room.
     * Only runs once — after that the DB is populated and we skip it.
     */
    suspend fun importIfNeeded(context: Context) {
        val db = TransvicDatabase.getInstance(context)

        // Check if we already imported — if count > 0, skip
        if (db.stopDao().count() > 0) return

        withContext(Dispatchers.IO) {
            importStops(context, db)
            importRoutes(context, db)
        }
    }

    private suspend fun importStops(context: Context, db: TransvicDatabase) {
        val stops = mutableListOf<StopEntity>()

        // This regex ensures commas inside quotes (e.g. "Name, Street") don't cause a split
        val csvRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

        context.assets.open("stops.txt").bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (index == 0 || line.isBlank()) return@forEachIndexed

                val parts = line.split(csvRegex)

                // Your data has 10 columns; we need at least up to index 5 for location_type
                if (parts.size < 6) return@forEachIndexed

                try {
                    stops.add(StopEntity(
                        // Indices mapped to: stop_id(0), stop_name(1), stop_lat(2), stop_lon(3), location_type(5)
                        stopId = parts[0].trim().removeSurrounding("\""),
                        stopName = parts[1].trim().removeSurrounding("\""),
                        stopLat = parts[2].trim().removeSurrounding("\"").toDouble(),
                        stopLon = parts[3].trim().removeSurrounding("\"").toDouble(),
                        locationType = parts[5].trim().removeSurrounding("\"").toIntOrNull() ?: 0
                    ))
                } catch (e: Exception) {
                    // Log.e("GtfsImporter", "Error parsing line $index: ${e.message}")
                }
            }
        }

        // Insert in batches to keep memory usage low and improve DB performance
        stops.chunked(500).forEach { batch ->
            db.stopDao().insertAll(batch)
        }
    }

    private suspend fun importRoutes(context: Context, db: TransvicDatabase) {
        val routes = mutableListOf<RouteEntity>()

        context.assets.open("routes.txt").bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (index == 0) return@forEachIndexed
                val parts = line.split(",")
                if (parts.size < 5) return@forEachIndexed

                try {
                    routes.add(RouteEntity(
                        routeId = parts[0].trim(),
                        routeShortName = parts[2].trim().removeSurrounding("\""),
                        routeLongName = parts[3].trim().removeSurrounding("\""),
                        routeColor = parts[7].trim().ifEmpty { "1B7FC4" }
                    ))
                } catch (e: Exception) { }
            }
        }
        db.routeDao().insertAll(routes)
    }
}