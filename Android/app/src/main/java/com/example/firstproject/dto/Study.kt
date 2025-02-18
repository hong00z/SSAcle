package com.example.firstproject.dto

import com.google.gson.annotations.SerializedName

data class Study(
    @SerializedName("_id") val id: String,
    val studyName: String,
    val createdBy: String,
    val count: Int,
    val members: List<ChatMember> = emptyList(),
    var unreadCount: Int?,
    var lastMessage: String?,
    var lastMessageCreatedAt: String?,
    val image: String = "",
)

data class ChatMember(
    val userId: String,
    val nickname: String,
    val image: String
)