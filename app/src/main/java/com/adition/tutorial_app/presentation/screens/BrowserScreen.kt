package com.adition.tutorial_app.presentation.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.adition.tutorial_app.ui.components.AppTopBarContainer
import kotlinx.serialization.Serializable

@Serializable
data class BrowserRoute(val url: String)

@Composable
fun BrowserScreen(
    url: String,
    navController: NavController
) {
    val context = LocalContext.current

    AppTopBarContainer(
        title = "Browser",
        onNavigateBack = { navController.navigateUp() }
    ) {
        AndroidView(
            factory = {
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            update = { it.loadUrl(url) },
            modifier = Modifier.fillMaxSize(),
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            }
        )
    }
}
