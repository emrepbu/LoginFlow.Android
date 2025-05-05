package com.emrepbu.loginflow

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.emrepbu.loginflow.domain.manager.LanguageManager
import com.emrepbu.loginflow.presentation.common.LanguageSelector
import com.emrepbu.loginflow.presentation.common.Navigation
import com.emrepbu.loginflow.ui.theme.LoginFlowTheme
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        // Get stored language code from preferences
        val sharedPreferences = newBase.getSharedPreferences(
            LanguageManager.PREFS_NAME, Context.MODE_PRIVATE
        )
        val languageCode = sharedPreferences.getString(LanguageManager.LANGUAGE_KEY, null)

        // Apply language configuration if available
        val context = if (languageCode != null) {
            try {
                Timber.d("MA: attach-lang=$languageCode")

                // Create configuration with selected locale
                val locale = Locale(languageCode)
                Locale.setDefault(locale)

                val config = Configuration(newBase.resources.configuration)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocales(android.os.LocaleList(locale))
                } else {
                    @Suppress("DEPRECATION")
                    config.locale = locale
                }

                newBase.createConfigurationContext(config)
            } catch (e: Exception) {
                Timber.e(e, "MA: attach-err")
                newBase
            }
        } else {
            Timber.d("MA: attach-default")
            newBase
        }

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if language was passed in intent (from recreation)
        intent.getStringExtra(LanguageManager.LANGUAGE_KEY)?.let { languageCode ->
            try {
                Timber.d("MA: intent-lang=$languageCode")

                // Update locale
                val locale = Locale(languageCode)
                Locale.setDefault(locale)

                // Create new configuration
                val config = Configuration(resources.configuration)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocales(android.os.LocaleList(locale))
                } else {
                    @Suppress("DEPRECATION")
                    config.locale = locale
                }

                // Update resources
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)

                // Create context with updated configuration
                createConfigurationContext(config)

                Timber.d("MA: applied-lang=$languageCode")
            } catch (e: Exception) {
                Timber.e(e, "MA: intent-lang-err")
            }
        }

        setContent {
            // Collect current language to trigger recomposition when language changes
            val currentLanguage by languageManager.currentLanguage.collectAsState()

            Timber.d("MA: render-lang=${currentLanguage.code}")

            LoginFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()

                        Navigation(
                            navController = navController,
                            googleSignInClient = googleSignInClient
                        )

                        // Language selector at top-end of the screen
                        LanguageSelector(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = { language ->
                                Timber.d("MA: lang-select=${language.code}")

                                // Configure for the selected language
                                val locale = Locale(language.code)
                                Locale.setDefault(locale)

                                // Update configuration and recreate
                                val config = Configuration(resources.configuration)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    config.setLocales(android.os.LocaleList(locale))
                                } else {
                                    @Suppress("DEPRECATION")
                                    config.locale = locale
                                }

                                @Suppress("DEPRECATION")
                                resources.updateConfiguration(config, resources.displayMetrics)

                                // Save preference and update state
                                languageManager.setLanguage(language, this@MainActivity)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("MA: resume-locale=${resources.configuration.locales.get(0)}")
    }
}