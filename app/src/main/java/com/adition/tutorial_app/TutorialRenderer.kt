package com.adition.tutorial_app

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.adition.sdk_core.api.core.AdRendererEventHandler
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.entities.exception.AdError
import com.adition.sdk_core.api.entities.exception.AdResult
import com.adition.sdk_core.api.entities.response.AdMetadata
import com.adition.sdk_core.api.entities.response.AdResponseBundle
import com.adition.sdk_core.api.services.event_listener.AdTapEvent
import com.adition.sdk_presentation_compose.api.AdComposeRenderer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject

internal class TutorialRenderer : AdComposeRenderer {
    private lateinit var eventHandler: AdRendererEventHandler
    private var imageBitmap by mutableStateOf<ImageBitmap?>(null)
    private var framingWidth = 1
    private var isBlackFraming = false

    override suspend fun configure(
        rendererEventHandler: AdRendererEventHandler,
        adMetadata: AdMetadata,
        adResponseBundle: AdResponseBundle
    ): AdResult<Unit> {
        this.eventHandler = rendererEventHandler

        val adDataMap = adResponseBundle.adResponse.body?.ext?.adData as? Map<*, *>
            ?: return AdResult.Error(AdError.Decoding(Exception("adData is missing.")))

        val jsonString = JSONObject(adDataMap).toString()
        val adData = Json.parseToJsonElement(jsonString).jsonObject

        val bannerURL = adData["banner_image"]?.jsonPrimitive?.content
            ?: return AdResult.Error(AdError.Decoding(Exception("Banner URL is null.")))

        framingWidth = adData["framing_width"]?.jsonPrimitive?.intOrNull ?: framingWidth
        isBlackFraming = adData["is_black_framing"]?.jsonPrimitive?.booleanOrNull ?: isBlackFraming

        val bannerResult = getBanner(bannerURL)
        return when (bannerResult) {
            is AdResult.Success -> {
                imageBitmap = bannerResult.result
                AdResult.Success(Unit)
            }

            is AdResult.Error -> AdResult.Error(bannerResult.error)
        }
    }

    private suspend fun getBanner(url: String): AdResult<ImageBitmap> {
        val cachedBanner = getCachedBanner(url)

        if (cachedBanner != null) {
            return AdResult.Success(cachedBanner)
        }

        return loadAndCacheBanner(url)
    }

    private suspend fun getCachedBanner(url: String): ImageBitmap? {
        val cache = AdService.getCacheInstance().getOrNull()
        val banner = cache?.find(url)?.getOrNull()

        if (banner != null) {
            return BitmapFactory.decodeByteArray(
                banner.data,
                0,
                banner.data.size
            )?.asImageBitmap()
        }

        return null
    }

    private suspend fun loadAndCacheBanner(url: String): AdResult<ImageBitmap> {
        val bitmapResult = eventHandler.downloadBitmap(url)

        return when (bitmapResult) {
            is AdResult.Error -> AdResult.Error(bitmapResult.error)
            is AdResult.Success -> {
                AdResult.Success(bitmapResult.result.asImageBitmap())
            }
        }
    }

    @Composable
    override fun RenderAd(modifier: Modifier) {
        val imageBitmap = rememberUpdatedState(this.imageBitmap)
        imageBitmap.value?.let {
            val borderColor = if (isBlackFraming) Color.Black else Color.White
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(it.width.toFloat() / it.height)
                    .border(
                        width = framingWidth.dp,
                        color = borderColor
                    )
            ) {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .clickable { eventHandler.performTapEvent(AdTapEvent.Tap) }
                )
            }
        }
    }
}