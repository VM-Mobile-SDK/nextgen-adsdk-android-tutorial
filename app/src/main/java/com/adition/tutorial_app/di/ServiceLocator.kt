package com.adition.tutorial_app.di

import android.content.Context
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.core.AdServiceProvider
import com.adition.ad_sdk.api.core.AdServiceProviderInterface
import com.adition.ad_sdk.api.entities.exception.AdResult

object ServiceLocator {
    lateinit var adServiceProvider: AdServiceProviderInterface
        private set

    val adService: AdService
        get() = when (val serviceResult = adServiceProvider.get()) {
            is AdResult.Success -> serviceResult.result
            is AdResult.Error -> throw Exception(serviceResult.error.description)
        }

    fun init(context: Context) {
        adServiceProvider = AdServiceProvider(context)
    }
}
