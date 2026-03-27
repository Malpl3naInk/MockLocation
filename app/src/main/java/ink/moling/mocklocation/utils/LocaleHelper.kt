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
        val locale = getLocaleFromLanguage(context, language)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getLocale(context: Context): Locale {
        return getLocaleFromLanguage(context, PrefsHelper.getLanguage(context))
    }

    /**
     * 获取 Locale，对于 SYSTEM 语言使用保存的系统原始语言
     */
    private fun getLocaleFromLanguage(context: Context, language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.SYSTEM -> {
                // 使用保存的系统原始语言，如果没有保存则使用当前 Locale.getDefault()
                val savedSystemLocale = PrefsHelper.getSystemLocale(context)
                if (savedSystemLocale != null) {
                    Locale(savedSystemLocale)
                } else {
                    Locale.getDefault()
                }
            }
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
        return getLocaleFromLanguage(context, PrefsHelper.getLanguage(context))
    }

    /**
     * 初始化系统语言，应在应用启动时调用一次
     */
    fun initSystemLocale(context: Context) {
        if (PrefsHelper.getSystemLocale(context) == null) {
            val systemLocale = Locale.getDefault().language
            PrefsHelper.setSystemLocale(context, systemLocale)
        }
    }
}
