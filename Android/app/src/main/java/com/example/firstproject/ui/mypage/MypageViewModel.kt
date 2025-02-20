package com.example.firstproject.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.EditProfileRequestDTO
import com.example.firstproject.data.model.dto.response.EditProfileResponseDTO
import com.example.firstproject.data.model.dto.response.Profile
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MypageViewModel : ViewModel() {
    private val repository = MainRepository

    private val _userProfileResult =
        MutableStateFlow<RequestResult<CommonResponseDTO<Profile>>>(RequestResult.None())
    val getProfileResult = _userProfileResult.asStateFlow()

    private val _editProfileResult =
        MutableStateFlow<RequestResult<CommonResponseDTO<EditProfileResponseDTO>>>(RequestResult.None())
    val editProfileResult = _editProfileResult.asStateFlow()

    fun getUserProfile() {
        viewModelScope.launch {
            _userProfileResult.update {
                RequestResult.Progress()
            }

            val result = repository.getUserProfile(tokenManager.getAccessToken()!!)
            _userProfileResult.update {
                result
            }
        }
    }

    fun editUserProfile(request: EditProfileRequestDTO, imageFile: File?) {
        viewModelScope.launch {
            _editProfileResult.update {
                RequestResult.Progress()
            }

            val result =
                repository.editUserProfile(tokenManager.getAccessToken()!!, request, imageFile)
            _editProfileResult.update { result }
        }
    }

}