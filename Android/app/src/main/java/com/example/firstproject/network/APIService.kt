package com.example.firstproject.network

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
import com.example.firstproject.data.model.dto.response.RefreshTokenDTO
import com.example.firstproject.data.model.dto.response.StudyDTO
import com.example.firstproject.data.model.dto.response.StudyDetailInfoResponseDTO
import com.example.firstproject.data.model.dto.response.StudyJoinRequestListDtoItem
import com.example.firstproject.data.model.dto.response.StudyRequestedInviteListDtoItem
import com.example.firstproject.data.model.dto.response.Top3RecommendedUsersDtoItem
import com.example.firstproject.data.model.dto.response.UserSuitableStudyDtoItem
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {

    @POST("/api/auth/kakao")
    suspend fun kakaoLogin(@Header("Authorization") accessToken: String): Response<CommonResponseDTO<KakaoTokenDTO>>

    @GET("/api/auth/newToken")
    suspend fun getRefreshToken(@Header("Authorization") refreshToken: String): Response<CommonResponseDTO<RefreshTokenDTO>>

    // 모집 중인 스터디 리스트 조회
    @GET("/api/studies/recruitingStudy")
    suspend fun getAllStudies(@Header("Authorization") accessToken: String): Response<List<StudyDTO>>


    // CommonDTO로 감싸져서 있길래, 삭제함.
    // (특정 스터디 조회)
    @GET("/api/studies/{studyId}")
    suspend fun getStudyDetailInfo(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String
    ): Response<StudyDetailInfoResponseDTO>

    // 스터디내 초대 현황
    @GET("/api/studies/{studyId}/wishList")
    suspend fun getStudyInvitedMembers(
        @Header("Authorization") accessToken: String,
        @Query("studyId") studyId: String
    ): Response<List<StudyRequestedInviteListDtoItem>>

    // 스터디내 수신함
    @GET("/api/studies/{studyId}/preList")
    suspend fun getStudyJoinRequests(
        @Header("Authorization") accessToken: String,
        @Query("studyId") studyId: String
    ): Response<List<StudyJoinRequestListDtoItem>>

    // 탑3 스터디원 추천
    @GET("/api/studies/recommendUser/{studyId}")
    suspend fun getTop3StudyCandidates(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String
    ): Response<List<Top3RecommendedUsersDtoItem>>

    // 유저의 조건에 맞는 스터디 추천 기능
    @GET("/api/studies/recommendStudy")
    suspend fun getRecommendedStudies(
        @Header("Authorization") accessToken: String
    ): Response<List<UserSuitableStudyDtoItem>>


    // 로그아웃
    @POST("/api/user")
    suspend fun logout(
        @Header("Authorization") accessToken: String
    ): Response<CommonResponseDTO<Unit>>

    // 회원 탈퇴
    @DELETE("/api/user")
    suspend fun deleteUserAccount(
        @Header("Authorization") accessToken: String
    ): Response<CommonResponseDTO<Unit>>

    // 내 신청 현황 리스트
    @GET("/api/user/wish-studies")
    suspend fun getMyAppliedStudies(
        @Header("Authorization") accessToken: String
    ): Response<List<MyAppliedStudyListDtoItem>>

    // 프로필 조회
    @GET("/api/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") accessToken: String
    ): Response<CommonResponseDTO<Profile>>

    // 내 스터디 리스트
    @GET("/api/user/my-studies")
    suspend fun getMyJoinedStudies(
        @Header("Authorization") accessToken: String
    ): Response<List<MyJoinedStudyListDtoItem>>

    // 내 수신함
    @GET("/api/user/invited-studies")
    suspend fun getMyInvitedStudies(
        @Header("Authorization") accessToken: String
    ): Response<List<MyInvitedStudyListDtoItem>>


    // 스터디 개설 (pass)
    @POST("/api/studies")
    suspend fun registerStudy(
        @Header("Authorization") accessToken: String,
        @Body studyInfo: RegisterStudyRequestDTO
    ): Response<Unit>

    // 스터디원 스카웃 제의 추가/ 내 수신함 추가 (pass)
    @PATCH("/api/studies/{studyId}/accept")
    suspend fun AcceptJoinUser(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String,
        @Body request: InviteUserRequestDTO
    ): Response<Unit>

    // 유저의 요청 수락 (pass)



    // 피드 생성 (pass)

    // 닉네임 중복 검사 (pass)
    @POST("/api/user/nickname")
    suspend fun CheckNickName(
        @Header("Authorization") accessToken: String,
        @Body nickname: NicknameRequestDTO
    ): Response<CommonResponseDTO<Boolean>>

    // 싸피생 인증 (pass)
    @POST("/api/user/ssafy")
    suspend fun AuthUser(
        @Header("Authorization") accessToken: String,
        @Body request: AuthRequestDTO
    ): Response<CommonResponseDTO<AuthResponseDTO>>

    // 온보딩 프로필 등록
    @Multipart
    @PATCH("/api/user/profile")
    suspend fun OnboardingProfile(
        @Header("Authorization") accessToken: String,
        @Part("request") request: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<EditProfileResponseDTO>

    // 프로필 수정
    @Multipart
    @PATCH("/api/user/profile")
    suspend fun editUserProfile(
        @Header("Authorization") accessToken: String,
        @Part("request") request: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<CommonResponseDTO<EditProfileResponseDTO>>

    // 유저에게 스터디 초대(가입) 요청
    @PATCH("/api/studies/{studyId}/addStudyRequest")
    suspend fun inviteStudyToUser(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String,
        @Body request: InviteUserRequestDTO
    ): Response<Unit>

    // 스터디에 가입 요청 보내기
    @PATCH("/api/user/wish-studies")
    suspend fun sendJoinRequest(
        @Header("Authorization") accessToken: String,
        @Body studyId: SendJoinRequestDTO
    ): Response<Unit>

    // 수신함에서 초대 수락
    @PATCH("/api/user/invited-studies/accept")
    suspend fun acceptJoinStudy(
        @Header("Authorization") accessToken: String,
        @Body request: SendJoinRequestDTO
    ): Response<Unit>
}