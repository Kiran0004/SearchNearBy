package com.fueled.search.nearbyfood

import android.app.Application
import com.fueled.search.nearbyfood.module.appModule
import com.fueled.search.nearbyfood.module.repositoryModule
import com.fueled.search.nearbyfood.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Kiran.
 */


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule, repositoryModule, viewModelModule)
        }
    }
}