package ink.moling.mocklocation.data

import ink.moling.mocklocation.data.models.RoutePoint

/**
 * 测试数据点
 */
private fun routeTestPoints(): List<RoutePoint> {
    return listOf(
        RoutePoint(0, 30.31278782, 120.37452974, "R", listOf(1, 36)),
        RoutePoint(1, 30.31270001, 120.37486252, "R", listOf(0, 2)),
        RoutePoint(2, 30.31255188, 120.37488464, "R", listOf(1, 3, 5)),
        RoutePoint(3, 30.31176745, 120.37448947, "R", listOf(2, 4)),
        RoutePoint(4, 30.31151951, 120.37454468, "W", listOf(3, 9, 14, 37)),
        RoutePoint(5, 30.31254331, 120.37556238, "R", listOf(2, 6, 28)),
        RoutePoint(6, 30.31215474, 120.37559495, "R", listOf(5, 15)),
        RoutePoint(7, 30.31089503, 120.37400104, "R", listOf(7, 29)),
        RoutePoint(8, 30.31099922, 120.37415835, "R", listOf(7, 9)),
        RoutePoint(9, 30.31107804, 120.37453341, "W", listOf(4, 8, 10)),
        RoutePoint(10, 30.31109505, 120.37481889, "R", listOf(9, 11)),
        RoutePoint(11, 30.31099153, 120.37506155, "R", listOf(10, 12)),
        RoutePoint(12, 30.31101573, 120.37525065, "R", listOf(11, 13)),
        RoutePoint(13, 30.31107460, 120.37518060, "R", listOf(12, 14)),
        RoutePoint(14, 30.31148371, 120.37516653, "R", listOf(4, 13, 15)),
        RoutePoint(15, 30.31152987, 120.37561017, "R", listOf(6, 14, 16)),
        RoutePoint(16, 30.31159333, 120.37585899, "L", listOf(15, 17, 28)),
        RoutePoint(17, 30.31143965, 120.37597601, "L", listOf(16, 18)),
        RoutePoint(18, 30.31133080, 120.37615380, "L", listOf(17, 19)),
        RoutePoint(19, 30.31130983, 120.37634747, "L", listOf(18, 20)),
        RoutePoint(20, 30.31135689, 120.37654047, "L", listOf(19, 21)),
        RoutePoint(21, 30.31155897, 120.37677538, "L", listOf(20, 22)),
        RoutePoint(22, 30.31258439, 120.37678427, "L", listOf(21, 23)),
        RoutePoint(23, 30.31272915, 120.37669964, "L", listOf(22, 24)),
        RoutePoint(24, 30.31282787, 120.37658566, "L", listOf(23, 25)),
        RoutePoint(25, 30.31290106, 120.37630929, "L", listOf(24, 26)),
        RoutePoint(26, 30.31281503, 120.37600992, "L", listOf(25, 27)),
        RoutePoint(27, 30.31271835, 120.37591156, "L", listOf(26, 28)),
        RoutePoint(28, 30.31262546, 120.37586177, "L", listOf(5, 16, 27)),
        RoutePoint(29, 30.31101727, 120.37364696, "R", listOf(7, 30)),
        RoutePoint(30, 30.31146107, 120.37361030, "R", listOf(29, 31, 37)),
        RoutePoint(31, 30.31157536, 120.37357460, "R", listOf(30, 32, 38)),
        RoutePoint(32, 30.31163393, 120.37349810, "R", listOf(31, 33)),
        RoutePoint(33, 30.31195135, 120.37347551, "R", listOf(32, 34)),
        RoutePoint(34, 30.31261212, 120.37348310, "R", listOf(33, 35)),
        RoutePoint(35, 30.31272823, 120.37365519, "R", listOf(34, 36)),
        RoutePoint(36, 30.31274091, 120.37431647, "W", listOf(0, 35, 44)),
        RoutePoint(37, 30.31154134, 120.37368335, "R", listOf(4, 30, 38)),
        RoutePoint(38, 30.31165234, 120.37367430, "W", listOf(31, 37, 39)),
        RoutePoint(39, 30.31208896, 120.37388341, "W", listOf(38, 40)),
        RoutePoint(40, 30.31215410, 120.37396471, "W", listOf(39, 41)),
        RoutePoint(41, 30.31225064, 120.37398847, "W", listOf(40, 42)),
        RoutePoint(42, 30.31235995, 120.37402515, "W", listOf(41, 43)),
        RoutePoint(43, 30.31242018, 120.37407256, "W", listOf(42, 44)),
        RoutePoint(44, 30.31250551, 120.37406562, "W", listOf(36, 43)),
    )
}