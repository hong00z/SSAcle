package com.example.firstproject.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.response.MyJoinedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.StudyDTO
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.data.repository.TokenManager
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MainRepository
    private val tokenManager = TokenManager(MyApplication.appContext)

    var accessToken = tokenManager.getAccessToken()

    // 참여 중인 스터디 조회
    private val _joinedStudyResult =
        MutableStateFlow<RequestResult<List<MyJoinedStudyListDtoItem>>>(RequestResult.None())
    val joinedStudyResult: StateFlow<RequestResult<List<MyJoinedStudyListDtoItem>>>
        get() = _joinedStudyResult

    // 전체 스터디 조회
    private val _allStudyListResult =
        MutableStateFlow<RequestResult<List<StudyDTO>>>(RequestResult.None())
    val allStudyListResult: StateFlow<RequestResult<List<StudyDTO>>>
        get() = _allStudyListResult

    // 모든 스터디 목록을 따로 저장
    private val _allStudyList = MutableStateFlow<List<StudyDTO>>(emptyList())
    val allStudyList = _allStudyList.asStateFlow()

    fun getJoinedStudy() {
        viewModelScope.launch {
            _joinedStudyResult.update {
                RequestResult.Progress()
            }

            val result = repository.getMyJoinedStudies(accessToken!!)

            result.onSuccess {
                _joinedStudyResult.update {
                    result
                }
            }.onFailure { code, e ->
                _joinedStudyResult.update {
                    RequestResult.Failure(code, e)
                }
            }

        }
    }

    fun getAllStudyInfo(context: Context) {

        viewModelScope.launch {
            _allStudyListResult.update {
                RequestResult.Progress()
            }

            if (accessToken.isNullOrEmpty()) {
                _allStudyListResult.value = RequestResult.Failure("토큰이 존재하지 않습니다.")
                return@launch
            }

            val result = repository.getAllStudyList(accessToken!!)

            if (result is RequestResult.Success) {
                val list = result.data
                _allStudyList.update { list }
            }

//            result.onSuccess { list ->
//                _allStudyList.value = list
//                _allStudyListResult.value = RequestResult.Success(list)
//            }.onFailure { code, e ->
//                _allStudyListResult.update {
//                    RequestResult.Failure(code, e)
//                }
//            }

            delay(200)

        }
    }

}