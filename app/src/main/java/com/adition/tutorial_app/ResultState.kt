package com.adition.tutorial_app

import com.adition.sdk_core.api.entities.exception.AdError

sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error<T>(val exception: AdError) : ResultState<T>()
}