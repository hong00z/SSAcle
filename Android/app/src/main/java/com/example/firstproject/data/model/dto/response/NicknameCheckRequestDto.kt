package com.example.firstproject.data.model.dto.response

// GET/api/user 닉네임 중복 검사
data class NicknameCheckRequestDto(
    val code: Int,
    val message: String,
    val `data`: String

)