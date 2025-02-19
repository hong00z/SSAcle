package com.example.firstproject.data.repository

import android.util.Log
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.data.model.dto.request.AuthRequestDTO
import com.example.firstproject.data.model.dto.request.EditProfileRequestDTO
import com.example.firstproject.data.model.dto.request.InviteUserRequestDTO
import com.example.firstproject.data.model.dto.request.NicknameRequestDTO
import com.example.firstproject.data.model.dto.request.RegisterStudyRequestDTO
import com.example.firstproject.data.model.dto.request.SendJoinRequestDTO
import com.example.firstproject.data.model.dto.response.AuthResponseDTO
import com.example.firstproject.data.model.dto.response.EditProfileResponseDTO
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
import java.io.File

object MainRepository {
    private val remoteDataSource = RemoteDataSource()
//    private val tokenManager = TokenManager(MyApplication.appContext)

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

    // 모집 중인 스터디 리스트 조회
    suspend fun getAllStudyList(accessToken: String): RequestResult<List<StudyDTO>> {
        return remoteDataSource.getAllStudy(accessToken)
    }

    // 싸피생 인증
    suspend fun sendAuthUser(
        accessToken: String,
        request: AuthRequestDTO
    ): RequestResult<CommonResponseDTO<AuthResponseDTO>> {
        return remoteDataSource.AuthUser(accessToken, request)
    }

    // 닉네임 중복검사
    suspend fun getCheckNickName(
        accessToken: String,
        nickname: NicknameRequestDTO
    ): RequestResult<CommonResponseDTO<Boolean>> {
        return remoteDataSource.CheckNickName(accessToken, nickname)
    }

    // 온보딩 등록
    suspend fun sendOnboardingProfile(
        accessToken: String,
        request: EditProfileRequestDTO,
        imageFile: File?
    ): RequestResult<EditProfileResponseDTO> {
        return remoteDataSource.OnboardingProfile(accessToken, request, imageFile)
    }

    // 스터디 개설
    suspend fun sendRegisterStudy(
        accessToken: String,
        request: RegisterStudyRequestDTO
    ): RequestResult<Unit> {
        return remoteDataSource.sendRegisterStudy(accessToken, request)
    }

    // /api/studies/{studyId} 특정 스터디 조회
    suspend fun getStudyDetailInfo(
        accessToken: String,
        studyId: String
    ): RequestResult<StudyDetailInfoResponseDTO> {
        return remoteDataSource.getStudyDetailInfo(accessToken, studyId)
    }

    // /api/studies/{studyId}/wishList 스터디내 초대 현황
    suspend fun getStudyInvitedMembers(
        accessToken: String,
        studyId: String
    ): RequestResult<List<StudyRequestedInviteListDtoItem>> {
        return remoteDataSource.getStudyInvitedMembers(accessToken, studyId)
    }

    // /api/studies/{studyId}/preList 스터디내 수신함
    suspend fun getStudyJoinRequests(
        accessToken: String,
        studyId: String
    ): RequestResult<List<StudyJoinRequestListDtoItem>> {
        return remoteDataSource.getStudyJoinRequests(accessToken, studyId)
    }

    // /api/studies/recommendUser/{studyId} 스터디원 추천
    suspend fun getTop3StudyCandidates(
        accessToken: String,
        studyId: String
    ): RequestResult<List<Top3RecommendedUsersDtoItem>> {
        return remoteDataSource.getTop3StudyCandidates(accessToken, studyId)
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
    suspend fun getMyInvitedStudies(accessToken: String): RequestResult<List<MyInvitedStudyListDtoItem>> {
        return remoteDataSource.getMyInvitedStudies(accessToken)
    }

    // 유저에게 스터디 초대 보내기
    suspend fun inviteStudyToUser(accessToken: String, studyId: String, request: InviteUserRequestDTO): RequestResult<Unit> {
        return remoteDataSource.inviteStudyToUser(accessToken, studyId, request)
    }

    // 스터디에 가입 요청 보내기
    suspend fun sendJoinRequest(accessToken: String, studyId: SendJoinRequestDTO): RequestResult<Unit> {
        return remoteDataSource.sendJoinRequest(accessToken, studyId)
    }

    // 가입요청 수락
    suspend fun acceptUserJoin(accessToken: String, studyId: String, request: InviteUserRequestDTO): RequestResult<Unit> {
        return remoteDataSource.AcceptJoinUser(accessToken, studyId, request)
    }

    // 스터디 초대 수락
    suspend fun acceptStudyJoin(accessToken: String, request: SendJoinRequestDTO): RequestResult<Unit> {
        return remoteDataSource.acceptJoinStudy(accessToken, request)
    }
}
