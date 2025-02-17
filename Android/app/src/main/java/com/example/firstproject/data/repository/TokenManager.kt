package com.example.firstproject.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.firstproject.MyApplication

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val FCM_TOKEN_KEY = "fcm_token"
    }

    // ✅ Access Token 저장
    fun saveAccessToken(token: String) {
        prefs.edit().putString(ACCESS_TOKEN_KEY, token).apply()
        MyApplication.accessToken = token // 전역 변수 업데이트
    }

    // ✅ Refresh Token 저장
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(REFRESH_TOKEN_KEY, token).apply()
    }

    // ✅ Access Token 조회
    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    // ✅ Refresh Token 조회
    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }

    // ✅ 토큰 삭제 (로그아웃 시 사용)
    fun clearTokens() {
        prefs.edit().remove(ACCESS_TOKEN_KEY).remove(REFRESH_TOKEN_KEY).apply()
        MyApplication.accessToken = null // 전역 변수 초기화
    }

    // FCM Token 저장
    fun saveFcmToken(token: String) {
        prefs.edit().putString(FCM_TOKEN_KEY, token).apply()
        MyApplication.fcmToken = token // 전역 변수 업데이트
    }

    // FCM Token 조회
    fun getFcmToken(): String? {
        return prefs.getString(FCM_TOKEN_KEY, null)
    }

}