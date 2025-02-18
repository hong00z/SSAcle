package com.example.firstproject.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.MyApplication.Companion.USER_ID
import com.example.firstproject.client.RetrofitClient.CHAT_API_URL
import com.example.firstproject.client.RetrofitClient.userService
import com.example.firstproject.databinding.FragmentChatDetailBinding
import com.example.firstproject.dto.Message
import com.example.firstproject.dto.UpdateLastReadRequest
import com.example.firstproject.utils.CommonUtils
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatDetailFragment : Fragment() {
    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ChatDetailFragmentArgs by navArgs()

    // 기존 Message 리스트 대신 ChatItem 리스트를 사용
    private var chatItems = mutableListOf<ChatItem>()

    private lateinit var adapter: MessageAdapter
    private var allMessages = mutableListOf<Message>() // 전체 메시지 리스트

    // Socket.IO 관련 변수
    private lateinit var socket: Socket
    private val gson = Gson()

    private lateinit var studyId: String

    companion object {
        private const val TAG = "ChatDetailFragment_TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SafeArgs로 받은 데이터 확인
        Log.d(TAG, "Received roomName: ${args.roomName}")
        Log.d(TAG, "Received messages size: ${args.messages.size}")
        Log.d(TAG, "Received studyId: ${args.studyId}")
        studyId = args.studyId

        // Toolbar 설정
        binding.chatDetailToolbar.apply {
            title = args.roomName
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.peopleCountTextView.apply {
            text = "${args.count}"
        }

        // SafeArgs로 전달된 messages 데이터를 리스트로 변환
        allMessages = args.messages.toMutableList()
        // ChatItem 리스트 생성: prepareChatItems() 함수로 전체 메시지 기반 날짜 헤더 적용
        chatItems = prepareChatItems(allMessages).toMutableList()

        // RecyclerView 설정 (최신 메시지가 보이도록 stackFromEnd 사용)
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        adapter = MessageAdapter(chatItems)
        binding.messagesRecycler.layoutManager = layoutManager
        binding.messagesRecycler.adapter = adapter

        // 전송 버튼 클릭 시 메시지 전송
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(studyId, USER_ID, messageText)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(requireContext(), "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 새 메시지 인디케이터 클릭 시 맨 아래로 스크롤
        binding.newMessageIndicator.setOnClickListener {
            binding.messagesRecycler.smoothScrollToPosition(chatItems.size - 1)
            binding.newMessageIndicator.visibility = View.GONE
        }

        // 소켓 연결 및 이벤트 등록
        initSocket()
    }

    // 소켓 초기화 및 이벤트 리스너 등록
    private fun initSocket() {
        try {
            socket = IO.socket(CHAT_API_URL)
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization error", e)
            return
        }
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on("newMessage", onNewMessage)
        socket.on("error", onError)
        socket.connect()
    }

    // 연결 성공 시 채팅방 입장
    private val onConnect = Emitter.Listener {
        lifecycleScope.launch {
            joinRoom(studyId, USER_ID)
        }
    }

    // 새 메시지 수신 처리
    private val onNewMessage = Emitter.Listener { args ->
        lifecycleScope.launch {
            if (args.isNotEmpty() && args[0] != null) {
                val messageJson = args[0].toString()
                val newMessage = gson.fromJson(messageJson, Message::class.java)
                withContext(Dispatchers.Main) {
                    // 추가: 만약 현재 스크롤이 하단에 있을 때만 자동 스크롤하도록 할 수도 있음.
                    allMessages.add(newMessage)

                    // 업데이트된 ChatItem 리스트에 새 메시지 추가
                    updateChatItems(newMessage)

                    // 현재 스크롤 위치 확인
                    val layoutManager =
                        binding.messagesRecycler.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    // 만약 마지막 아이템이 보이는 상태라면(즉, 사용자가 하단에 있다면) 자동 스크롤
                    if (lastVisiblePosition >= chatItems.size - 2) {
                        binding.messagesRecycler.smoothScrollToPosition(chatItems.size - 1)
                        binding.newMessageIndicator.visibility = View.GONE
                    } else {
                        binding.newMessageIndicator.text =
                            "${newMessage.nickname}: ${newMessage.message}"
                        binding.newMessageIndicator.visibility = View.VISIBLE
                    }

                }
            }
        }
    }

    // 에러 이벤트 처리
    private val onError = Emitter.Listener { args ->
        lifecycleScope.launch {
            Log.d(TAG, "onError: $args")
        }
    }

    // 채팅방 입장 이벤트 전송
    private fun joinRoom(studyId: String, userId: String) {
        val data = JSONObject().apply {
            put("studyId", studyId)
            put("userId", userId)
        }
        socket.emit("joinRoom", data)
    }

    // 메시지 전송 메서드
    private fun sendMessage(studyId: String, userId: String, message: String) {
        val data = JSONObject().apply {
            put("studyId", studyId)
            put("userId", userId)
            put("message", message)
        }
        socket.emit("sendMessage", data)
    }

    // prepareChatItems: 전체 Message 리스트를 ChatItem 리스트로 변환
    private fun prepareChatItems(messages: List<Message>): List<ChatItem> {
        val items = mutableListOf<ChatItem>()
        var lastDate: String? = null
        for (message in messages) {
            val currentDate = CommonUtils.formatDateForHeader(message.createdAt)
            if (lastDate == null || currentDate != lastDate) {
                items.add(ChatItem.DateHeader(currentDate))
                lastDate = currentDate
            }
            items.add(ChatItem.MessageItem(message))
        }
        return items
    }

    // updateChatItems: 새 메시지가 들어왔을 때 ChatItem 리스트를 업데이트
    private fun updateChatItems(newMessage: Message) {
        val newMessageDate = CommonUtils.formatDateForHeader(newMessage.createdAt)
        // 만약 chatItems가 비어있으면 새 DateHeader와 MessageItem 추가
        if (chatItems.isEmpty()) {
            chatItems.add(ChatItem.DateHeader(newMessageDate))
            chatItems.add(ChatItem.MessageItem(newMessage))
        } else {
            // 마지막 DateHeader 찾기
            var lastHeaderDate: String? = null
            for (i in chatItems.size - 1 downTo 0) {
                when (val item = chatItems[i]) {
                    is ChatItem.DateHeader -> {
                        lastHeaderDate = item.date
                        break
                    }

                    else -> continue
                }
            }
            if (lastHeaderDate == newMessageDate) {
                // 같은 날짜이면 단순히 메시지 항목 추가
                chatItems.add(ChatItem.MessageItem(newMessage))
            } else {
                // 날짜가 다르면 새 DateHeader와 메시지 항목 추가
                chatItems.add(ChatItem.DateHeader(newMessageDate))
                chatItems.add(ChatItem.MessageItem(newMessage))
            }
        }
        adapter.notifyDataSetChanged()  // 상황에 따라 notifyItemInserted() 등 세밀하게 갱신 가능
    }

    private fun updateLastReadTimeOnServer() {
        // 현재 시간을 밀리초 단위로 가져옴
        val currentTime = System.currentTimeMillis()

        // 업데이트 요청 객체 생성 (args.studyId는 SafeArgs로 전달받은 채팅방 ID)
        val request = UpdateLastReadRequest(studyId, lastReadTime = currentTime)

        // 백그라운드에서 API 호출
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.updateLastReadTime(USER_ID, request)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to update lastReadTime: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while updating lastReadTime", e)
            }
        }
    }

    override fun onStop() {
        updateLastReadTimeOnServer()
        Log.d(TAG, "onStop: 읽기 업데이트")
        super.onStop()
    }

    override fun onDestroyView() {
        socket.disconnect()
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off("newMessage", onNewMessage)
        socket.off("error", onError)
        _binding = null

        super.onDestroyView()
    }

}
