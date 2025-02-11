package com.example.firstproject.ui.home

import androidx.lifecycle.ViewModel
import com.example.firstproject.data.model.dto.response.JoiningStudyListResponseDTO
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _userInfoStateResult =
        MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val userInfoStateResult: StateFlow<RequestResult<Unit>>
        get() = _userInfoStateResult

    private val _myStudyListResult =
        MutableStateFlow<RequestResult<JoiningStudyListResponseDTO>>(RequestResult.None())
    val myStudyListResult: StateFlow<RequestResult<JoiningStudyListResponseDTO>>
        get() = _myStudyListResult




}