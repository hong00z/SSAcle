package com.example.firstproject.data.model.dto.response

data class MyAppliedStudyListDtoItem(
    val count: Int,
    val meetingDays: List<String>,
    val members: List<String>,
    val studyId: String,
    val studyName: String,
    val topic: String
)