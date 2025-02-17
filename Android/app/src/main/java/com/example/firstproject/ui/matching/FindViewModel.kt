package com.example.firstproject.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.response.UserSuitableStudyDtoItem
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FindViewModel:ViewModel() {
    private val repository = MainRepository
    var accessToken = tokenManager.getAccessToken()

    private val _recommandStudyResult =
        MutableStateFlow<RequestResult<List<UserSuitableStudyDtoItem>>>(RequestResult.None())
    val recommandStudyResult = _recommandStudyResult.asStateFlow()


    private val _recommandStudyList =
        MutableStateFlow<List<UserSuitableStudyDtoItem>>(emptyList())
    val recommandStudyList = _recommandStudyList.asStateFlow()


    fun getRecommandStudyList() {
        viewModelScope.launch {
            _recommandStudyResult.update {
                RequestResult.Progress()
            }

            val result = repository.getRecommendedStudies(accessToken!!)

            _recommandStudyResult.update {
                result
            }

            // 추천 스터디 리스트 업데이트
            if (result is RequestResult.Success) {
                _recommandStudyList.value = result.data
            }

            delay(200)
        }

    }

}