package com.adition.tutorial_app.presentation.entities

sealed class PresentationState<out Data> {
    object Loading : PresentationState<Nothing>()
    data class Error(val description: String) : PresentationState<Nothing>()
    data class Loaded<Data>(val data: Data) : PresentationState<Data>()
}
