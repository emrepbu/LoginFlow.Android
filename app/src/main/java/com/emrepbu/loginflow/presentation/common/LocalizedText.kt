package com.emrepbu.loginflow.presentation.common

import androidx.annotation.StringRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Localized text composable
 */
@Composable
fun LocalizedText(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    // Force recomposition
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // Get localized text
    val text = remember(resId, configuration) {
        // Create context with current config
        val configContext = context.createConfigurationContext(configuration)
        configContext.getString(resId)
    }

    Text(
        text = text,
        modifier = modifier,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Get localized string
 */
@Composable
@ReadOnlyComposable
fun localizedStringResource(@StringRes resId: Int): String {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    return context.createConfigurationContext(configuration).getString(resId)
}