package ink.moling.mocklocation.service.joystickService

import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import ink.moling.mocklocation.ui.FloatingScreen

class JoystickService : LifecycleService(), SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    // 添加 ViewModelStore 和 SavedStateRegistry 支持
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    // 实现 SavedStateRegistryOwner 接口
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showFloatingWindow()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    override fun onDestroy() {
        super.onDestroy()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        store.clear()
    }

    private fun showFloatingWindow() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 300
        }

        composeView = ComposeView(this).apply {
            // 关键：手动设置三个 ViewTree owners
            setViewTreeLifecycleOwner(this@JoystickService)
            setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = store
            })
            setViewTreeSavedStateRegistryOwner(object : SavedStateRegistryOwner {
                override val savedStateRegistry: SavedStateRegistry
                    get() = savedStateRegistryController.savedStateRegistry
                override val lifecycle: Lifecycle
                    get() = this@JoystickService.lifecycle
            })

            setContent {
                FloatingScreen(
                    windowManager = windowManager,
                    composeView = composeView,
                    params = params
                )
            }
        }

        windowManager.addView(composeView, params)
    }
}