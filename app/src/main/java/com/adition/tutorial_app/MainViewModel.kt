package com.adition.tutorial_app

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_presentation_compose.api.configure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainViewModel(context: Context) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val adServiceStatus = MutableLiveData<Boolean>()
    private val NETWORK_ID = "1800"

    init {
        coroutineScope.launch {
            val initResult = AdService.configure(NETWORK_ID, context)

            initResult.get(
                onSuccess =  {
                    Log.d("App", "Init is success")
                    adServiceStatus.postValue(true)
                },
                onError = {
                    Log.e("App", it.description)
                }
            )
        }
    }
}