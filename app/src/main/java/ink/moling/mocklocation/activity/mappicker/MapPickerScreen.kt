package ink.moling.mocklocation.activity.mappicker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import ink.moling.mocklocation.R
import ink.moling.mocklocation.data.models.CandidateLocation
import ink.moling.mocklocation.data.models.Source
import ink.moling.mocklocation.service.locationService.LocationService
import kotlinx.coroutines.launch

@OptIn(MapboxDelicateApi::class)
@Composable
fun MapPickerScreen(
    initialLat: Double,
    initialLng: Double,
    onConfirm: (Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapState = rememberMapState()
    var currentCenter by remember { mutableStateOf<Point?>(null) }
    var currentLocation by remember { mutableStateOf<CandidateLocation?>(null) }
    var locationBinder by remember { mutableStateOf<LocationService.MockLocationServiceBinder?>(null) }

    // 绑定 LocationService 获取当前位置
    DisposableEffect(context) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                locationBinder = service as? LocationService.MockLocationServiceBinder
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                locationBinder = null
            }
        }

        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(connection)
            locationBinder = null
        }
    }

    // 监听位置更新
    LaunchedEffect(locationBinder) {
        locationBinder?.locationFlow()?.collect { location ->
            currentLocation = location
        }
    }

    // 确定默认中心点：优先使用传入的初始位置，其次使用当前位置，最后使用默认位置
    val defaultCenter = remember(initialLat, initialLng, currentLocation) {
        when {
            initialLat != 0.0 && initialLng != 0.0 -> {
                Point.fromLngLat(initialLng, initialLat)
            }
            currentLocation != null && currentLocation?.source != Source.DEFAULT -> {
                Point.fromLngLat(currentLocation!!.lng, currentLocation!!.lat)
            }
            else -> {
                Point.fromLngLat(0.0005, 51.4769) // 格林尼治天文台
            }
        }
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(17.0)
            center(defaultCenter)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // 标记是否已经执行过初始定位
    var hasAutoCentered by remember { mutableStateOf(false) }

    // 当获取到当前位置且没有初始位置时，自动移动到当前位置（仅执行一次）
    LaunchedEffect(currentLocation) {
        if (!hasAutoCentered &&
            initialLat == 0.0 && initialLng == 0.0 &&
            currentLocation != null &&
            currentLocation?.source != Source.DEFAULT
        ) {
            hasAutoCentered = true
            mapViewportState.flyTo(
                cameraOptions {
                    center(Point.fromLngLat(currentLocation!!.lng, currentLocation!!.lat))
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 地图
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapState = mapState,
            mapViewportState = mapViewportState,
            compass = { Compass(Modifier.padding(top = 80.dp, end = 16.dp)) },
            scaleBar = { ScaleBar(
                Modifier.padding(bottom = 180.dp, start = 16.dp),
                alignment = Alignment.BottomStart
            ) },
            logo = { Logo(Modifier.padding(bottom = 20.dp, start = 16.dp)) },
            attribution = { Attribution(Modifier.padding(bottom = 40.dp, start = 16.dp)) }
        ) {
            // 位置定位
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck()
                    enabled = true
                }
            }
        }

        // 中心标记（参考 WaypointScreen 的实现）
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .padding(bottom = 36.dp)
        )

        // 顶部标题栏和关闭按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // 关闭按钮
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.button_cancel),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 标题
            Text(
                text = stringResource(R.string.map_picker_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 底部按钮栏
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            // 当前坐标显示
            currentCenter?.let { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "%.6f, %.6f".format(point.latitude(), point.longitude()),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 定位到当前位置按钮
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            currentLocation?.let { location ->
                                if (location.source != Source.DEFAULT) {
                                    mapViewportState.flyTo(
                                        cameraOptions {
                                            center(Point.fromLngLat(location.lng, location.lat))
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Outlined.MyLocation, null)
                }

                // 确认按钮
                TextButton(
                    onClick = {
                        currentCenter?.let { point ->
                            onConfirm(point.latitude(), point.longitude())
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = if (currentCenter != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    enabled = currentCenter != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = if (currentCenter != null)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.button_confirm),
                            color = if (currentCenter != null)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // 监听地图中心位置变化
    LaunchedEffect(mapViewportState.cameraState) {
        mapViewportState.cameraState?.center?.let { center ->
            currentCenter = center
        }
    }
}
