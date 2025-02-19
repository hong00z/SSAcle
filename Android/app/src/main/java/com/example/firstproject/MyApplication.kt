package com.example.firstproject

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.firstproject.client.WebRtcClientConnection
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.data.repository.TokenManager
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.mediasoup.droid.MediasoupClient

class MyApplication : Application() {

    companion object {
        private val Context.dataStore by preferencesDataStore("user_prefs")

        // 모든 퍼미션 관련 배열
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )

        lateinit var webRtcClientConnection: WebRtcClientConnection

        var accessToken: String? = null
        var fcmToken: String? = null
        lateinit var USER_ID: String
        lateinit var NICKNAME: String
        lateinit var IMAGE_URL: String
        lateinit var EMAIL: String

        lateinit var instance: MyApplication
            private set

        val appContext: Context
            get() = instance.applicationContext

        lateinit var tokenManager: TokenManager

        // ✅ DataStore Keys
        private val KEY_GRADE = stringPreferencesKey("user_grade")
        private val KEY_NAME = stringPreferencesKey("user_name")
        private val KEY_TERM = stringPreferencesKey("user_term")
        private val KEY_CAMPUS = stringPreferencesKey("user_campus")

        // ✅ 사용자 정보 저장 (비동기)
        fun saveUserInfo(
            grade: String,
            name: String,
            term: String,
            campus: String
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                appContext.dataStore.edit { prefs ->
                    prefs[KEY_GRADE] = grade
                    prefs[KEY_NAME] = name
                    prefs[KEY_TERM] = term
                    prefs[KEY_CAMPUS] = campus
                }
            }
        }

        fun getUserGrade(): Flow<String?> {
            return appContext.dataStore.data.map { prefs -> prefs[KEY_GRADE] }
        }

        fun getUserName(): Flow<String?> {
            return appContext.dataStore.data.map { prefs -> prefs[KEY_NAME] }
        }

        fun getUserTerm(): Flow<String?> {
            return appContext.dataStore.data.map { prefs -> prefs[KEY_TERM] }
        }

        fun getUserCampus(): Flow<String?> {
            return appContext.dataStore.data.map { prefs -> prefs[KEY_CAMPUS] }
        }

        private val KEY_AUTH_COMPLETED = booleanPreferencesKey("auth_completed")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // 사용자 인증 완료 여부
        fun setAuthCompleted(value: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                appContext.dataStore.edit { prefs ->
                    prefs[KEY_AUTH_COMPLETED] = value
                }
            }
        }

        fun isAuthCompleted(): Flow<Boolean> {
            return appContext.dataStore.data.map { prefs ->
                prefs[KEY_AUTH_COMPLETED] ?: false
            }
        }

        // 온보딩 완료 여부
        fun setOnboardingCompleted(value: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                appContext.dataStore.edit { prefs ->
                    prefs[KEY_ONBOARDING_COMPLETED] = value
                }
            }
        }

        fun isOnboardingCompleted(): Flow<Boolean> {
            return appContext.dataStore.data.map { prefs ->
                prefs[KEY_ONBOARDING_COMPLETED] ?: false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // ✅ 카카오 SDK 초기화 (필수)
        KakaoSdk.init(this, "0618f69e1a386d67ec61fc517f36c35d")

        tokenManager = TokenManager(this)
        accessToken = tokenManager.getAccessToken()

        instance = this
        webRtcClientConnection = WebRtcClientConnection()
        MediasoupClient.initialize(applicationContext)
    }
}