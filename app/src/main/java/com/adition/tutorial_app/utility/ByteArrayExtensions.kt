package com.adition.tutorial_app.utility

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.adition.ad_sdk.api.entities.exception.AdError
import com.adition.ad_sdk.api.entities.exception.AdResult
import org.json.JSONException

fun ByteArray.toImageBitmap(url: String): AdResult<ImageBitmap> {
    val image = runCatching {
        BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap()
    }.getOrNull()

    return if (image != null) {
        AdResult.Success(image)
    } else {
        val exception = JSONException("Image bitmap decoding failed for URL: $url")
        AdResult.Error(AdError.Decoding(exception))
    }
}
