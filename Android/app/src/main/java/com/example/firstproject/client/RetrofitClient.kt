package com.example.firstproject.client

import com.example.firstproject.service.ChatService
import com.example.firstproject.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val WEBRTC_URL = "https://webrtc.43.203.250.200.nip.io/"
    const val CHAT_API_URL = "https://chat.43.203.250.200.nip.io/"

    val userService: UserService by lazy {
        Retrofit.Builder()
            .baseUrl(CHAT_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserService::class.java)
    }


    val chatService: ChatService by lazy {
        Retrofit.Builder()
            .baseUrl(CHAT_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatService::class.java)
    }

}