package com.example.firstproject.ui.home.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.SendJoinRequestDTO
import com.example.firstproject.data.model.dto.response.MyAppliedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.MyInvitedStudyListDtoItem
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = MainRepository

    // 내가 신청한 목록 현황 보기
    private val _myAppliedResult =
        MutableStateFlow<RequestResult<List<MyAppliedStudyListDtoItem>>>(RequestResult.None())
    val myAppliedResult = _myAppliedResult.asStateFlow()

    fun getMyAppliedInfo() {
        viewModelScope.launch {
            _myAppliedResult.update {
                RequestResult.Progress()
            }

            val result =
                repository.getMyAppliedStudies(accessToken = tokenManager.getAccessToken()!!)

            _myAppliedResult.update {
                result
            }

        }
    }

    // 나에게 온 스터디 초대 목록 보기
    private val _inviteResult =
        MutableStateFlow<RequestResult<List<MyInvitedStudyListDtoItem>>>(RequestResult.None())
    val inviteResult = _inviteResult.asStateFlow()

    fun getInviteStudyInfo() {
        viewModelScope.launch {
            _inviteResult.update {
                RequestResult.Progress()
            }

            val result =
                repository.getMyInvitedStudies(accessToken = tokenManager.getAccessToken()!!)

            _inviteResult.update {
                result
            }

        }
    }

    // 스터디 가입 수락
    private val _acceptStudyResult = MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val acceptStudyResult = _acceptStudyResult.asStateFlow()

    fun acceptJoin(request: SendJoinRequestDTO) {
        viewModelScope.launch {
            _acceptStudyResult.update {
                RequestResult.Progress()
            }

            val result = repository.acceptStudyJoin(
                accessToken = tokenManager.getAccessToken()!!,
                request
            )

            _acceptStudyResult.update {
                result
            }

        }
    }
}