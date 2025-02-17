package com.example.firstproject.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.response.StudyDetailInfoResponseDTO
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyDetailViewModel : ViewModel() {
    private val repository = MainRepository

    private val _studyDetailResult =
        MutableStateFlow<RequestResult<StudyDetailInfoResponseDTO>>(RequestResult.None())
    val studyDetailResult = _studyDetailResult.asStateFlow()

    fun getStudyDetailInfo(studyId: String) {
        viewModelScope.launch {
            _studyDetailResult.update {
                RequestResult.Progress()
            }

            val result = repository.getStudyDetailInfo(
                accessToken = tokenManager.getAccessToken()!!,
                studyId = studyId
            )

            _studyDetailResult.update {
                result
            }

            delay(200)
        }
    }
}