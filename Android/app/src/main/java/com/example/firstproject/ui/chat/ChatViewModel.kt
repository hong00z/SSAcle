package com.example.firstproject.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstproject.client.RetrofitClient.chatService
import com.example.firstproject.dto.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    val TAG = "ChatViewModel"

    // 채팅방의 메시지를 불러오는 함수
    fun fetchChatMessages(studyId: String, onResult: (List<Message>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = chatService.getMessages(studyId)
                Log.d(TAG, "fetchChatMessages: $response")
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onResult(response.body()!!)
                    }
                } else {
                    Log.d(TAG, "메시지 목록을 불러올 수 없습니다.")
                }
            } catch (e: Exception) {
                Log.d(TAG, "네트워크 오류: ${e.message}")
            }
        }
    }

}