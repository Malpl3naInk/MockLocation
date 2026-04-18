package ink.moling.mocklocation.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import ink.moling.mocklocation.BuildConfig

object HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "MockLocationHookAPI"
            isEnable = true
            isRecord = false
            elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
        }
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = YukiHookAPI.encase {
        YLog.debug(packageName)
        // Your code here.
    }
}