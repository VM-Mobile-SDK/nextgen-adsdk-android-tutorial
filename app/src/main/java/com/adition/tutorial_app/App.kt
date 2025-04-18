package com.adition.tutorial_app

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.adition.sdk_core.api.core.AdService
import com.adition.sdk_core.api.entities.request.AdRequestGlobalParameters
import com.adition.sdk_core.api.entities.request.GDPR
import com.adition.sdk_core.api.entities.request.TrackingGlobalParameters
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
            val initResult = AdService.configure(
                "1800",
                applicationContext,
                cacheSizeInMb = 20u,
                cachePath = cacheDir.path + "/tutorialApp/"
            )

            initResult.get(
                onSuccess =  {
                    // coroutineScope.launch { AdService.setCacheSize(20u) }
                    // coroutineScope.launch { AdService.setCachePath(cacheDir.path + "/tutorialApp/") }
                    addGlobalParameters()
                    adServiceStatus.postValue(ResultState.Success(Unit))
                },
                onError = {
                    adServiceStatus.postValue(ResultState.Error(it))
                }
            )
        }
    }

    private fun addGlobalParameters() {
        val gdpr = GDPR(consent = "gdprconsentexample", isRulesEnabled = true)

        AdService.setAdRequestGlobalParameter(AdRequestGlobalParameters::gdpr, gdpr)
        // AdService.removeAdRequestGlobalParameter(AdRequestGlobalParameters::gdpr)

        AdService.setTrackingGlobalParameter(TrackingGlobalParameters::gdpr, gdpr)
        // AdService.removeTrackingGlobalParameter(TrackingGlobalParameters::gdpr)
    }
}