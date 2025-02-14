package com.example.firstproject.data.model.dto.response

// POST/api/user 로그아웃
data class LogoutDTO(
    val code: Int,
    val `data`: Data,
    val message: String
)