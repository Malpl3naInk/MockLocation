package ink.moling.mocklocation.utils

import android.content.Context
import android.content.res.Configuration
import ink.moling.mocklocation.data.local.AppLanguage
import ink.moling.mocklocation.data.local.PrefsHelper
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context): Context {
        val language = PrefsHelper.getLanguage(context)
        return updateLocale(context, language)
    }

    private fun updateLocale(context: Context, language: AppLanguage): Context {
        val locale = getLocaleFromLanguage(language)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getLocale(context: Context): Locale {
        return getLocaleFromLanguage(PrefsHelper.getLanguage(context))
    }

    private fun getLocaleFromLanguage(language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.SYSTEM -> Locale.getDefault()
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.CHINESE -> Locale.CHINESE
        }
    }

    /**
     * 检查语言是否发生变化
     * 通过比较当前 Context 的 Locale 和目标 Locale
     */
    fun hasLanguageChanged(context: Context): Boolean {
        val targetLocale = getTargetLocale(context)
        val currentLocale = context.resources.configuration.locales.get(0)
        return currentLocale.language != targetLocale.language
    }

    /**
     * 获取应该使用的 Locale
     */
    fun getTargetLocale(context: Context): Locale {
        return when (PrefsHelper.getLanguage(context)) {
            AppLanguage.SYSTEM -> Locale.getDefault()
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.CHINESE -> Locale.CHINESE
        }
    }
}
