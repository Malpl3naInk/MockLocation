package ink.moling.mocklocation.service.overlayService

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ink.moling.mocklocation.service.overlayService.state.OverlayStateHolder
import ink.moling.mocklocation.ui.theme.MockLocationTheme
import ink.moling.mocklocation.utils.LocaleHelper
import ink.moling.mocklocation.utils.logger.Logger

private const val TAG = "OverlayService"

const val ACTION_TOGGLE_JOYSTICK_VISIBILITY = "ink.moling.mocklocation.ACTION_TOGGLE_JOYSTICK_VISIBILITY"
const val EXTRA_VISIBILITY = "extra_visibility"
const val EXTRA_DISPLAY_MODE = "extra_display_mode"

class OverlayService : LifecycleService(), SavedStateRegistryOwner {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.setLocale(it) })
    }

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    private var isVisible: Boolean = true

    /** 悬浮窗是否已成功添加到 WindowManager（addView 失败时为 false） */
    private var isViewAttached: Boolean = false

    // 添加 ViewModelStore 和 SavedStateRegistry 支持
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    // 实现 SavedStateRegistryOwner 接口
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        // TYPE_APPLICATION_OVERLAY 需要 SYSTEM_ALERT_WINDOW 授权。
        // 未授权（用户未授予，或被系统/厂商 ROM 撤销）时 addView 会抛出
        // BadTokenException("permission denied for window type 2038")，
        // 而异常从 Service.onCreate 抛出会导致整个进程致命崩溃，因此必须提前拦截。
        if (!Settings.canDrawOverlays(this)) {
            Logger.e(TAG, "SYSTEM_ALERT_WINDOW not granted, floating window will not be shown")
            stopSelf()
            return
        }

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
            OverlayStateHolder.setMode(
                it.getIntExtra(EXTRA_DISPLAY_MODE, 0)
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        detachFloatingWindow()
        store.clear()
    }

    /**
     * 安全地移除悬浮窗
     *
     * 只有 addView 成功过才允许 removeView，否则会抛出 IllegalArgumentException。
     */
    private fun detachFloatingWindow() {
        if (!isViewAttached) return
        isViewAttached = false
        try {
            windowManager.removeView(composeView)
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to remove floating window", e)
        }
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
                MockLocationTheme {
                    OverlayScreen(
                        windowManager = windowManager,
                        composeView = composeView,
                        params = params
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, params)
            isViewAttached = true
        } catch (e: RuntimeException) {
            // BadTokenException / SecurityException / IllegalStateException：
            // 权限被撤销，或被系统（Android 15+ 及部分 OEM ROM）拒绝添加悬浮窗时，
            // 异常一旦冒泡到 Service.onCreate 就会直接崩溃整个进程，这里降级为记录 + 自我停止。
            Logger.e(TAG, "Failed to add floating window", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            stopSelf()
        }
    }

    private fun setVisibility(visible: Boolean) {
        if (!isViewAttached) return

        isVisible = visible
        composeView.visibility = if (visible) View.VISIBLE else View.GONE
    }
}