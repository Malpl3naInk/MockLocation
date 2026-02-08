package ink.moling.mocklocation.service.overlayService

import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
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

const val ACTION_TOGGLE_JOYSTICK_VISIBILITY = "ink.moling.mocklocation.ACTION_TOGGLE_JOYSTICK_VISIBILITY"
const val EXTRA_VISIBILITY = "extra_visibility"
const val EXTRA_DISPLAY_MODE = "extra_display_mode"

class OverlayService : LifecycleService(), SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    
    private var isVisible: Boolean = true

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
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_TOGGLE_JOYSTICK_VISIBILITY -> {
                    val visibility = it.getBooleanExtra(EXTRA_VISIBILITY, !isVisible)
                    setVisibility(visibility)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

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
            x = 60
            y = 900
        }

        composeView = ComposeView(this).apply {
            // 关键：手动设置三个 ViewTree owners
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = store
            })
            setViewTreeSavedStateRegistryOwner(object : SavedStateRegistryOwner {
                override val savedStateRegistry: SavedStateRegistry
                    get() = savedStateRegistryController.savedStateRegistry
                override val lifecycle: Lifecycle
                    get() = this@OverlayService.lifecycle
            })

            setContent {
                OverlayScreen(
                    windowManager = windowManager,
                    composeView = composeView,
                    params = params
                )
            }
        }

        windowManager.addView(composeView, params)
    }
    
    private fun setVisibility(visible: Boolean) {
        if (!::composeView.isInitialized) return
        
        isVisible = visible
        composeView.visibility = if (visible) View.VISIBLE else View.GONE
    }
}