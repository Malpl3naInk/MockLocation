package ink.moling.mocklocation.utils

import android.util.Log

enum class LatLngInputType { LAT, LNG }

fun isValidLatLngInput(
    input: String,
    type: LatLngInputType
): Boolean {
    // 允许输入中状态
    if (input.isEmpty() || input == "-" || input == "." || input == "-.") {
        Log.d("isValidLatLngInput", "$input is valid through incompleted")
        return true
    }

    // 基础格式校验
    if (!input.matches(Regex("^-?\\d*(\\.\\d*)?$"))) {
        Log.d("isValidLatLngInput", "$input is invalid through regex")
        return false
    }

    // 数值范围校验
    val value = input.toDoubleOrNull() ?: return true
    Log.d("isValidLatLngInput", "$input value is $value")

    return when (type) {
        LatLngInputType.LAT -> value in -90.0..90.0
        LatLngInputType.LNG -> value in -180.0..180.0
    }
}
