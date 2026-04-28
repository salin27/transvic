package au.id.transvic

import androidx.room.*

// @Entity tells Room this is a database table called "stops"
@Entity(tableName = "stops")
data class StopEntity(
    @PrimaryKey val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val locationType: Int = 0
)

// DAO = Data Access Object — defines how we read/write to the stops table
@Dao
interface StopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stops: List<StopEntity>)

    // Search stops by name — % means "anything before/after the query"
    @Query("SELECT * FROM stops WHERE stopName LIKE '%' || :query || '%' ORDER BY stopName LIMIT 30")
    suspend fun search(query: String): List<StopEntity>

    @Query("SELECT COUNT(*) FROM stops")
    suspend fun count(): Int
}