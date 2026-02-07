package ink.moling.mocklocation.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ink.moling.mocklocation.data.models.RouteObject

@Entity(tableName = "saved_routes")
data class MockRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val details: RouteObject
)
