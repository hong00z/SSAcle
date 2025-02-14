package com.example.firstproject.data.model.dto.response

data class Top3RecommendedUsersDtoItem(
    val campus: String,
    val countJoinedStudies: Int,
    val image: String,
    val meetingDays: List<String>,
    val nickname: String,
    val similarity: Int,
    val term: String,
    val topics: List<String>,
    val userId: String
)