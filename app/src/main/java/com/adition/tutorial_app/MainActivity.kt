package com.adition.tutorial_app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.AdService.tagUser
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.exception.AdError
import com.adition.sdk_core.api.entities.request.AdRequest
import com.adition.sdk_core.api.entities.request.TagRequest
import com.adition.sdk_core.api.entities.request.TrackingRequest
import com.adition.sdk_core.api.entities.response.AdMetadata
import com.adition.sdk_core.api.services.event_listener.AdEventListener
import com.adition.sdk_core.api.services.event_listener.AdEventType
import com.adition.sdk_presentation_compose.api.Ad
import com.adition.tutorial_app.ui.theme.TutorialAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") { MainScreen(navController) }
        composable("interstitial") { InterstitialScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("interstitial") },
                content = { Text("Go to Interstitial") },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            InlineAd()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InlineAd() {
    val adViewModel: AdViewModel = viewModel()
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

    val adEventListener: AdEventListener = object : AdEventListener {
        override fun eventProcessed(adEventType: AdEventType, adMetadata: AdMetadata) {
            Log.d("AdViewModel events", "Collected EVENT - $adEventType")
            when (adEventType) {
                is AdEventType.Impression -> {}
                is AdEventType.RendererMessageReceived -> {}
                is AdEventType.Tap -> {}
                is AdEventType.UnloadRequest -> {}
                is AdEventType.Viewable -> {
                    when (adEventType.percentage) {
                        AdEventType.VisibilityPercentage.ONE -> {
                            Log.d("AdViewModel events", "Viewable - 1%")
                        }
                        AdEventType.VisibilityPercentage.FIFTY -> {
                            Log.d("AdViewModel events", "Viewable - 50%")
                        }
                        AdEventType.VisibilityPercentage.ONE_HUNDRED -> {
                            Log.d("AdViewModel events", "Viewable - 100%")
                        }
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            val tagUser = async { tagUser() }
            val conversionTracking = async { conversionTracking() }

            tagUser.await()
            conversionTracking.await()

            AdService.makeAdvertisement(
                adRequest,
                adEventListener = adEventListener
            ).get(
                onSuccess = {
                    aspectRatio = it.adMetadata?.aspectRatio ?: aspectRatio
                    advertisementState.value = ResultState.Success(it)
                },
                onError = {
                    Log.e("AdViewModel", "Failed makeAdvertisement: ${it.description}")
                    when(it) {
                        is AdError.CacheOverflow -> {
                            flushCache()
                        }
                        else -> {}
                    }
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }

    private fun flushCache() {
        val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        coroutineScope.launch {
            AdService.flushCache().get(
                onSuccess = {
                    Log.d("AdViewModel", "flushCache successfully")
                },
                onError = {
                    Log.d("AdViewModel", "Failed flushCache: ${it.description}")
                }
            )
        }
    }

    private suspend fun tagUser() {
        val tags = listOf(TagRequest.Tag("segments", "category", "home"))

        AdService.tagUser(TagRequest(tags)).get(
            onSuccess = {
                Log.d("AdViewModel", "User tagging was successfully")
            },
            onError = {
                Log.d("AdViewModel", "Failed user tagging: ${it.description}")
            }
        )
    }

    private suspend fun conversionTracking() {
        val request = TrackingRequest(
            landingPageId = 1,
            trackingSpotId = 1,
            orderId = "orderId",
            itemNumber = "itemNumber",
            description = "description",
            quantity = 1,
            price = 19.99f,
            total = 39.98f
        )

        AdService.trackingRequest(request).get(
            onSuccess = {
                Log.d("AdViewModel", "Conversion tracking was successfully")
            },
            onError = {
                Log.d("AdViewModel", "Failed conversion tracking: ${it.description}")
            }
        )
    }
}