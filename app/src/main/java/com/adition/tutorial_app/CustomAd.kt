package com.adition.tutorial_app

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.request.AdRequest
import com.adition.sdk_presentation_compose.api.Ad
import kotlinx.coroutines.launch

@Composable
fun CustomAd() {
    val viewModel: CustomAdViewModel = viewModel()
    viewModel.advertisementState.value?.let {
        when (it) {
            is ResultState.Error -> {
                Text(it.exception.description)
            }

            is ResultState.Success -> {
                it.data.adMetadata
                Ad(
                    it.data
                )
            }
        }
    }
}

class CustomAdViewModel : ViewModel() {
    private val adRequest = AdRequest("5227780")
    var advertisementState = mutableStateOf<ResultState<Advertisement>?>(null)

    init {
        viewModelScope.launch {
            AdService.makeAdvertisement(
                adRequest,
            ).get(
                onSuccess = {
                    advertisementState.value = ResultState.Success(it)
                },
                onError = {
                    Log.e("CustomAdViewModel", "Failed makeAdvertisement: ${it.description}")
                    advertisementState.value = ResultState.Error(it)
                }
            )
        }
    }
}