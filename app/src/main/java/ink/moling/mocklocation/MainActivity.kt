package ink.moling.mocklocation

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ink.moling.mocklocation.ui.theme.MockLocationTheme

class MainActivity : ComponentActivity() {
    lateinit var mServiceBinder: MockLocationService.MockLocationServiceBinder
    lateinit var mConnection: ServiceConnection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?,service: IBinder?) {
                mServiceBinder = service as MockLocationService.MockLocationServiceBinder
            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }

        }

        setContent {
            MockLocationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isMocking by rememberSaveable { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            Button(
                                onClick = {
                                    if (isMocking) stopMockLocation() else startMockLocation()
                                    isMocking = !isMocking
                                }
                            ) {
                                val buttonLabel = if (isMocking) "Stop" else "Start"
                                Text(text = buttonLabel)
                            }
                            Button(
                                onClick = {
                                    mServiceBinder.setPosition(53.4519076,-3.0029668,48.0)
                                }
                            ) {
                                Text(text = "Change Location")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMockLocation() {
        val serviceMockLocation = Intent(this, MockLocationService::class.java)
        bindService(serviceMockLocation, mConnection, BIND_AUTO_CREATE) // 绑定服务与活动
        startForegroundService(serviceMockLocation)
    }

    private fun stopMockLocation() {
        unbindService(mConnection)
        val serviceMockLocation = Intent(this, MockLocationService::class.java)
        stopService(serviceMockLocation)
    }
}