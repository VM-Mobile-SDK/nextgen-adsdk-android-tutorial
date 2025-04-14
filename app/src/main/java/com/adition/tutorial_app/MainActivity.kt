package com.adition.tutorial_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.AdService.init
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.exception.AdError
import com.adition.sdk_core.api.entities.request.AdRequest
import com.adition.sdk_presentation_compose.api.Ad
import com.adition.tutorial_app.ui.theme.TutorialAppTheme
import kotlinx.coroutines.launch

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
                            AdView()
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

@Composable
fun AdView() {
    val adViewModel by remember { mutableStateOf(AdViewModel()) }
    adViewModel.advertisementState.value?.let {
        when(it) {
            is ResultState.Error -> {
                Text(it.exception.description)
            }
            is ResultState.Success -> {
                it.data.adMetadata
                Ad(
                    it.data,
                    modifier = Modifier.aspectRatio(adViewModel.aspectRatio)
                )
            }
        }
    }
}

class AdViewModel: ViewModel() {
    private val adRequest = AdRequest("4810915")
    var advertisementState = mutableStateOf<ResultState<Advertisement>?>(null)
    var aspectRatio = 2f

    init {
        viewModelScope.launch {
            AdService.makeAdvertisement(adRequest).get(
                onSuccess = {
                    aspectRatio = it.adMetadata?.aspectRatio ?: aspectRatio
                    advertisementState.value = ResultState.Success(it)
                },
                onError = {
                    Log.e("AdViewModel", "Failed makeAdvertisement: ${it.description}")
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }
}