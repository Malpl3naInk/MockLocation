package ink.moling.mocklocation.utils

import ink.moling.mocklocation.utils.logger.Logger

enum class LatLngInputType { LAT, LNG, ALT }

fun isValidLatLngInput(
    input: String,
    type: LatLngInputType
): Boolean {
    // 允许输入中状态
    if (input.isEmpty() || input == "-" || input == "." || input == "-.") {
        Logger.d("isValidLatLngInput", "$input is valid through incompleted")
        return true
    }

    // 基础格式校验
    if (!input.matches(Regex("^-?\\d*(\\.\\d*)?$"))) {
        Logger.d("isValidLatLngInput", "$input is invalid through regex")
        return false
    }

    // 数值范围校验
    val value = input.toDoubleOrNull() ?: return true
    Logger.d("isValidLatLngInput", "$input value is $value")

    return when (type) {
        LatLngInputType.LAT -> value in -90.0..90.0
        LatLngInputType.LNG -> value in -180.0..180.0
        LatLngInputType.ALT -> value in -10000.0..100000.0
    }
}
