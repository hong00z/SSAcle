package com.example.firstproject

import android.Manifest
import android.app.Application

class MyApplication : Application() {

    companion object {
        // 모든 퍼미션 관련 배열
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }

    override fun onCreate() {
        super.onCreate()

    }
}