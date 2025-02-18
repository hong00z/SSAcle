package com.example.firstproject.dto

import org.mediasoup.droid.Consumer

data class LiveMember(
    val isMe: Boolean = false,
    val nickname: String = "나",
    var peerId: String? = null,
    var videoConsumer: Consumer? = null,
    var audioConsumer: Consumer? = null,
)
