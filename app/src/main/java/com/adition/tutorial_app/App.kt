package com.adition.tutorial_app

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_presentation_compose.api.configure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class App: Application() {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val adServiceStatus = MutableLiveData<ResultState<Unit>>()

    override fun onCreate() {
        super.onCreate()

        coroutineScope.launch {
            val initResult = AdService.configure("1800", applicationContext)

            initResult.get(
                onSuccess =  {
                    adServiceStatus.postValue(ResultState.Success(Unit))
                },
                onError = {
                    adServiceStatus.postValue(ResultState.Error(it))
                }
            )
        }
    }
}