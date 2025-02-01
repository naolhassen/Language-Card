package com.naol.languagecard

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    viewModel {
        CardViewModel(get())
    }
}

class LanguageCardApp:  Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LanguageCardApp)
            modules(listOf(appModule))
        }
    }
}