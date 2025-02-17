package com.example.firstproject.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.RegisterStudyRequestDTO
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterStudyViewModel: ViewModel() {
    private val repository = MainRepository

    private val _registerResult =
        MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val registerResult = _registerResult.asStateFlow()

    fun sendRegisterStudy(request: RegisterStudyRequestDTO) {
        viewModelScope.launch {
            _registerResult.update {
                RequestResult.Progress()
            }

            val result = repository.sendRegisterStudy(tokenManager.getAccessToken()!!, request)
            _registerResult.update {
                result
            }

        }
    }

}