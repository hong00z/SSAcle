package com.example.firstproject.data.repository

import android.content.Context
import android.util.Log
import com.example.firstproject.BuildConfig
import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.response.AllStudyListResponseDTO
import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.example.firstproject.data.model.dto.response.RefreshTokenDTO
import com.example.firstproject.data.model.dto.response.StudyDTO
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


private val TAG = "리모트데이터소스"

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

    private val tokenInterceptor = TokenInterceptor(context)

    private val client = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor) // 토큰 인터셉터 추가
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
            .baseUrl(BASE_URL_RTC)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val retrofitChat: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CHAT)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun getSpringService(): APIService {
        return retrofitSpring.create(APIService::class.java)
    }

    fun getRTCService(): APIService {
        return retrofitRTC.create(APIService::class.java)
    }

    fun getChatService(): APIService {
        return retrofitChat.create(APIService::class.java)
    }

    private val springService = getSpringService()

    suspend fun loginWithKakao(accessToken: String): RequestResult<KakaoTokenDTO> {
        Log.d(TAG, "서버로 보낼 토큰: Bearer $accessToken")
        return try {
            val response = springService.kakaoLogin("Bearer $accessToken")
            Log.d(TAG, "서버 응답 코드: ${response.code()}") // ✅ HTTP 응답 코드 확인
            Log.d(TAG, "서버 응답 바디: ${response.body()}") // ✅ 응답 바디 로그

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (response.code() == 200 && body.data != null) {
                    Log.d(TAG,"서버 응답 성공: ${body.code} - ${body.message}")
                    RequestResult.Success(body.data)  // ✅ KakaoTokenDTO 반환

                } else {
                    Log.e(TAG, "서버에서 로그인 실패: ${body.code} - ${body.message}")

                    RequestResult.Failure(
                        body.code.toString(),
                        Exception(body.message ?: "로그인 실패")
                    )
                }

            } else {
                Log.e(TAG, "서버 응답 실패: ${response.code()}")

                RequestResult.Failure(response.code().toString(), Exception("서버 응답 실패"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "로그인 요청 중 예외 발생", e)
            RequestResult.Failure("EXCEPTION", e)
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): RequestResult<RefreshTokenDTO> {
        return try {
            val response = springService.getRefreshToken("Bearer $refreshToken")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()
                if (body != null && body.code == 200 && body.data != null) {
                    RequestResult.Success(body.data)  // ✅ RefreshTokenDTO 반환
                } else {
                    RequestResult.Failure(
                        body?.code.toString(),
                        Exception(body?.message ?: "토큰 갱신 실패")
                    )
                }
            } else {
                RequestResult.Failure(response.code().toString(), Exception("서버 응답 실패"))
            }

        } catch (e: Exception) {
            RequestResult.Failure("EXCEPTION", e)
        }
    }

    // 이건 아님
//    suspend fun getAllStudyList(accessToken: String): RequestResult<AllStudyListResponseDTO> {
//        return try {
//            val response = springService.getAllStudies("Bearer $accessToken")
//
//            if (response.isSuccessful && response.body() != null) {
//                val body = response.body()!!
//
//                if (response.code() == 200 && body.data != null) {
//                    RequestResult.Success(body.data)
//                } else {
//                    RequestResult.Failure(
//                        body.code.toString(),
//                        Exception(body.message ?: "로그인 실패")
//                    )
//                }
//
//            } else {
//                RequestResult.Failure(response.code().toString(), Exception("서버 응답 실패"))
//            }
//        } catch (e: Exception) {
//            RequestResult.Failure("EXCEPTION", e)
//        }
//    }

    // 스터디 관련 통신
    suspend fun getAllStudy(accessToken: String) : RequestResult<List<StudyDTO>> {
        return try {
            val response = springService.getAllStudies("Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                RequestResult.Success(body)
            } else {
                RequestResult.Failure(response.code().toString(), Exception("통신 실패"))
            }

        } catch (e: Exception) {
            RequestResult.Failure("EXCEPTION", e)
        }
    }
}