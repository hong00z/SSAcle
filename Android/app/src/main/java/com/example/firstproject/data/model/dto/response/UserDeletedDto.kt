package com.example.firstproject.data.model.dto.response

// DELETE/api/user 회원탈퇴
data class UserDeletedDto(
    val code: Int,
    val `data`: Data,
    val message: String
)