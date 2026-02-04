package ink.moling.mocklocation.utils.extensions

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete

fun TextFieldBuffer.replace(text: CharSequence): Unit =
    replace(0, length, text)

fun TextFieldBuffer.delete(): Unit =
    delete(0, length)