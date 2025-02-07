package com.example.firstproject.service

import com.example.firstproject.dto.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("api/users/{userId}")
    fun getUser(@Path("userId") userId: String): Call<User>
}