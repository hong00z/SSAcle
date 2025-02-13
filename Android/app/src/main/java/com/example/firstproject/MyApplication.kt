package com.example.firstproject

import android.Manifest
import android.app.Application
import android.content.Context
import com.example.firstproject.client.WebRtcClientConnection
import org.mediasoup.droid.MediasoupClient
import com.example.firstproject.data.repository.TokenManager
import com.kakao.sdk.common.KakaoSdk
import timber.log.Timber

class MyApplication : Application() {

    companion object {
        // 모든 퍼미션 관련 배열
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )

        lateinit var webRtcClientConnection: WebRtcClientConnection

        var accessToken: String? = null

        lateinit var instance: MyApplication
            private set

        val appContext: Context
            get() = instance.applicationContext
    }

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
        webRtcClientConnection = WebRtcClientConnection()
        MediasoupClient.initialize(applicationContext)
    }
}