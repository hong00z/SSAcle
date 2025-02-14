package com.example.firstproject.data.model.dto.response

// POST/api/user/ssafy 싸피생 인증
data class SsafyAuthResponseDto(
    val code: Int,
    val profile: Profile,
    val message: String
)