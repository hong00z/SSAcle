package com.example.firstproject.data.model.dto.response

data class Data(
    val campus: String,
    val image: String,
    val meetingDays: List<String>,
    val nickname: String,
    val term: String,
    val topics: List<String>
)