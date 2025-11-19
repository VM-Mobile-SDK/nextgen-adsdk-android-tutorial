package com.adition.tutorial_app.presentation.screens

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
import com.adition.ad_sdk.api.entities.exception.AdResult
import com.adition.ad_sdk.api.entities.request.global_parameters.AccessMode
import com.adition.ad_sdk.api.entities.request.global_parameters.AdRequestGlobalParameter
import com.adition.ad_sdk.api.entities.request.global_parameters.AdRequestGlobalParameters
import com.adition.ad_sdk.api.entities.request.global_parameters.ExternalUID
import com.adition.ad_sdk.api.entities.request.global_parameters.GlobalParameter
import com.adition.ad_sdk.api.entities.request.global_parameters.GlobalParameters
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.presentation.screens.inline_screen.InlineRoute
import com.adition.tutorial_app.ui.components.PresentationStateContainer
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
                    globalParameters = globalParameters,
                    adRequestGlobalParameters = adRequestGlobalParameters
                )
                .flatMap { adServiceProvider.get() }
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
                .get(
                    onSuccess = { _state.value = PresentationState.Loaded(Unit) },
                    onError = { _state.value = PresentationState.Error(it.description) }
                )
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

private suspend fun <T, ActionResult> AdResult<T>.flatMap(
    action: suspend (T) -> AdResult<ActionResult>
): AdResult<ActionResult> {
    return when (this) {
        is AdResult.Success -> action(this.result)
        is AdResult.Error -> AdResult.Error(this.error)
    }
}

private suspend fun <T> AdResult<T>.onSuccess(
    action: suspend (T) -> Unit
) : AdResult<T> {
    return when (this) {
        is AdResult.Success -> {
            action(this.result)
            this
        }
        is AdResult.Error -> this
    }
}
