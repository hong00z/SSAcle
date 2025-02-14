package com.example.firstproject.data.model.dto.response

// swagger 기준 GET/api/studies
data class StudyDTO(
    val id: String,
    val studyName: String,
    val topic: String,
    val meetingDays: List<String>,
    val count: Int,
    val members: List<String>,
    val studyContent: String,
    val createdBy:String
)