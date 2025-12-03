package com.adition.tutorial_app.presentation.screens.inline_screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.entities.request.AdRequest
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.presentation.screens.inline_screen.components.AdItem
import com.adition.tutorial_app.presentation.screens.inline_screen.components.AdItemState
import com.adition.tutorial_app.ui.components.AppTopBarContainer
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.Serializable

@Serializable
data object InlineRoute

@Composable
fun InlineScreen(
    viewModel: InlineViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.state.collectAsState()

    PresentationStateContainer(
        uiState,
        Modifier.fillMaxSize()
    ) { dataSource ->
        AppTopBarContainer(
            title = "Inline Screen",
            onNavigateBack = { navController.navigateUp() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    dataSource,
                    key = { it.id }
                ) { itemState ->
                    AdItem(itemState, navController)
                }
            }
        }
    }
}

class InlineViewModel(
    private val adService: AdService = ServiceLocator.adService
) : ViewModel() {
    private val dataSource = mutableListOf<AdItemState>()
    private val _state = MutableStateFlow<PresentationState<List<AdItemState>>>(
        PresentationState.Loading
    )

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cellStates = getDataSource()
            dataSource.addAll(cellStates)
            _state.value = PresentationState.Loaded(cellStates)
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataSource.onCleared()
    }

    private suspend fun getDataSource(): List<AdItemState> = supervisorScope {
        val requests = MutableList(5) {
            AdRequest(
                contentUnit = "4810915",
                profiles = hashMapOf(), // Can be skipped
                keywords = listOf(), // Can be skipped
                window = null, // Can be skipped
                timeoutAfterSeconds = 10u, // Can be skipped
                gdprPd = null, // Can be skipped
                campaignId = null, // Can be skipped
                bannerId = null, // Can be skipped
                isSHBEnabled = null, // Can be skipped
                dsa = null // Can be skipped
            )
        }

        requests.add(0, AdRequest(contentUnit = "5227780"))

        requests
            .mapIndexed { index, request ->
                async {
                    val itemState = AdItemState(
                        index,
                        adService,
                        request,
                        viewModelScope
                    )

                    itemState.loadAdvertisement()
                    itemState
                }
            }
            .awaitAll()
    }
}

private fun List<AdItemState>.onCleared() = forEach { it.onCleared() }
