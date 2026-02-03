package ink.moling.mocklocation.utils.extensions

import androidx.compose.foundation.text.input.TextFieldBuffer

fun TextFieldBuffer.replace(text: CharSequence): Unit =
    replace(0, length, text)