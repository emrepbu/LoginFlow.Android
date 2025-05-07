package com.emrepbu.loginflow

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Locale

@HiltAndroidApp
class LoginFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Apply saved language
        applyLanguageFromPreferences()
    }

    override fun attachBaseContext(base: Context) {
        val sharedPreferences = base.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("language_code", null)

        val context = if (languageCode != null) {
            // Apply configuration
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)

            base.createConfigurationContext(config)
        } else {
            base
        }

        super.attachBaseContext(context)
    }

    private fun applyLanguageFromPreferences() {
        try {
            val prefs = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
            val languageCode = prefs.getString("language_code", null) ?: return

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = resources.configuration
            config.setLocale(locale)

            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)

            Timber.d("App: pref-lang=$languageCode")
        } catch (e: Exception) {
            Timber.e(e, "App: pref-lang-err")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Timber.d("App: config-changed=${newConfig.locales.get(0)}")
    }
}