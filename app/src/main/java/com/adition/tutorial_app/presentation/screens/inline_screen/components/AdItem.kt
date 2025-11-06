package com.adition.tutorial_app.presentation.screens.inline_screen.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.core.Advertisement
import com.adition.ad_sdk.api.entities.exception.AdError
import com.adition.ad_sdk.api.entities.exception.AdResult
import com.adition.ad_sdk.api.entities.request.AdPlacementType
import com.adition.ad_sdk.api.entities.request.AdRequest
import com.adition.ad_sdk.api.entities.request.TagRequest
import com.adition.ad_sdk.api.entities.response.AdMetadata
import com.adition.ad_sdk.api.presentation.Ad
import com.adition.ad_sdk.api.services.event_listener.AdEventListener
import com.adition.ad_sdk.api.services.event_listener.AdTapEvent
import com.adition.ad_sdk.api.services.event_listener.AdTrackingEvent
import com.adition.tutorial_app.presentation.entities.PresentationState
import com.adition.tutorial_app.presentation.screens.BasketRoute
import com.adition.tutorial_app.ui.components.LabeledContent
import com.adition.tutorial_app.ui.components.PresentationStateContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AdItem(state: AdItemState, navController: NavController) {
    val uiState by state.state.collectAsState()

    PresentationStateContainer(
        uiState,
        modifier = Modifier
            .fillMaxWidth()
    ) { data ->
        Column {
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

            LabeledContent(
                "Price: €${state.price}",
                Modifier.padding(15.dp)
            ) {
                Button(onClick = {
                    state.onBasket()
                    navController.navigate(
                        BasketRoute(state.id, state.price)
                    )
                }) {
                    Text("Add to basket")
                }
            }
        }
    }
}

class AdItemState(
    val id: Int,
    private val adService: AdService,
    private val request: AdRequest,
    private val parentCoroutineScope: CoroutineScope
) {
    private val _state = MutableStateFlow<PresentationState<ItemData>>(PresentationState.Loading)
    private var advertisement: Advertisement? = null

    val state = _state.asStateFlow()
    val price: Int = Random.nextInt(10, 200)

    fun onBasket() {
        val request = TagRequest(
            tags = listOf(
                TagRequest.Tag(
                    key = "MyTutorialStore",
                    subKey = "Movies",
                    value = "$id"
                )
            ),
            timeout = null // Can be skipped
        )

        parentCoroutineScope.launch {
            adService.tagUser(request)
                .get(
                    onSuccess = {
                        Log.d("AdItemState", "Successfully tagged user for item $id")
                    },
                    onError = {
                        Log.d("AdItemState", "Failed to tag user for item $id: ${it.description}")
                    }
                )
        }
    }

    suspend fun loadAdvertisement() {
        _state.value = PresentationState.Loading

        adService
            .makeAdvertisement(
                adRequest = request,
                placementType  = AdPlacementType.INLINE, // Inline by default
                targetURLHandler = null, // Can be skipped
                adEventListener = eventListener
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

    private val eventListener = object : AdEventListener {
        override fun unloadRequest() {
            _state.value = PresentationState.Error("Unloaded by event listener")
            advertisement?.dispose()
            advertisement = null
        }

        override fun trackingEventProcessed(
            event: AdTrackingEvent,
            processedURLs: List<String>,
            metadata: AdMetadata
        ) {
            when (event) {
                is AdTrackingEvent.Impression -> {
                    Log.d("AdItemState", "My ad $id is ready")
                }
                is AdTrackingEvent.Viewable -> {
                    Log.d(
                        "AdItemState",
                        "${event.percentage.value}% of my ad $id is now visible on the screen."
                    )
                }
            }

            Log.d(
                "AdItemState",
                "SDK notified server about that via URLs: $processedURLs"
            )
        }

        override suspend fun trackingEventProcessingFailed(
            event: AdTrackingEvent,
            processedURLs: List<String>,
            failedURLs: Map<String, AdError>
        ): AdEventListener.FailureAction {
            when (event) {
                is AdTrackingEvent.Impression -> {
                    Log.d("AdItemState", "My ad $id is ready")
                }
                is AdTrackingEvent.Viewable -> {
                    Log.d(
                        "AdItemState",
                        "${event.percentage.value}% of my ad $id is now visible on the screen."
                    )
                }
            }

            Log.d(
                "AdItemState",
                """
                    SDK notified server about that via URLs: $processedURLs",
                    but failed during requesting those: $failedURLs
                """.trimIndent()
            )

            return AdEventListener.FailureAction.IGNORE
        }

        override fun tapEventProcessed(
            event: AdTapEvent,
            processedURL: String,
            metadata: AdMetadata
        ) {
            when (event) {
                is AdTapEvent.Tap, is AdTapEvent.TapURL -> {
                    Log.d(
                        "AdItemState",
                        """
                            User tapped on my ad $id.
                            $processedURL opened for the user.
                        """.trimIndent()
                    )
                }
                is AdTapEvent.TapAsset -> {
                    Log.d(
                        "AdItemState",
                        """
                            User tapped on asset ${event.id} of my ad $id.
                            $processedURL opened for the user.
                        """.trimIndent()
                    )
                }
                is AdTapEvent.SilentTap -> {
                    Log.d(
                        "AdItemState",
                        """
                            The renderer of ad $id want to process click counter redirect.
                            URL for redirect: ${event.url}.
                            As a result of redirects we get $processedURL.
                            This URL is NOT opened for the user.
                        """.trimIndent()
                    )
                }
            }
        }

        override suspend fun tapEventProcessingFailed(
            event: AdTapEvent,
            error: AdError
        ): AdEventListener.FailureAction {
            when (event) {
                is AdTapEvent.Tap, is AdTapEvent.TapURL -> {
                    Log.d("AdItemState", "User tapped on my ad $id.")
                }
                is AdTapEvent.TapAsset -> {
                    Log.d("AdItemState", "User tapped on asset ${event.id} of my ad $id")
                }
                is AdTapEvent.SilentTap -> {
                    Log.d(
                        "AdItemState",
                        """
                            The renderer of ad $id want to process click counter redirect.
                            URL for redirect: ${event.url}.
                        """.trimIndent()
                    )
                }
            }

            Log.d("AdItemState", "But processing failed with error: ${error.description}")

            return AdEventListener.FailureAction.IGNORE
        }

        override fun rendererMessageReceived(name: String, message: String?) {
            Log.d(
                "AdItemState",
                """
                    Renderer of my ad $id sent me a message.
                    Name: $name, message: $message"
                    We can create custom logic in the application based on it.
                """.trimIndent()
            )
        }

        override fun customTrackingEventProcessed(name: String, url: String, metadata: AdMetadata) {
            Log.d(
                "AdItemState",
                "Custom tracking event '$name' of my ad $id processed. URL: $url"
            )
        }

        override suspend fun customTrackingEventProcessingFailed(
            name: String,
            url: String,
            error: AdError
        ): AdEventListener.FailureAction {
            Log.d(
                "AdItemState",
                """
                    Custom tracking event '$name' of my ad $id failed during processing.
                    URL: $url, error: ${error.description}
                """.trimIndent()
            )

            return AdEventListener.FailureAction.IGNORE
        }
    }
}

private suspend fun <T, ActionResult> AdResult<T>.map(
    action: suspend (T) -> ActionResult
): AdResult<ActionResult> {
    return when (this) {
        is AdResult.Success -> AdResult.Success(action(this.result))
        is AdResult.Error -> AdResult.Error(this.error)
    }
}
