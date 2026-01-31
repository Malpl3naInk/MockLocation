package ink.moling.mocklocation.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * 错误对话框
 */
@Composable
@Suppress("DEPRECATION")
fun ErrorDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    stackTrace: String
) {
    val context = LocalContext.current
    val stackTraceScrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        text = {
            Column {
                Text(text)
                Text(
                    stackTrace,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 20.dp)
                        .verticalScroll(state = stackTraceScrollState)
                        .combinedClickable(
                            onClick = { /* 可选 */ },
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(stackTrace))
                                Toast.makeText(
                                    context,
                                    "Stack trace copied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

