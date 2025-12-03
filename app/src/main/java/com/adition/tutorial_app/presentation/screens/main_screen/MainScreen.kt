package com.adition.tutorial_app.presentation.screens.main_screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adition.ad_sdk.api.core.AdServiceProviderInterface
import com.adition.ad_sdk.api.entities.request.global_parameters.AccessMode
import com.adition.ad_sdk.api.entities.request.global_parameters.AdRequestGlobalParameter
import com.adition.ad_sdk.api.entities.request.global_parameters.AdRequestGlobalParameters
import com.adition.ad_sdk.api.entities.request.global_parameters.ExternalUID
import com.adition.ad_sdk.api.entities.request.global_parameters.GlobalParameter
import com.adition.ad_sdk.api.entities.request.global_parameters.GlobalParameters
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.presentation.screens.InterstitialRoute
import com.adition.tutorial_app.presentation.screens.inline_screen.InlineRoute
import com.adition.tutorial_app.presentation.screens.main_screen.components.LocaleChangeEffect
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import com.adition.tutorial_app.ui.components.tutorial_renderer.TutorialRenderer
import com.adition.tutorial_app.utility.flatMap
import com.adition.tutorial_app.utility.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.showDialog.value = false
                viewModel.onLoad(false)
            },
            text = { Text("Please grant the permission to collect the data") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.showDialog.value = false
                    viewModel.onLoad(true)
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.showDialog.value = false
                    viewModel.onLoad(false)
                }) {
                    Text("Deny")
                }
            }
        )
    }

    PresentationStateContainer(
        uiState,
        Modifier.fillMaxSize()
    ) {
        LocaleChangeEffect { viewModel.onLocaleChange() }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate(InlineRoute) }
            ) {
                Text("Inline Ads List")
            }

            Button(
                onClick = { navController.navigate(InterstitialRoute) }
            ) {
                Text("Interstitial Ad")
            }
        }
    }
}

class MainViewModel(
    val adServiceProvider: AdServiceProviderInterface = ServiceLocator.adServiceProvider
) : ViewModel() {
    private val _state = MutableStateFlow<PresentationState<Unit>>(
        PresentationState.Loading
    )

    val state = _state.asStateFlow()
    val showDialog = MutableStateFlow(true)

    fun onLoad(isDataCollectionAllowed: Boolean) {
        val globalParameters = GlobalParameters(
            accessMode = isDataCollectionAllowed.toAccessMode()
        )

        val adRequestGlobalParameters = AdRequestGlobalParameters(
            cookiesAccess = isDataCollectionAllowed.toCookiesAccess()
        )

        viewModelScope.launch {
            adServiceProvider
                .configure(
                    "1800",
                    parentCoroutineScope = this,
                    cacheSize = 20u,
                    globalParameters = globalParameters,
                    adRequestGlobalParameters = adRequestGlobalParameters
                )
                .flatMap { adServiceProvider.get() }
                // .onSuccess {
                //     val possibleError = it.setCacheSize(20u).adErrorOrNull()
                // }
                .onSuccess { adService ->
                    adService.setGlobalParameters(
                        GlobalParameter(
                            GlobalParameters::externalUID,
                            ExternalUID("uid")
                        )
                    )

                    adService.removeGlobalParameter(GlobalParameters::externalUID)
                    adService.setAdRequestGlobalParameters(
                        AdRequestGlobalParameter(
                            AdRequestGlobalParameters::isIpIdentified,
                            true
                        )
                    )

                    adService.removeAdRequestGlobalParameter(
                        AdRequestGlobalParameters::isIpIdentified
                    )
                }
                .onSuccess {
                    it.registerRenderer("tutorialad") { serviceLocator ->
                        TutorialRenderer(
                            cache = serviceLocator.assetCache,
                            requestService = serviceLocator.assetRequestService,
                            eventHandler = serviceLocator.eventHandler
                        )
                    }
                }
                .get(
                    onSuccess = { _state.value = PresentationState.Loaded(Unit) },
                    onError = { _state.value = PresentationState.Error(it.description) }
                )
        }
    }

    fun onLocaleChange() {
        viewModelScope.launch {
            adServiceProvider.get()
                .onSuccess {
                    val possibleError = it.flushCache().adErrorOrNull()

                    if (possibleError != null) {
                        Log.e(
                            "MainViewModel",
                            "Error flushing cache: ${possibleError.description}"
                        )
                    }
                }
        }
    }
}

private fun Boolean.toCookiesAccess(): AdRequestGlobalParameters.CookiesAccess {
    return if (this) {
        AdRequestGlobalParameters.CookiesAccess.GET
    } else {
        AdRequestGlobalParameters.CookiesAccess.NO_COOKIES
    }
}

private fun Boolean.toAccessMode() = if (this) AccessMode.OPT_IN else AccessMode.OPT_OUT
