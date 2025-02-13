package com.example.firstproject.data.repository

import android.content.Context
import com.example.firstproject.MyApplication
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager(context)
        val requestBuilder = chain.request().newBuilder()

        // Access Token 가져오기
        val accessToken = tokenManager.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        // ✅ Access Token 만료 시 Refresh Token 사용해 갱신
        if (response.code == 401) { // 401 Unauthorized → Access Token 만료
            val newAccessToken = runBlocking {
                when ( val result = MainRepository.refreshAccessToken()) {
                    is RequestResult.Success -> tokenManager.getAccessToken()
                    is RequestResult.Failure -> null
                    else -> null
                }

            }
            if (!newAccessToken.isNullOrEmpty()) {
                // ✅ 새로운 Access Token으로 요청 재시도
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()
                response.close()
                return chain.proceed(newRequest)
            } else {
                // 🚨 Refresh Token도 만료된 경우 → 로그아웃 처리
                tokenManager.clearTokens()
            }
        }

        return response
    }

    // ✅ Refresh Token으로 Access Token 갱신
    private suspend fun refreshToken(context: Context): String? {
        val tokenManager = TokenManager(context)
        val refreshToken = tokenManager.getRefreshToken()

        return if (!refreshToken.isNullOrEmpty()) {
            try {
                // ✅ API 호출하여 새로운 Access Token 요청
                val response = RemoteDataSource().getSpringService()
                    .getRefreshToken("Bearer $refreshToken")

                if (response.isSuccessful && response.body()?.code == 200) {
                    response.body()?.data?.accessToken
                } else null

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null

    }

}