package com.example.firstproject.data.model.dto.response

data class StudyJoinRequestListDtoItem(
    val campus: String,
    val image: String,
    val meetingDays: List<String>,
    val nickname: String,
    val term: String,
    val topics: List<String>,
    val userId: String
)