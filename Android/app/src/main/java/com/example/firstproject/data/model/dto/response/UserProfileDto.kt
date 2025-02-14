package com.example.firstproject.data.model.dto.response

// GET/api/user/profile 프로필 조회
data class UserProfileDto(
    val code: Int,
    val `data`: Data,
    val message: String
)