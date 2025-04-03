package com.adition.tutorial_app

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_presentation_compose.api.configure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class App: Application() {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val adServiceStatus = MutableLiveData<Boolean>()
    private val NETWORK_ID = "1800"

    override fun onCreate() {
        super.onCreate()

        coroutineScope.launch {
            val initResult = AdService.configure(NETWORK_ID, applicationContext)

            initResult.get(
                onSuccess =  {
                    adServiceStatus.postValue(true)
                },
                onError = {
                    Log.e("App", it.description)
                }
            )
        }
    }
}