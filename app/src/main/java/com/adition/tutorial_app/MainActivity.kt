package com.adition.tutorial_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adition.tutorial_app.ui.theme.TutorialAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        app.adServiceStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                setContent {
                    TutorialAppTheme {
                        Greeting(name = "AdSDK")
                    }
                }
            } else {
                showAppError()
            }

        }
    }

    private fun showAppError() {
        Toast.makeText(this, "Initialization failed", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
