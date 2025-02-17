package com.example.firstproject.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.response.Profile
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MypageViewModel: ViewModel() {
    private val repository = MainRepository

    private val _userProfileResult =
        MutableStateFlow<RequestResult<CommonResponseDTO<Profile>>>(RequestResult.None())
    val getProfileResult = _userProfileResult.asStateFlow()

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

}