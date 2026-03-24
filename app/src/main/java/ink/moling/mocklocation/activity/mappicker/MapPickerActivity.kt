package ink.moling.mocklocation.activity.mappicker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ink.moling.mocklocation.ui.theme.MockLocationTheme

class MapPickerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_INITIAL_LATITUDE = "initial_latitude"
        const val EXTRA_INITIAL_LONGITUDE = "initial_longitude"
        const val EXTRA_RESULT_LATITUDE = "result_latitude"
        const val EXTRA_RESULT_LONGITUDE = "result_longitude"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialLat = intent.getDoubleExtra(EXTRA_INITIAL_LATITUDE, 0.0)
        val initialLng = intent.getDoubleExtra(EXTRA_INITIAL_LONGITUDE, 0.0)

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapPickerScreen(
                        initialLat = initialLat,
                        initialLng = initialLng,
                        onConfirm = { lat, lng ->
                            val resultIntent = Intent().apply {
                                putExtra(EXTRA_RESULT_LATITUDE, lat)
                                putExtra(EXTRA_RESULT_LONGITUDE, lng)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        },
                        onCancel = {
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
