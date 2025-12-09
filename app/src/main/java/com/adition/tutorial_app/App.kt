package com.adition.tutorial_app

import android.app.Application
import com.adition.tutorial_app.di.ServiceLocator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(applicationContext)
    }
}
