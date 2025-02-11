package com.example.firstproject.data.repository

import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.rootachieve.requestresult.RequestResult

object MainRepository {
    private val remoteDataSource = RemoteDataSource()
    private val tokenManager = TokenManager(MyApplication.appContext)

    // 카카오 로그인
    suspend fun loginWithKakao(accessToken: String): RequestResult<Unit> {
        return when (val result = remoteDataSource.loginWithKakao(accessToken)) {
            is RequestResult.Success -> {
                tokenManager.saveAccessToken(result.data.accessToken)
                tokenManager.saveRefreshToken(result.data.refreshToken)
                RequestResult.Success(Unit) // 성공 결과 반환
            }
            is RequestResult.Failure -> RequestResult.Failure(result.exception.toString())
            is RequestResult.None -> TODO()
            is RequestResult.Progress -> TODO()
        }
    }

    // 리프레쉬 토큰
    suspend fun refreshAccessToken(): RequestResult<Unit> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return RequestResult.Failure("리프레시 토큰 없음")

        return when (val result = remoteDataSource.refreshAccessToken(refreshToken)) {
            is RequestResult.Success -> {
                tokenManager.saveAccessToken(result.data.accessToken)
                RequestResult.Success(Unit)
            }
            is RequestResult.Failure -> RequestResult.Failure(result.exception.toString())
            is RequestResult.Progress -> TODO()
            is RequestResult.None -> TODO()
        }
    }

    suspend fun kakaoLogin(accessToken: String): RequestResult<KakaoTokenDTO> {
        return remoteDataSource.loginWithKakao(accessToken)
    }

}
