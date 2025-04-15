package com.adition.tutorial_app

import android.util.Log
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.request.AdRequest
import com.adition.sdk_presentation_compose.api.Ad
import kotlinx.coroutines.launch

@Composable
fun InlineAd() {
    val viewModel: InlineAdViewModel = viewModel()
    viewModel.advertisementState.value?.let {
        when(it) {
            is ResultState.Error -> {
                Text(it.exception.description)
            }
            is ResultState.Success -> {
                it.data.adMetadata
                Ad(
                    it.data,
                    modifier = Modifier.aspectRatio(viewModel.aspectRatio)
                )
            }
        }
    }
}

class InlineAdViewModel: ViewModel() {
    private val adRequest = AdRequest("4810915")
    var advertisementState = mutableStateOf<ResultState<Advertisement>?>(null)
    var aspectRatio = 2f

    init {
        viewModelScope.launch {
            AdService.makeAdvertisement(adRequest).get(
                onSuccess = {
                    aspectRatio = it.adMetadata?.aspectRatio ?: aspectRatio
                    advertisementState.value = ResultState.Success(it)
                },
                onError = {
                    Log.e("AdViewModel", "Failed makeAdvertisement: ${it.description}")
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }
}