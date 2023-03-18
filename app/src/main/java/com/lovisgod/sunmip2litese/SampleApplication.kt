package com.lovisgod.sunmip2litese

import android.app.Application

class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        SunmiLiteSeApplication.onCreate(this)
    }
}