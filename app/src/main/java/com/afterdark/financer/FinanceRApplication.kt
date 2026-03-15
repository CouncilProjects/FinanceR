package com.afterdark.financer

import android.app.Application
import com.afterdark.financer.data.AppContainer

class FinanceRApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(context = this)
    }
}