package ink.moling.mocklocation.data.local.repository

import kotlinx.coroutines.flow.MutableStateFlow

object MockServiceStatusRepository {
    val state = MutableStateFlow<MockServiceState>(MockServiceState.Disabled)
}
