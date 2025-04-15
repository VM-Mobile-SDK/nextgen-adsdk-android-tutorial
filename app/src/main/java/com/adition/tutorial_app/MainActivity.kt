package com.adition.tutorial_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.adition.sdk_core.api.entities.exception.AdError
import com.adition.tutorial_app.ui.theme.TutorialAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        app.adServiceStatus.observe(this) { result ->
            when(result) {
                is ResultState.Error -> {
                    showAppError(result.exception)
                }

                is ResultState.Success -> {
                    setContent {
                        TutorialAppTheme {
                            Navigation()
                        }
                    }
                }
            }
        }
    }

    private fun showAppError(adError: AdError) {
        Toast.makeText(this, "Initialization failed: ${adError.description}", Toast.LENGTH_LONG).show()
    }
}