package com.adition.tutorial_app.utility

import com.adition.ad_sdk.api.entities.exception.AdError
import com.adition.ad_sdk.api.entities.exception.AdResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

inline fun <reified T> Json.decodeString(json: String) = try {
    AdResult.Success(decodeFromString<T>(json))
} catch (exception: SerializationException) {
    AdResult.Error(AdError.Decoding(exception))
}
