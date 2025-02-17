package com.example.firstproject.data.model.dto.response

// swagger 기준 GET/api/studies
data class StudyDTO(
    val studyId: String,
    val studyName: String,
    val topic: String,
    val meetingDays: List<String>,
    val count: Int,
    val memberCount: Int,
    val members: List<Member>,
)

data class StudyInfoDTO(
    val nickname: String,
    val image: String,
    val creator: Boolean
)