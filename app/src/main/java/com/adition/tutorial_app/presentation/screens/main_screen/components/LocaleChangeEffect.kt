package com.adition.tutorial_app.presentation.screens.main_screen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
fun LocaleChangeEffect(onLocaleChanged: (Locale) -> Unit) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val localeTag = locale.toLanguageTag()
    val previousLocaleTag = rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(localeTag) {
        if (previousLocaleTag.value != null && previousLocaleTag.value != localeTag) {
            onLocaleChanged(locale)
        }
        previousLocaleTag.value = localeTag
    }
}
