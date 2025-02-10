package com.example.firstproject.dto

import com.google.gson.annotations.SerializedName

data class Study(
    @SerializedName("_id") val id: String,
    val studyName: String,
    val image: String = "",
    val topic: String,
    val meetingDays: List<String> = emptyList(),
    val count: Int,
    val members: List<String> = emptyList(),
    val studyContent: String = "",
    val wishMembers: List<String> = emptyList(),
    val preMembers: List<String> = emptyList(),
    val feeds: List<String> = emptyList(),
    val createdAt: String = "",
    var lastMessage: String?,
    var lastMessageCreatedAt: String?,
    var unreadCount: Int?
)
