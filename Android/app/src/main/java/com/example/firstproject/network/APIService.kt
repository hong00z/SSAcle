package com.example.firstproject.network

import com.example.firstproject.data.model.dto.response.AllStudyListResponseDTO
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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {

    @POST("/api/auth/kakao")
    suspend fun kakaoLogin(@Header("Authorization") accessToken: String): Response<CommonResponseDTO<KakaoTokenDTO>>

    @GET("/api/auth/newToken")
    suspend fun getRefreshToken(@Header("Authorization") refreshToken: String): Response<CommonResponseDTO<RefreshTokenDTO>>

    @GET("/api/studies")
    suspend fun getAllStudies(@Header("Authorization") accessToken: String): Response<List<StudyDTO>>


    // CommonDTO로 감싸져서 있길래, 삭제함. (특정 스터디 조회)
    @GET("/api/studies/{studyId}")
    suspend fun getStudyDetailInfo(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String
    ): Response<StudyDetailInfoResponseDTO>

    // 스터디내 초대 현황
    @GET("/api/studies/{studyId}/wishList")
    suspend fun getStudyInvitedMembers(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String
    ): Response<List<StudyRequestedInviteListDtoItem>>

    // 스터디내 수신함
    @GET("/api/studies/{studyId}/preList")
    suspend fun getStudyJoinRequests(
        @Header("Authorization") accessToken: String,
        @Path("studyId") studyId: String
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
    ): Response<MyJoinedStudyListDtoItem>

    // 내 수신함
    @GET("/api/user/invited-studies")
    suspend fun getMyInvitedStudies(
        @Header("Authorization") accessToken: String
    ): Response<MyInvitedStudyListDtoItem>



    // 스터디 개설 (pass)
    // 스터디원 스카웃 제의 추가/ 내 수신함 추가 (pass)
    // 유저의 요청 수락 (pass)

    // 피드 생성 (pass)

    // 닉네임 중복 검사 (pass)
    // 싸피생 인증 (pass)
    // 내 신청 보내기 (pass)
}