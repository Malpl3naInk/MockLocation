package ink.moling.mocklocation.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MockPointEntity::class,
        MockRouteEntity::class
   ],
    version = 1,
    exportSchema = false // 禁用数据库导出 (数据库迁移用)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mockPointDao(): MockPointDao
    abstract fun mockRouteDao(): MockRouteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "MockLocationDB"
                ).build().also { instance = it }
            }
        }
    }
}