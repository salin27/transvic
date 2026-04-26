package au.id.transvic

// Simple data class to hold the info we want from each vehicle
data class VehiclePosition(
    val id: String,           // unique vehicle ID
    val routeId: String,      // which route it's on (e.g. "1" for Alamein)
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,       // direction it's heading in degrees (0 = north)
    val type: VehicleType
)

enum class VehicleType { TRAIN, TRAM, BUS }