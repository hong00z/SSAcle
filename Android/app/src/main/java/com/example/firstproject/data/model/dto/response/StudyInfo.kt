package com.example.firstproject.data.model.dto.response

data class StudyInfo(
    val title: String,
    val topic: String,
    val personNum: Int,
    val isHost: Boolean,
    val isNewAlarm: Boolean
)
