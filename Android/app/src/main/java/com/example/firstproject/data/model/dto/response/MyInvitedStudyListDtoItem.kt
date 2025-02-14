package com.example.firstproject.data.model.dto.response

data class MyInvitedStudyListDtoItem(
    val count: Int,
    val meetingDays: List<String>,
    val members: List<String>,
    val studyId: String,
    val studyName: String,
    val topic: String
)