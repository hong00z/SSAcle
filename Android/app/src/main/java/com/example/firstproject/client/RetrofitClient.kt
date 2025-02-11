package com.example.firstproject.client

import com.example.firstproject.service.ChatService
import com.example.firstproject.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val CHAT_API_URL = "http://192.168.137.105:4001"
    const val USER_ID = "67a5e7f43d3fc61ef2203113"

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