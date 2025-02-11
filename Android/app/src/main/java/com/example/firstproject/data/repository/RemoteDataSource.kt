package com.example.firstproject.data.repository

import com.example.firstproject.BuildConfig
import com.example.firstproject.network.APIService
import com.rootachieve.requestresult.RequestResult
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteDataSource {
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

//    private val authInterceptor = Interceptor { chain ->
//        val token = MainApplication.prefs.token
//        val requestBuilder = chain.request().newBuilder()
//
//        // 토큰이 존재할 경우만 Authorization 헤더 추가
//        token?.let {
//            requestBuilder.addHeader("Authorization", "Bearer $it")
//        }
//
//        chain.proceed(requestBuilder.build())
//    }

    private val client = OkHttpClient.Builder()
//        .addInterceptor(authInterceptor) // 토큰 인터셉터 추가
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

    // JWT 리프레쉬 토큰 확인
//    private suspend inline fun <T> runningWithCheckRefresh(running: () -> T): T {
//        val result = running()
//        return if (result is RequestResult.Failure) {
//            if(getAccessToken()){
//                running()
//            }else{
//                result
//            }
//        } else {
//            result
//        }
//    }
}



