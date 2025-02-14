package com.example.firstproject.data.model.dto.response

data class Feed(
    val content: String,
    val createdAt: String,
    val creatorInfo: CreatorInfo,
    val study: String,
    val title: String
)