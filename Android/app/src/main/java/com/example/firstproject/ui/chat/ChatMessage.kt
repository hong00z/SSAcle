package com.example.firstproject.ui.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMessage(
    val sender: String, // 메시지 보낸 사람
    val content: String, // 메시지 내용
    val time: String, // 보낸 시간
    val isMine: Boolean // 내가 보낸 메시지인지 여부

) : Parcelable