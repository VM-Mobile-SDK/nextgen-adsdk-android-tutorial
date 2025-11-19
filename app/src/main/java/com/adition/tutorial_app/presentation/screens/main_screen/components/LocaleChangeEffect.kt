package com.adition.tutorial_app.presentation.screens.main_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
fun LocaleChangeEffect(onLocaleChanged: (Locale) -> Unit) {
    val locale = rememberCurrentLocale()

    LaunchedEffect(locale) {
        onLocaleChanged(locale)
    }
}

@Composable
private fun rememberCurrentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return configuration.locales[0]
}
