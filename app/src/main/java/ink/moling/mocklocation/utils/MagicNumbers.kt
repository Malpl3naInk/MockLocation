package ink.moling.mocklocation.utils

class MockMode {
    companion object {
        const val MOCK_MODE_POINT = 0
        const val MOCK_MODE_ROUTE = 1
    }
}

class WaypointSheet {
    companion object {
        const val WAYPOINT_SHEET_POINTS = 0
        const val WAYPOINT_SHEET_DETAIL = 1
    }
}

class WaypointGraph {
    companion object {
        const val WAYPOINT_GRAPH_CANVAS = 0
        const val WAYPOINT_GRAPH_MAP    = 1
    }
}