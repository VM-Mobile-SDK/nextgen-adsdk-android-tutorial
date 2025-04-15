package com.adition.tutorial_app

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.AdInterstitialState
import com.adition.sdk_core.api.entities.request.AdPlacementType
import com.adition.sdk_core.api.entities.request.AdRequest
import com.adition.sdk_core.api.entities.response.AdMetadata
import com.adition.sdk_core.api.services.event_listener.AdEventListener
import com.adition.sdk_core.api.services.event_listener.AdEventType
import com.adition.sdk_presentation_compose.api.Interstitial
import kotlinx.coroutines.launch


@ExperimentalMaterial3Api
@Composable
fun InterstitialScreen() {
    val viewModel: InterstitialAdViewModel = viewModel()
    viewModel.advertisementState.value?.let {
        when (it) {
            is ResultState.Error -> {
                Text(it.exception.description)
            }

            is ResultState.Success -> {
                Button(
                    onClick = {
                        viewModel.interstitialState.presentIfLoaded()
                    },
                    modifier = Modifier
                        .padding(26.dp)
                ) {
                    Text(
                        text = "Show Interstitial",
                    )
                }
                Interstitial(viewModel.interstitialState)
            }
        }
    }
}

class InterstitialAdViewModel : ViewModel() {
    private val adRequest = AdRequest("5192923")
    var advertisementState = mutableStateOf<ResultState<Advertisement>?>(null)
    lateinit var interstitialState: AdInterstitialState

    val adEventListener: AdEventListener = object : AdEventListener {
        override fun eventProcessed(adEventType: AdEventType, adMetadata: AdMetadata) {
            Log.d("InterstitialAdViewModel events", "Collected EVENT - $adEventType")
            if (adEventType == AdEventType.UnloadRequest) {
                interstitialState.hide()
            }
        }
    }

    init {
        viewModelScope.launch {
            AdService.makeAdvertisement(
                adRequest,
                placementType = AdPlacementType.INTERSTITIAL,
                adEventListener = adEventListener
            ).get(
                onSuccess = {
                    interstitialState = AdInterstitialState(it, this)
                    advertisementState.value = ResultState.Success(it)
                },
                onError = {
                    Log.e("InterstitialAdViewModel", "Failed makeAdvertisement: ${it.description}")
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }
}


