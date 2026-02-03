package ink.moling.mocklocation.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MockRouteDao {
    @Insert suspend fun insert(route: MockRouteEntity)
    @Insert suspend fun insertAll(route: List<MockRouteEntity>)
    @Query("SELECT * FROM saved_routes")
    suspend fun getAll(): List<MockRouteEntity>
    @Query("SELECT * FROM saved_routes WHERE id = :id")
    suspend fun getById(id: Long): MockRouteEntity?
    @Query("SELECT * FROM saved_routes WHERE name LIKE '%' || :name || '%'")
    suspend fun searchByName(name: String): List<MockRouteEntity>
    @Query("DELETE FROM saved_routes WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("DELETE FROM saved_routes")
    suspend fun clearAll()
}