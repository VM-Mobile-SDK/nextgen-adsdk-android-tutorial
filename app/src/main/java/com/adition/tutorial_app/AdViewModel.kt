package com.adition.tutorial_app

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.core.AdService.init
import com.adition.sdk_core.api.core.Advertisement
import com.adition.sdk_core.api.entities.request.AdRequest
import kotlinx.coroutines.launch

class AdViewModel: ViewModel() {
    var advertisement = mutableStateOf<Advertisement?>(null)

    init {
        val adRequest = AdRequest(contentId = "4800850")
        viewModelScope.launch {
            AdService.makeAdvertisement(adRequest).get(
                onSuccess = {
                    advertisement.value = it
                },
                onError = {
                    Log.d("AdViewModel", "Failed makeAdvertisement: ${it.description}")
                }
            )
        }
    }
}