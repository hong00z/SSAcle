package com.example.firstproject.ui.matching

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.InviteUserRequestDTO
import com.example.firstproject.data.model.dto.request.SendJoinRequestDTO
import com.example.firstproject.data.model.dto.response.Top3RecommendedUsersDtoItem
import com.example.firstproject.data.model.dto.response.UserSuitableStudyDtoItem
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FindViewModel : ViewModel() {
    private val repository = MainRepository
    var accessToken = tokenManager.getAccessToken()

    // 추천 스터디 통신
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

    // 해당 스터디에 추천하는 스터디원 통신
    private val _personRecommandResult =
        MutableStateFlow<RequestResult<List<Top3RecommendedUsersDtoItem>>>(RequestResult.None())
    val personRecommandResult = _personRecommandResult.asStateFlow()

    private val _personRecommandList =
        MutableStateFlow<List<Top3RecommendedUsersDtoItem>>(emptyList())
    val personRecommandList = _personRecommandList.asStateFlow()

    fun getPersonRecommandList(studyId: String) {
        viewModelScope.launch {
            _personRecommandResult.update {
                RequestResult.Progress()
            }

            val result = repository.getTop3StudyCandidates(
                accessToken = tokenManager.getAccessToken()!!,
                studyId = studyId
            )

            _personRecommandResult.update {
                result
            }

            // 스터디원 리스트 업데이트
            if (result is RequestResult.Success) {
                _personRecommandList.value = result.data
            }

            delay(200)

        }

    }

    // 유저에게 스터디 초대 보내기
    private val _inviteUserResult =
        MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val inviteUserResult = _inviteUserResult.asStateFlow()

    fun sendInviteUser(
        studyId: String,
        request: InviteUserRequestDTO,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _inviteUserResult.update {
                RequestResult.Progress()
            }
            try {
                val result = repository.inviteStudyToUser(
                    accessToken = tokenManager.getAccessToken()!!,
                    studyId = studyId,
                    request = request
                )

                _inviteUserResult.update {
                    result
                }
                onResult(result.isSuccess())
            } catch (e: Exception) {
                _inviteUserResult.value = RequestResult.Failure(e.toString())
                onResult(false)
            }
        }

    }

    // 스터디에 가입 신청하기
    private val _joinStudyResult =
        MutableStateFlow<RequestResult<Unit>>(RequestResult.None())
    val joinStudyResult = _joinStudyResult.asStateFlow()

    fun sendJoinStudy(
        studyId: SendJoinRequestDTO,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _joinStudyResult.update {
                RequestResult.Progress()
            }

            try {
                val result = repository.sendJoinRequest(
                    accessToken = tokenManager.getAccessToken()!!,
                    studyId = studyId
                )

                _joinStudyResult.update { result }
                onResult(result.isSuccess())
            } catch (e: Exception) {
                _joinStudyResult.value = RequestResult.Failure(e.toString())
                onResult(false)
            }

        }
    }
}