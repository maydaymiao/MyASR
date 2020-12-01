package com.baidu.speech.recognizerdemo.util

import android.app.Application
import android.content.Context

class BaseApplication: Application() {
    companion object{
        lateinit var context: Context
        const val TOKEN_BAIDU = "24.30b9d32ec699da39fd69f0517213fb35.2592000.1608693412.282335-23026141"
        const val BASE_URL_BAIDU = "https://aip.baidubce.com/"
        const val CLIENT_ID = "MyASR"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}