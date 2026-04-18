package ink.moling.mocklocation.hook

import android.location.Location
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import ink.moling.mocklocation.BuildConfig
import ink.moling.mocklocation.data.models.CandidateLocation

@InjectYukiHookWithXposed
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
        loadSystem {
            YLog.info("Current package name: $packageName")
            Location::class.resolve().apply {
                firstMethod {
                    name = "getLatitude"
                    parameters(Double::class)
                }.hook {
                    after {
                        result = CandidateLocation.Default.lat
                    }
                }
                firstMethod {
                    name = "getLongitude"
                    parameters(Double::class)
                }.hook {
                    after {
                        result = CandidateLocation.Default.lng
                    }
                }
                firstMethod {
                    name = "getAccuracy"
                    parameters(Float::class)
                }.hook {
                    after {
                        result = CandidateLocation.Default.accuracy
                    }
                }
            }
        }
    }
}