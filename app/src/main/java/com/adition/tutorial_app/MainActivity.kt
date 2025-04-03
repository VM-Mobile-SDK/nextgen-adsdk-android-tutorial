package com.adition.tutorial_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adition.sdk_presentation_compose.api.Ad
import com.adition.tutorial_app.ui.theme.TutorialAppTheme

class MainActivity : ComponentActivity() {

    private val adViewModel: AdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        app.adServiceStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                setContent {
                    TutorialAppTheme {
                        AdView(adViewModel)
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
fun AdView(adViewModel: AdViewModel) {
    adViewModel.advertisement.value?.let {
        Ad(advertisement = it)
    }
}
