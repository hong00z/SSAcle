package com.example.firstproject.ui.LoginAuth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.request.EditProfileRequestDTO
import com.example.firstproject.data.model.dto.request.NicknameRequestDTO
import com.example.firstproject.data.model.dto.response.EditProfileResponseDTO
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.data.repository.TokenManager
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File


class OnboardingViewModel : ViewModel() {
    private val repository = MainRepository
//    private val tokenManager = TokenManager(MyApplication.appContext)
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
            Log.d("Onboarding 뷰모델", "중복확인: ${result}")
            _checkUserNickname.update {
                result
            }

            delay(200)
        }

    }

    private val _onboardingProfile =
        MutableStateFlow<RequestResult<EditProfileResponseDTO>>(RequestResult.None())
    val onboardingProfile = _onboardingProfile.asStateFlow()

    fun onboardingUserProfile(context: Context, request: EditProfileRequestDTO, imageFile: File?) {
        viewModelScope.launch {
            val defaultFile = createFileFromDrawable(context, R.drawable.img_default_profile)

            _onboardingProfile.update {
                RequestResult.Progress()
            }

            val result = repository.sendOnboardingProfile(accessToken!!, request, defaultFile)
            Log.d("Onboarding 뷰모델", "온보딩: ${result}")
            _onboardingProfile.update {
                result
            }

            delay(200)
        }
    }


    private fun createFileFromDrawable(context: Context, resId: Int): File {
        val inputStream = context.resources.openRawResource(resId)
        val tempFile = File(context.cacheDir, "default_image.png")
        tempFile.outputStream().use { out ->
            inputStream.copyTo(out)
        }
        return tempFile
    }

}