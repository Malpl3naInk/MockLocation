package ink.moling.mocklocation.data.local.repository

sealed class MockServiceState {
    object Disabled : MockServiceState()
    object Initializing : MockServiceState()
    object Enabled : MockServiceState()
    data class Error(val type: String, val msg: String, val stackTrace: String) : MockServiceState()
}
