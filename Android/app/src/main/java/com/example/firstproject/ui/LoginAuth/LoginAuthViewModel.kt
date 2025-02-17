package com.example.firstproject.ui.LoginAuth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.AuthRequestDTO
import com.example.firstproject.data.model.dto.response.AuthResponseDTO
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.kakao.sdk.user.UserApiClient
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginAuthViewModel : ViewModel() {
    private val repository = MainRepository
//    private val tokenManager = TokenManager(MyApplication.appContext)

    var accessToken = tokenManager.getAccessToken()

    // 카카오 로그인
    private val _loginState = MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val loginState: StateFlow<RequestResult<Unit>> = _loginState

    // 싸피생 인증
    private val _authUserResult =
        MutableStateFlow<RequestResult<CommonResponseDTO<AuthResponseDTO>>>(RequestResult.None())
    val authUserResult: StateFlow<RequestResult<CommonResponseDTO<AuthResponseDTO>>>
        get() = _authUserResult

    fun loginWithKakao(context: Context) {
        viewModelScope.launch {
            _loginState.value = RequestResult.Progress() // 로그인 진행 중

            var accessToken = tokenManager.getAccessToken()
            Log.d("카카오 로그인 시작", "저장된 토큰: $accessToken") // ✅ 기존 저장된 토큰 확인

            if (accessToken.isNullOrEmpty()) {
                // ✅ 저장된 토큰이 없으면 카카오 로그인을 수행하여 새로운 토큰 발급
                accessToken = getKakaoAccessToken(context)
                Log.d("카카오 로그인 시작", "새로운 토큰: $accessToken") // ✅ 새로 받은 토큰 확인
            }


            if (accessToken != null) {
                when (val result = repository.kakaoLogin(accessToken)) {
                    is RequestResult.Success -> {
                        // ✅ 로그인 성공 시 토큰 저장
                        tokenManager.saveAccessToken(result.data.accessToken)
                        tokenManager.saveRefreshToken(result.data.refreshToken)
                        Log.d("카카오 로그인", "로그인 성공: ${result.data.accessToken}")

                        _loginState.value = RequestResult.Success(Unit) // 로그인 성공 상태
                        delay(500)
                        Log.d("카카오 로그인", "로그인 성공: ${_loginState.value}")
                    }

                    is RequestResult.Failure -> {
                        Log.e("카카오 로그인", "로그인 실패: ${result.code} - ${result.exception?.message}")
                        _loginState.value = RequestResult.Failure(result.code, result.exception)
                    }

                    else -> {
                        Log.e("카카오 로그인", "알 수 없는 오류 발생")
                        _loginState.value = RequestResult.Failure("UNKNOWN", Exception("알 수 없는 오류"))
                    }
                }
            } else {
                Log.e("카카오 로그인", "카카오 토큰 가져오기 실패")
                _loginState.value =
                    RequestResult.Failure("TOKEN_ERROR", Exception("카카오 토큰 가져오기 실패"))
            }
        }
    }

    private suspend fun getKakaoAccessToken(context: Context): String? {
        return try {
            suspendCoroutine { continuation ->
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    // ✅ 카카오톡 앱 로그인
                    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                        if (error != null) {
                            Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                            continuation.resume(null)
                        } else {
                            continuation.resume(token?.accessToken)
                        }
                    }
                } else {
                    // ✅ 카카오 계정 로그인 (카카오톡 앱이 없을 때)
                    UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                        if (error != null) {
                            Log.e("KakaoLogin", "카카오 계정 로그인 실패", error)
                            continuation.resume(null)
                        } else {
                            continuation.resume(token?.accessToken)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("KakaoLogin", "로그인 중 오류 발생", e)
            null
        }
    }

    // 인증 요청
    fun sendAuthUser(request: AuthRequestDTO) {
        viewModelScope.launch {
            _authUserResult.update {
                RequestResult.Progress()
            }

            val result = repository.sendAuthUser(accessToken!!, request)
            Log.d("Auth 뷰모델", "${result}")
            _authUserResult.update {
                result
            }

            delay(200)
        }
    }

}