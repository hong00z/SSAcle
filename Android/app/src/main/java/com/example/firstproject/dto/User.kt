package com.example.firstproject.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val email: String,
    val nickname: String,
    val imageUrl: String? = "",
    val term: Int,
    val campus: String,
    val topics: List<String> = emptyList(),
    val meetingDays: List<String> = emptyList(),
    val joinedStudies: List<String> = emptyList(),
    val wishStudies: List<String> = emptyList(),
    val invitedStudies: List<String> = emptyList(),
    val refreshToken: String? = "",
)