package com.adition.tutorial_app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
fun InterstitialScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        InterstitialAd(Modifier.padding(innerPadding))
    }
}

@Composable
fun InterstitialAd(modifier: Modifier) {
    val interstitialAdViewModel: InterstitialAdViewModel = viewModel()
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        interstitialAdViewModel.advertisementState.value?.let {
            when(it) {
                is ResultState.Error -> {
                    Text(it.exception.description)
                }
                is ResultState.Success -> {
                    Button(
                        onClick = {
                            interstitialAdViewModel.interstitialState?.presentIfLoaded()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(26.dp)
                    ) {
                        Text(
                            text = "Show Interstitial",
                        )
                    }
                    interstitialAdViewModel.interstitialState?.let { it1 -> Interstitial(it1) }
                }
            }
        }
    }
}

class InterstitialAdViewModel : ViewModel() {
    private val adRequest = AdRequest("5192923")
    var advertisementState = mutableStateOf<ResultState<Advertisement>?>(null)
    var interstitialState: AdInterstitialState? = null

    val adEventListener: AdEventListener = object : AdEventListener {
        override fun eventProcessed(eventType: AdEventType, adMetadata: AdMetadata) {
            Log.d("InterstitialAdViewModel events", "Collected EVENT - $eventType")
            if (eventType == AdEventType.UnloadRequest) {
                interstitialState?.hide()
            }
        }
    }

    init {
        viewModelScope.launch {
            AdService.makeAdvertisement(
                adRequest,
                placementType = AdPlacementType.INTERSTITIAL,
                adEventListener= adEventListener
            ).get(
                onSuccess = {
                    advertisementState.value = ResultState.Success(it)
                    interstitialState = AdInterstitialState(it, this)
                },
                onError = {
                    Log.e("InterstitialAdViewModel", "Failed makeAdvertisement: ${it.description}")
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }
}


