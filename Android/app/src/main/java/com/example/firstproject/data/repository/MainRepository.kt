package com.example.firstproject.data.repository

import com.example.firstproject.MyApplication
import com.example.firstproject.data.model.dto.request.AuthRequestDTO
import com.example.firstproject.data.model.dto.response.AllStudyListResponseDTO
import com.example.firstproject.data.model.dto.response.AuthResponseDTO
import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.example.firstproject.data.model.dto.response.MyAppliedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.MyInvitedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.MyJoinedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.Profile
import com.example.firstproject.data.model.dto.response.StudyDTO
import com.example.firstproject.data.model.dto.response.StudyDetailInfoResponseDTO
import com.example.firstproject.data.model.dto.response.StudyJoinRequestListDtoItem
import com.example.firstproject.data.model.dto.response.StudyRequestedInviteListDtoItem
import com.example.firstproject.data.model.dto.response.Top3RecommendedUsersDtoItem
import com.example.firstproject.data.model.dto.response.UserSuitableStudyDtoItem
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import com.rootachieve.requestresult.RequestResult

object MainRepository {
    private val remoteDataSource = RemoteDataSource()
    private val tokenManager = TokenManager(MyApplication.appContext)

    // 카카오 로그인
    suspend fun loginWithKakao(accessToken: String): RequestResult<Unit> {
        return when (val result = remoteDataSource.loginWithKakao(accessToken)) {
            is RequestResult.Success -> {
                tokenManager.saveAccessToken(result.data.accessToken)
                tokenManager.saveRefreshToken(result.data.refreshToken)
                RequestResult.Success(Unit) // 성공 결과 반환
            }

            is RequestResult.Failure -> RequestResult.Failure(result.exception.toString())
            is RequestResult.None -> TODO()
            is RequestResult.Progress -> TODO()
        }
    }

    // 리프레쉬 토큰
    suspend fun refreshAccessToken(): RequestResult<Unit> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return RequestResult.Failure("리프레시 토큰 없음")

        return when (val result = remoteDataSource.refreshAccessToken(refreshToken)) {
            is RequestResult.Success -> {
                tokenManager.saveAccessToken(result.data.accessToken)
                RequestResult.Success(Unit)
            }

            is RequestResult.Failure -> RequestResult.Failure(result.exception.toString())
            is RequestResult.Progress -> TODO()
            is RequestResult.None -> TODO()
        }
    }

    suspend fun kakaoLogin(accessToken: String): RequestResult<KakaoTokenDTO> {
        return remoteDataSource.loginWithKakao(accessToken)
    }

    suspend fun getAllStudyList(accessToken: String): RequestResult<List<StudyDTO>> {
        return remoteDataSource.getAllStudy(accessToken)
    }

    // 싸피생 인증
    suspend fun sendAuthUser(accessToken: String, request: AuthRequestDTO): RequestResult<CommonResponseDTO<AuthResponseDTO>> {
        return remoteDataSource.AuthUser(accessToken, request)
    }

    // /api/studies/{studyId} 특정 스터디 조회
    suspend fun getStudyDetailInfo(
        accessToken: String,
        studyId: String
    ): RequestResult<StudyDetailInfoResponseDTO> {
        return remoteDataSource.getStudyDetailInfo(accessToken)
    }

    // /api/studies/{studyId}/wishList 스터디내 초대 현황
    suspend fun getStudyInvitedMembers(
        accessToken: String,
        studyId: String
    ): RequestResult<List<StudyRequestedInviteListDtoItem>> {
        return remoteDataSource.getStudyInvitedMembers(accessToken)
    }

    // /api/studies/{studyId}/preList 스터디내 수신함
    suspend fun getStudyJoinRequests(
        accessToken: String,
        studyId: String
    ): RequestResult<List<StudyJoinRequestListDtoItem>> {
        return remoteDataSource.getStudyJoinRequests(accessToken)
    }

    // /api/studies/recommendUser/{studyId} 스터디원 추천
    suspend fun getTop3StudyCandidates(
        accessToken: String,
        studyId: String
    ): RequestResult<List<Top3RecommendedUsersDtoItem>> {
        return remoteDataSource.getTop3StudyCandidates(accessToken)
    }

    // /api/studies/recommendStudy 스터디 추천기능
    suspend fun getRecommendedStudies(accessToken: String): RequestResult<List<UserSuitableStudyDtoItem>> {
        return remoteDataSource.getRecommendedStudies(accessToken)
    }

    // /api/user 로그아웃
    suspend fun logout(accessToken: String): RequestResult<CommonResponseDTO<Unit>> {
        return remoteDataSource.logout(accessToken)
    }

    // /api/user 회원 탈퇴
    suspend fun deleteUserAccount(accessToken: String): RequestResult<CommonResponseDTO<Unit>> {
        return remoteDataSource.deleteUserAccount(accessToken)
    }

    // /api/user/wish-studies 내 신청 현황 리스트
    suspend fun getMyAppliedStudies(accessToken: String): RequestResult<List<MyAppliedStudyListDtoItem>> {
        return remoteDataSource.getMyAppliedStudies(accessToken)
    }

    // /api/user/profile 프로필 조회
    suspend fun getUserProfile(accessToken: String): RequestResult<CommonResponseDTO<Profile>> {
        return remoteDataSource.getUserProfile(accessToken)
    }

    // /api/user/my-studies 내 스터디 리스트
    suspend fun getMyJoinedStudies(accessToken: String): RequestResult<List<MyJoinedStudyListDtoItem>> {
        return remoteDataSource.getMyJoinedStudies(accessToken)
    }

    // /api/user/invited-studies 내 수신함
    suspend fun getMyInvitedStudies(accessToken: String): RequestResult<MyInvitedStudyListDtoItem> {
        return remoteDataSource.getMyInvitedStudies(accessToken)
    }

}
