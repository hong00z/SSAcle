package com.example.firstproject

import android.app.Application
import android.content.Context
import com.example.firstproject.data.repository.TokenManager
import com.kakao.sdk.common.KakaoSdk
import timber.log.Timber

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // 다른 초기화 코드들
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // ✅ 카카오 SDK 초기화 (필수)
        KakaoSdk.init(this, "0618f69e1a386d67ec61fc517f36c35d")


        val tokenManager = TokenManager(this)
        accessToken = tokenManager.getAccessToken()

        instance = this
    }

    companion object {
        var accessToken: String? = null

        lateinit var instance: MyApplication
            private set

        val appContext: Context
            get() = instance.applicationContext
    }

}