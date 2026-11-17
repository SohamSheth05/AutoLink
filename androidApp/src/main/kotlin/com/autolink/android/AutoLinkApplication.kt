package com.autolink.android

import android.app.Application
import com.autolink.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AutoLinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@AutoLinkApplication)
        }
    }
}
