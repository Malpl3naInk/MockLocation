package ink.moling.mocklocation.utils.extensions

import android.location.LocationManager

fun LocationManager.isProviderAvailable(provider: String): Boolean {
    return this.allProviders.contains(provider)
}