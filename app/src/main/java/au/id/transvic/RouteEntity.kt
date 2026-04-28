package au.id.transvic

import androidx.room.*

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String,
    val routeShortName: String,
    val routeLongName: String,
    val routeColor: String = "1B7FC4"  // default PTV blue
)

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes WHERE routeId = :routeId")
    suspend fun getRoute(routeId: String): RouteEntity?

    @Query("SELECT * FROM routes")
    suspend fun getAll(): List<RouteEntity>
}