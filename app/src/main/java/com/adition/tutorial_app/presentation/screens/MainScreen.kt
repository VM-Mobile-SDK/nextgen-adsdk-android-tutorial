package com.adition.tutorial_app.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adition.ad_sdk.api.core.AdServiceProviderInterface
import com.adition.ad_sdk.api.entities.exception.AdError
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()

    PresentationStateContainer(
        uiState,
        Modifier.fillMaxSize()
    ) {
        Text("Ready")
    }
}

class MainViewModel(
    val adServiceProvider: AdServiceProviderInterface = ServiceLocator.adServiceProvider
) : ViewModel() {
    private val _state = MutableStateFlow<PresentationState<Unit>>(
        PresentationState.Loading
    )

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            adServiceProvider.configure(
                "1800",
                parentCoroutineScope = this
            ).get(
                onSuccess = { _state.value = PresentationState.Loaded(Unit) },
                onError = { _state.value = PresentationState.Error(it.description) }
            )
        }
    }
}
