package com.adition.tutorial_app

import android.util.Log
import androidx.compose.runtime.MutableState
import com.adition.sdk_core.api.services.event_handler.TargetURLHandler

class CustomTargetURLHandler(private val openBrowser: MutableState<String?>) : TargetURLHandler {
    override fun isValidURL(url: String): Boolean {
        Log.d("CustomTargetURLHandler", "CustomTargetURLHandler isValidURL: $url")
        return true
    }


    override fun handleURL(url: String) {
        Log.d("CustomTargetURLHandler", "CustomTargetURLHandler URL: $url")
        openBrowser.value = url
    }
}