package com.adition.tutorial_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adition.tutorial_app.ui.theme.TutorialAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel = MainViewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.adServiceStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                setContent {
                    TutorialAppTheme {
                        Greeting(name = "Android")
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TutorialAppTheme {
        Greeting("Android")
    }
}

