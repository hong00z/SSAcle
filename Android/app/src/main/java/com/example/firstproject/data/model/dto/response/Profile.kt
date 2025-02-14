package com.example.firstproject.data.model.dto.response

// 프로필 조회
data class Profile(
    val campus: String,
    val image: String,
    val meetingDays: List<String>,
    val nickname: String,
    val term: String,
    val topics: List<String>
)