package au.id.transvic

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StopEntity::class, RouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TransvicDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDao

    companion object {
        // Singleton — only one database instance ever created
        @Volatile private var INSTANCE: TransvicDatabase? = null

        fun getInstance(context: Context): TransvicDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TransvicDatabase::class.java,
                    "transvic.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}