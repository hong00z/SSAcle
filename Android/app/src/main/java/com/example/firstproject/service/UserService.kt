package com.example.firstproject.service

import com.example.firstproject.dto.Study
import com.example.firstproject.dto.TokenUpdateRequest
import com.example.firstproject.dto.UpdateLastReadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET("api/users/{userId}/studies")
    suspend fun getJoinedStudies(@Path("userId") userId: String): Response<List<Study>>

    @POST("api/users/{userId}/lastRead")
    suspend fun updateLastReadTime(
        @Path("userId") userId: String, @Body request: UpdateLastReadRequest
    ): Response<Unit>

    @POST("api/users/{userId}/fcmToken")
    suspend fun updateFcmToken(
        @Path("userId") userId: String,
        @Body tokenUpdateRequest: TokenUpdateRequest
    ): Response<Unit>
}