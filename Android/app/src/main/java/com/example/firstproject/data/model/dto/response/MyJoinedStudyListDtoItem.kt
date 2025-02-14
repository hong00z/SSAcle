package com.example.firstproject.data.model.dto.response

data class MyJoinedStudyListDtoItem(
    val count: Int,
    val createdBy: String,
    val id: String,
    val meetingDays: List<String>,
    val memberCount: Int,
    val members: List<Member>,
    val studyContent: String,
    val studyName: String,
    val topic: String
)