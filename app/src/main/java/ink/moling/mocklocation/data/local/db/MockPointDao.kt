package ink.moling.mocklocation.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MockPointDao {
    @Insert suspend fun insert(point: MockPointEntity)
    @Insert suspend fun insertAll(points: List<MockPointEntity>)
    @Query("SELECT * FROM saved_points")
    suspend fun getAll(): List<MockPointEntity>
    @Query("SELECT * FROM saved_points WHERE id = :id")
    suspend fun getById(id: Long): MockPointEntity?
    @Query("DELETE FROM saved_points WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("DELETE FROM saved_points")
    suspend fun clearAll()
}