package com.example.firstproject

import android.Manifest
import android.app.Application
import com.example.firstproject.client.WebRtcClientConnection
import org.mediasoup.droid.MediasoupClient

class MyApplication : Application() {

    companion object {
        // 모든 퍼미션 관련 배열
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )

        lateinit var webRtcClientConnection: WebRtcClientConnection
    }

    override fun onCreate() {
        super.onCreate()

        webRtcClientConnection = WebRtcClientConnection()
        MediasoupClient.initialize(applicationContext)
    }
}