package ink.moling.mocklocation.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_points")
data class MockPointEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lat: Double,
    val lng: Double,
    val alt: Double
)