package com.emrepbu.loginflow.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.emrepbu.loginflow.domain.manager.LanguageManager
import com.emrepbu.loginflow.domain.model.Language
import timber.log.Timber
import java.util.Locale
import androidx.core.content.edit

/**
 * Language manager for Compose
 */
object ComposeLanguageUtil {

    /**
     * Set app language and recreate activity
     */
    fun setAppLanguage(language: Language, context: Context) {
        // Get activity
        val activity = context.findActivity()

        // Save pref
        val prefs = context.getSharedPreferences(LanguageManager.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(LanguageManager.LANGUAGE_KEY, language.code)
        }

        // Update locale
        updateLocale(context, language.code)

        // Recreate if available
        activity?.let {
            Timber.d("CmpUtil: recreate-lang=${language.code}")
            val intent = Intent(activity, activity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    /**
     * Update locale config
     */
    fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        return context.createConfigurationContext(configuration)
    }

    /**
     * Find activity from context
     */
    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}

/**
 * Get current locale
 */
@Composable
@ReadOnlyComposable
fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        configuration.locale
    }
}

/**
 * Get localized string
 */
@Composable
fun localizedStringResource(resId: Int): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(resId, configuration) {
        context.createConfigurationContext(configuration).getString(resId)
    }
}