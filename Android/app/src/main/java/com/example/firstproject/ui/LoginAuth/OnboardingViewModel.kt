package com.example.firstproject.ui.LoginAuth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.request.NicknameRequestDTO
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.data.repository.TokenManager
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val repository = MainRepository
    private val tokenManager = TokenManager(MyApplication.appContext)
    var accessToken = tokenManager.getAccessToken()

    // 닉네임 중복확인
    private val _checkUserNickname =
        MutableStateFlow<RequestResult<CommonResponseDTO<Boolean>>>(RequestResult.None())
    val checkUserNickname = _checkUserNickname.asStateFlow()

    fun checkNickname(nickname: NicknameRequestDTO) {
        viewModelScope.launch {
            _checkUserNickname.update {
                RequestResult.Progress()
            }

            val result = repository.getCheckNickName(accessToken!!, nickname)
            Log.d("Onboarding 뷰모델", "${result}")
            _checkUserNickname.update {
                result
            }

            delay(200)
        }

    }


}