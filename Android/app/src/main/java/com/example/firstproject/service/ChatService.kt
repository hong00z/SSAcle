package com.example.firstproject.service

import com.example.firstproject.dto.Message
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatService {
    @GET("api/chat/{studyId}/messages")
    suspend fun getMessages(@Path("studyId") studyId: String): Response<List<Message>>

}