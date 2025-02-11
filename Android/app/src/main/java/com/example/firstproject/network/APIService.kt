package com.example.firstproject.network

import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.example.firstproject.data.model.dto.response.RefreshTokenDTO
import com.example.firstproject.data.model.dto.response.common.CommonResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface APIService {

    @POST("/api/auth/kakao")
    suspend fun kakaoLogin(@Header("Authorization") accessToken: String): Response<CommonResponseDTO<KakaoTokenDTO>>

    @GET("/api/auth/newToken")
    suspend fun getRefreshToken(@Header("Authorization") refreshToken: String): Response<CommonResponseDTO<RefreshTokenDTO>>
}