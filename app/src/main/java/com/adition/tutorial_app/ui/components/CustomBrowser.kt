import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CustomBrowser(url: String) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = { webView ->
            webView.stopLoading()
            webView.destroy()
        }
    )
}