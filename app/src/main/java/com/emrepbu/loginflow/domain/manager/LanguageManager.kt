package com.emrepbu.loginflow.domain.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.emrepbu.loginflow.MainActivity
import com.emrepbu.loginflow.domain.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val context: Context
) {
    private val _currentLanguage =
        MutableStateFlow(getLanguageFromPreferences() ?: getDeviceLanguage())
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    fun getCurrentLocale(): Locale {
        return Locale(currentLanguage.value.code)
    }

    fun setLanguage(language: Language, activity: Activity? = null) {
        Timber.d("Lang: set=${language.code}")

        // Save language preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, language.code).apply()

        // Update current language state
        _currentLanguage.value = language

        // Update configuration and locale
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        // Update configuration
        val config = Configuration()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        // Apply to context resources
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // If activity is provided, recreate it to apply language changes immediately
        activity?.let {
            Timber.d("Lang: recreate-activity")

            try {
                // Force recreate the activity
                val intent = Intent(activity, activity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                // Add the locale to the intent for the new activity
                intent.putExtra(LANGUAGE_KEY, language.code)

                // Start the new activity
                activity.startActivity(intent)

                // Add animation for smooth transition
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                // Finish the current activity
                activity.finish()
            } catch (e: Exception) {
                Timber.e(e, "Lang: recreate-err")
            }
        }
    }

    fun updateConfiguration(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        // Update configuration on the context
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        Timber.d("Lang: config-updated=$locale")
    }

    fun getLanguageFromPreferences(): Language? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(LANGUAGE_KEY, null) ?: return null
        Timber.d("Lang: prefs=$languageCode")
        return Language.values().find { it.code == languageCode }
    }

    private fun getDeviceLanguage(): Language {
        val deviceLocale = Locale.getDefault().language
        Timber.d("Lang: device=$deviceLocale")
        return Language.values().find { it.code == deviceLocale } ?: Language.ENGLISH
    }

    companion object {
        const val PREFS_NAME = "language_prefs"
        const val LANGUAGE_KEY = "language_code"
    }
}