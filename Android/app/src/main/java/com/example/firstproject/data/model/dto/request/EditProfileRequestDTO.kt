package com.example.firstproject.data.model.dto.request

data class EditProfileRequestDTO(
    var nickname: String,
    var topics: List<String>,
    var meetingDays: List<String>
)
