package com.example.firstproject.ui.chat

data class ChatRoom(
    val roomName: String, // 채팅방 이름
    val messages: List<ChatMessage> // 메시지 목록
)