package com.adition.tutorial_app.presentation.screens.inline_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.core.Advertisement
import com.adition.ad_sdk.api.entities.exception.AdResult
import com.adition.ad_sdk.api.entities.request.AdPlacementType
import com.adition.ad_sdk.api.entities.request.AdRequest
import com.adition.ad_sdk.api.presentation.Ad
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun AdItem(state: AdItemState) {
    val uiState by state.state.collectAsState()

    PresentationStateContainer(
        uiState,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.0f)
    ) { data ->
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(data.aspectRatio ?: 2.0f)
        ) {
            Ad(
                advertisement = data.advertisement,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(data.aspectRatio ?: 2.0f)
            )
        }
    }
}

class AdItemState(
    val id: Int,
    private val adService: AdService,
    private val request: AdRequest
) {
    private val _state = MutableStateFlow<PresentationState<ItemData>>(PresentationState.Loading)
    private var advertisement: Advertisement? = null

    val state = _state.asStateFlow()

    suspend fun loadAdvertisement() {
        _state.value = PresentationState.Loading

        adService
            .makeAdvertisement(
                adRequest = request,
                placementType  = AdPlacementType.INLINE, // Inline by default
                targetURLHandler = null, // Can be skipped
                adEventListener = null // Can be skipped
            )
            .map { ItemData(it, it.getMetadata()?.aspectRatio) }
            .get(
                onSuccess = { data ->
                    advertisement = data.advertisement
                    _state.value = PresentationState.Loaded(data)
                },
                onError = { error ->
                    _state.value = PresentationState.Error(error.description)
                }
            )
    }

    fun onCleared() {
        advertisement?.dispose()
        advertisement = null
    }

    data class ItemData(
        val advertisement: Advertisement,
        val aspectRatio: Float?
    )
}

private suspend fun <T, ActionResult> AdResult<T>.map(
    action: suspend (T) -> ActionResult
): AdResult<ActionResult> {
    return when (this) {
        is AdResult.Success -> AdResult.Success(action(this.result))
        is AdResult.Error -> AdResult.Error(this.error)
    }
}
