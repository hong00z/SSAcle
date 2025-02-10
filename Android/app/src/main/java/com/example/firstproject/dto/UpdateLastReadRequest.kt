package com.example.firstproject.dto

data class UpdateLastReadRequest(
    val studyId: String,
    val lastReadTime: Long  // Unix epoch 밀리초 값
)
