package com.example.firstproject.dto

data class LiveChatMessage(val isMe: Boolean = false, val nickname: String, val message: String)
