package com.example.firstproject.ui.home.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.InviteUserRequestDTO
import com.example.firstproject.data.model.dto.response.StudyJoinRequestListDtoItem
import com.example.firstproject.data.model.dto.response.StudyRequestedInviteListDtoItem
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyNotificationViewModel : ViewModel() {
    private val repository = MainRepository

    // 스터디 초대 보낸 내역
    private val _studyInvitedResult =
        MutableStateFlow<RequestResult<List<StudyRequestedInviteListDtoItem>>>(RequestResult.None())
    val studyInvitedResult = _studyInvitedResult.asStateFlow()

    fun getStudyInvitedMember(studyId: String) {
        viewModelScope.launch {
            _studyInvitedResult.update {
                RequestResult.Progress()
            }

            val result = repository.getStudyInvitedMembers(
                accessToken = tokenManager.getAccessToken()!!,
                studyId = studyId
            )

            _studyInvitedResult.update {
                result
            }

        }
    }

    // 스터다 가입 요청 목록
    private val _joinWishResult =
        MutableStateFlow<RequestResult<List<StudyJoinRequestListDtoItem>>>(RequestResult.None())
    val joinWishResult = _joinWishResult.asStateFlow()

    fun getJoinWishUser(studyId: String) {
        viewModelScope.launch {
            _joinWishResult.update {
                RequestResult.Progress()
            }

            val result = repository.getStudyJoinRequests(
                accessToken = tokenManager.getAccessToken()!!,
                studyId = studyId
            )

            _joinWishResult.update {
                result
            }

        }
    }

    // 가입 요청 수락
    private val _acceptUserResult =
        MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val acceptUserRequest = _acceptUserResult.asStateFlow()

    fun sendAcceptUser(studyId: String, request: InviteUserRequestDTO) {
        viewModelScope.launch {
            _acceptUserResult.update {
                RequestResult.Progress()
            }

            val result = repository.acceptUserJoin(
                accessToken = tokenManager.getAccessToken()!!,
                studyId = studyId,
                request = request
            )

            _acceptUserResult.update {
                result
            }

        }
    }

}