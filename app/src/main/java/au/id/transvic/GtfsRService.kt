package au.id.transvic

import com.google.transit.realtime.GtfsRealtime.FeedMessage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Fetches live vehicle data from the Transport Victoria GTFS-R API.
 * The API returns protobuf binary data — FeedMessage.parseFrom() reads it.
 */
object GtfsRService {

    private val client = OkHttpClient()

    // Base URL for all GTFS-R feeds
    private const val BASE = "https://api.opendata.transport.vic.gov.au"

    // The API key from your OpenData portal — loaded from BuildConfig
    private val apiKey = BuildConfig.GTFS_R_KEY

    /**
     * Fetches a GTFS-R feed and returns a parsed FeedMessage.
     * feedPath example: "/gtfs-r/metro-train/vehicle-positions"
     */
    fun fetchFeed(feedPath: String): FeedMessage {
        val request = Request.Builder()
            .url("$BASE$feedPath")
            .header("KeyID", apiKey)
            .build()

        client.newCall(request).execute().use { response ->
            android.util.Log.d("GtfsR", "URL: $BASE$feedPath")
            android.util.Log.d("GtfsR", "Key: $apiKey")
            android.util.Log.d("GtfsR", "Response code: ${response.code}")
            android.util.Log.d("GtfsR", "Response message: ${response.message}")
            if (!response.isSuccessful) {
                val body = response.body?.string() ?: "no body"
                android.util.Log.d("GtfsR", "Error body: $body")
                throw IOException("API error: ${response.code} ${response.message} — $body")
            }
            return FeedMessage.parseFrom(response.body!!.bytes())
        }
    }
    // Convenience function — fetch metro train vehicle positions
    fun getMetroTrainPositions(): FeedMessage {
        return fetchFeed("/opendata/public-transport/gtfs/realtime/v1/metro/vehicle-positions")
    }

    // Convenience function — fetch metro bus positions
    fun getBusPositions(): FeedMessage {
        return fetchFeed("/gtfs-r/metro-bus/vehicle-positions")
    }

    // Convenience function — fetch tram positions
    fun getTramPositions(): FeedMessage {
        return fetchFeed("/gtfs-r/yarra-trams/vehicle-positions")
    }
}