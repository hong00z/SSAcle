package com.example.firstproject.data.model.dto.request

data class RegisterStudyRequestDTO(
    val studyName: String,
    val topic: String,
    val meetingDays: List<String>,
    val count: Int,
    val studyContent: String
)
