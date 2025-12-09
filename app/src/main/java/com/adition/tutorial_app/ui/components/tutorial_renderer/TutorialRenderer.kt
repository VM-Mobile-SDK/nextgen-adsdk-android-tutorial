package com.adition.tutorial_app.ui.components.tutorial_renderer

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.adition.ad_sdk.api.core.AdRenderer
import com.adition.ad_sdk.api.core.AdRendererEventHandler
import com.adition.ad_sdk.api.entities.exception.AdResult
import com.adition.ad_sdk.api.entities.response.AdMetadata
import com.adition.ad_sdk.api.services.asset_request_service.AssetRequestService
import com.adition.ad_sdk.api.services.cache.AssetCache
import com.adition.ad_sdk.api.services.cache.AssetPath
import com.adition.ad_sdk.api.services.event_listener.AdTapEvent
import com.adition.tutorial_app.utility.decodeString
import com.adition.tutorial_app.utility.flatMap
import com.adition.tutorial_app.utility.map
import com.adition.tutorial_app.utility.onSuccess
import com.adition.tutorial_app.utility.toImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TutorialRenderer(
    // private val assetRepository: AssetRepository,
    private val eventHandler: AdRendererEventHandler,
    private val cache: AssetCache,
    private val requestService: AssetRequestService,
    private val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : AdRenderer {
    private var rendererData = MutableStateFlow<RendererData?>(null)
    private val jsonFormat = Json { ignoreUnknownKeys = true }

    // Will be called every time an ad is loaded or reloaded.
    override suspend fun configure(
        adResponse: String,
        adMetadata: AdMetadata
    ) = jsonFormat.decodeString<TutorialRendererResponse>(adResponse)
        /*
        .flatMap { response ->
            assetRepository
                .getAsset(response.bannerImage)
                .onSuccess { assetResult ->
                    assetResult.cacheResult.get(
                        onSuccess = {
                            Log.d("TutorialRenderer", "Banner cached: $it")
                        },
                        onError = {
                            Log.e(
                                "TutorialRenderer",
                                "Banner caching failed: ${it.description}"
                            )
                        }
                    )
                }
                .flatMap { it.data.toImageBitmap(response.bannerImage) }
                .map { Pair(response, it) }
        }
         */
        .flatMap { response ->
            getBannerByteArray(response.bannerImage)
                .flatMap { it.toImageBitmap(response.bannerImage) }
                .map { Pair(response, it) }

        }
        .onSuccess {
            adMetadata.rendererMetadata = mutableMapOf(
                "custom_data" to "my custom data that will be available in the app"
            )
        }
        .onSuccess { (response, banner) ->
            rendererData.value = RendererData(
                banner = banner,
                framingWidth = response.framingWidth,
                isBlackFraming = response.isBlackFraming
            )
        }
        .map {}

    private suspend fun getBannerByteArray(url: String): AdResult<ByteArray> {
        val path = AssetPath.fromURL(
            folder = "TutorialRendererResources", // Optional
            url = url
        )

        // cache.remove(path)

        val cacheResult = cache.read(path).getOrNull()

        if (cacheResult != null) {
            val (bytes, uri) = cacheResult
            Log.d("TutorialRenderer", "Banner loaded from cache: $uri")

            return AdResult.Success(bytes)
        }

        Log.d("TutorialRenderer", "Banner not found in cache")

        return requestService.request(url)
            .onSuccess { bytes ->
                cache.write(path, bytes)
                    .get(
                        onSuccess = {
                            Log.d(
                                "TutorialRenderer",
                                "Banner cached after download: $it"
                            )
                        },
                        onError = {
                            Log.e(
                                "TutorialRenderer",
                                "Banner loaded, but caching failed: ${it.description}"
                            )
                        }
                    )
            }
    }

    // Will be called when app want to reload the ad
    override suspend fun prepareForReload(): AdResult<Unit> {
        rendererData.value = null
        return AdResult.Success(Unit)
    }

    // Called before renderer will be removed
    override fun dispose() {
        rendererData.value = null
        coroutineScope.cancel()
    }

    fun onTap() {
        coroutineScope.launch {
            eventHandler.performTap(AdTapEvent.Tap)
            // eventHandler.unloadRequest()
            // eventHandler.sendMessage("Message_to_app", "My message to the app")
        }
    }

    data class RendererData(
        val banner: ImageBitmap,
        val framingWidth: Double,
        val isBlackFraming: Boolean
    )

    @Composable
    override fun RenderAd(modifier: Modifier) {
        val data = rendererData.collectAsState()
        data.value?.let {
            val borderColor = if (it.isBlackFraming) Color.Black else Color.White
            Box(
                modifier = modifier
                    .border(
                        width = it.framingWidth.dp,
                        color = borderColor
                    )
                    .clickable { onTap() }
            ) {
                Image(
                    bitmap = it.banner,
                    contentDescription = null
                )
            }
        }
    }
}
