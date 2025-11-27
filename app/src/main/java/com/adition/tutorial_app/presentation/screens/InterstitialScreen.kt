package com.adition.tutorial_app.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.entities.AdInterstitialState
import com.adition.ad_sdk.api.entities.request.AdPlacementType
import com.adition.ad_sdk.api.entities.request.AdRequest
import com.adition.ad_sdk.api.presentation.Interstitial
import com.adition.ad_sdk.api.services.event_listener.AdEventListener
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.ui.components.AppTopBarContainer
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object InterstitialRoute

@Composable
fun InterstitialScreen(
    viewModel: InterstitialViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.state.collectAsState()

    PresentationStateContainer(
        uiState,
        Modifier.fillMaxSize()
    ) { interstitialState ->
        AppTopBarContainer(
            title = "Interstitial Screen",
            onNavigateBack = { navController.navigateUp() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = { viewModel.onPresent() }
                ) {
                    Text("Present")
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Interstitial(interstitialState)
        }
    }
}

class InterstitialViewModel(
    private val adService: AdService = ServiceLocator.adService
) : ViewModel() {
    private var interstitialState: AdInterstitialState? = null
    private val _state = MutableStateFlow<PresentationState<AdInterstitialState>>(
        PresentationState.Loading
    )

    private val adEventListener: AdEventListener = object : AdEventListener {
        override fun unloadRequest() { interstitialState?.hide() }
    }

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val request = AdRequest(contentUnit = "5192923")

            adService.makeAdvertisement(
                adRequest = request,
                placementType = AdPlacementType.INTERSTITIAL,
                adEventListener = adEventListener
            ).get(
                onSuccess = { ad ->
                    val adState = AdInterstitialState(ad, this)
                    interstitialState = adState
                    _state.value = PresentationState.Loaded(adState)
                },
                onError = { error ->
                    interstitialState = null
                    _state.value = PresentationState.Error(error.description)
                }
            )
        }
    }

    fun onPresent() { interstitialState?.presentIfLoaded() }

    override fun onCleared() {
        super.onCleared()
        interstitialState?.advertisement?.dispose()
    }
}
