package com.example.firstproject.data.repository

import android.content.Context
import com.example.firstproject.BuildConfig
import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.example.firstproject.data.model.dto.response.RefreshTokenDTO
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.network.APIService
import com.google.android.gms.common.api.Response
import com.rootachieve.requestresult.RequestResult
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteDataSource {
    private val context = MyApplication.appContext

    companion object {
        private const val BASE_URL_SPRING = "http://43.203.250.200:5001/"
        private const val BASE_URL_RTC = ""
        private const val BASE_URL_CHAT = ""
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
        redactHeader("Cookie")
    }

//    private val tokenInterceptor = TokenInterceptor(context)

    private val client = OkHttpClient.Builder()
//        .addInterceptor(tokenInterceptor) // 토큰 인터셉터 추가
        .addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
        .connectTimeout(15, TimeUnit.SECONDS) // 연결 타임아웃
        .readTimeout(15, TimeUnit.SECONDS)    // 읽기 타임아웃
        .writeTimeout(15, TimeUnit.SECONDS)  // 쓰기 타임아웃
        .build()

    private val retrofitSpring: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val retrofitRTC: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val retrofitChat: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun getSpringService() : APIService {
        return retrofitSpring.create(APIService::class.java)
    }
    fun getRTCService() : APIService {
        return retrofitRTC.create(APIService::class.java)
    }
    fun getChatService() : APIService {
        return retrofitChat.create(APIService::class.java)
    }

    private val springService = getSpringService()

    suspend fun loginWithKakao(accessToken: String): RequestResult<KakaoTokenDTO> {
        return try {
            val response = springService.kakaoLogin(accessToken)
            if (response.isSuccessful && response.body()?.data != null) {
                RequestResult.Success(response.body()!!.data!!)
            } else {
                RequestResult.Failure(Exception(response.body()?.message ?: "로그인 실패").toString())
            }
        } catch (e: Exception) {
            RequestResult.Failure(e.toString())
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): RequestResult<RefreshTokenDTO> {
        return try {
            val response = springService.getRefreshToken("Bearer $refreshToken")
            if (response.isSuccessful && response.body()?.data != null) {
                RequestResult.Success(response.body()!!.data!!)
            } else {
                RequestResult.Failure(Exception(response.body()?.message ?: "토큰 갱신 실패").toString())
            }
        } catch (e: Exception) {
            RequestResult.Failure(e.toString())
        }
    }
}



