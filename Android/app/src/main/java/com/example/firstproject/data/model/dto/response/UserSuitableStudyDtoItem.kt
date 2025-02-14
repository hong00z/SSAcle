package com.example.firstproject.data.model.dto.response

data class UserSuitableStudyDtoItem(
    val count: Int,
    val meetingDays: List<String>,
    val memberCount: Int,
    val members: List<Member>,
    val similarity: Int,
    val studyId: String,
    val studyName: String,
    val topic: String
)