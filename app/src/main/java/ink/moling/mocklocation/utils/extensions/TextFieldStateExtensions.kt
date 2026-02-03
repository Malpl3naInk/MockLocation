package ink.moling.mocklocation.utils.extensions

import androidx.compose.foundation.text.input.TextFieldState

fun TextFieldState.toDouble(): Double =
    this.text.toString().toDouble()

fun TextFieldState.isNumber(): Boolean =
    this.text.toString().toDoubleOrNull() != null

fun TextFieldState.isValidLat(): Boolean =
    this.toDouble() in -90.0..90.0

fun TextFieldState.isValidLng(): Boolean =
    this.toDouble() in -180.0..180.0

fun TextFieldState.isValidAlt(): Boolean =
    this.toDouble() in -10000.0..100000.0
