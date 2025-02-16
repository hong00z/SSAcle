package com.example.firstproject.data.model.dto.response

// PATCH/api/user/profile 프로필 수정
data class EditProfileResponseDTO(
    val campus: String,
    val image: String,
    val meetingDays: List<String>,
    val nickname: String,
    val term: String,
    val topics: List<String>
)