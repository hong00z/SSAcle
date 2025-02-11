//package com.example.firstproject.data.repository
//
//import android.content.Context
//import com.example.firstproject.MyApplication
//import kotlinx.coroutines.runBlocking
//import okhttp3.Interceptor
//import okhttp3.Response
//
//class TokenInterceptor(private val context: Context): Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val tokenManager = TokenManager(context)
//        val requestBuilder = chain.request().newBuilder()
//
//        // Access Token 가져오기
//        var accessToken = MyApplication.accessToken
//
//        if (!accessToken.isNullOrEmpty()) {
//            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
//        }
//
//        val response = chain.proceed(requestBuilder.build())
//
//        // ✅ Access Token 만료 시 Refresh Token 사용해 갱신
//        if (response.code == 401 && response.message == "토큰이 만료되었습니다.") { // 401 Unauthorized → Access Token 만료
//
//            runBlocking {
//                val newAccessToken = refreshToken(context)
//                if (newAccessToken != null) {
//                    MyApplication.accessToken = newAccessToken
//                    tokenManager.saveAccessToken(newAccessToken)
//
//                    // ✅ 새로운 Access Token으로 요청 재시도
//                    val newRequest = chain.request().newBuilder()
//                        .addHeader("Authorization", "Bearer $newAccessToken")
//                        .build()
//                    return@runBlocking chain.proceed(newRequest)
//                } else {
//
//                }
//            }
//        }
//
//        return response
//    }
//
//    // ✅ Refresh Token으로 Access Token 갱신
//    private suspend fun refreshToken(context: Context): String? {
//        val tokenManager = TokenManager(context)
//        val refreshToken = tokenManager.getRefreshToken()
//
//        return if (!refreshToken.isNullOrEmpty()) {
//            try {
//                // ✅ API 호출하여 새로운 Access Token 요청
//                val response = RemoteDataSource(context).getSpringService()
//                    .getRefreshToken("Bearer $refreshToken")
//
//                if (response.isSuccessful && response.body()?.code == 200) {
//                    val newAccessToken = response.body()?.data?.accessToken
//
//                    if (!newAccessToken.isNullOrEmpty()) {
//                        // ✅ 새로운 Access Token 저장
//                        tokenManager.saveAccessToken(newAccessToken)
//                        MyApplication.accessToken = newAccessToken
//                        return newAccessToken
//                    }
//                } else {
//                    // Refresh Token 만료 또는 실패 시 로그아웃 처리
//
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            null
//        } else {
//            null
//        }
//    }
//}