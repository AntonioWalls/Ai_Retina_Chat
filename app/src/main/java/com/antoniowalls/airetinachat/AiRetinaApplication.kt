package com.antoniowalls.airetinachat

import android.app.Application
import com.antoniowalls.airetinachat.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AiRetinaApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AiRetinaApplication)
            modules(appModule)
        }
    }
}