package ink.moling.mocklocation.ui.dialog

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.R

/**
 * 帮助按钮配置
 *
 * @param text 按钮文本，null 时使用默认文本
 * @param onClick 点击回调
 */
data class HelpButtonConfig(
    val text: String? = null
)

/**
 * 错误对话框
 *
 * @param title 对话框标题
 * @param text 错误描述文本
 * @param stackTrace 堆栈跟踪信息（可选），长按可复制
 * @param helpButton 帮助按钮配置（可选），提供时显示帮助按钮
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun ErrorDialog(
    title: String,
    text: String,
    stackTrace: String? = null,
    helpButton: HelpButtonConfig? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val stackTraceScrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var errorDetails by remember { mutableStateOf(text) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                // 仅在提供 stackTrace 时显示
                if (!stackTrace.isNullOrEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.error_dialog_text_stack_trace),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = stackTrace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .verticalScroll(state = stackTraceScrollState)
                            .combinedClickable(
                                onClick = { /* 可选 */ },
                                onLongClick = {
                                    clipboardManager.setText(AnnotatedString(stackTrace))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_dialog_toast_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                    )
                }
            }
        },
        dismissButton = helpButton?.let {
            {
                Button(
                    onClick = {
                        if (!it.text.isNullOrEmpty()) {
                            errorDetails = it.text
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(stringResource(R.string.error_dialog_button_help))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.error_dialog_button_ok))
            }
        }
    )
}

