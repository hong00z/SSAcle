package com.example.firstproject.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.client.RetrofitClient.CHAT_API_URL
import com.example.firstproject.client.RetrofitClient.USER_ID
import com.example.firstproject.client.RetrofitClient.chatService
import com.example.firstproject.client.RetrofitClient.userService
import com.example.firstproject.databinding.FragmentChatBinding
import com.example.firstproject.dto.Message
import com.example.firstproject.dto.Study
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant


const val TAG = "ChatFragment_TAG"

class ChatFragment : Fragment() {

    private val studyList: MutableList<Study> = mutableListOf()

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var studyAdapter: StudyAdapter

    // Socket.IO 관련 변수
    private lateinit var socket: Socket
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 스와이프하여 새로고침 시 채팅방 목록 갱신
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchJoinedStudies()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // RecyclerView 설정
        binding.chatListRecycler.layoutManager = LinearLayoutManager(requireContext())
        studyAdapter = StudyAdapter(studyList) { study ->
            // 채팅방 클릭 시, 해당 채팅방의 메시지를 불러옴
            Log.d(TAG, "onCreateView: studyId=${study.id}")
            fetchChatMessages(study.id) { messages ->
                // 메시지 데이터를 ChatDetailFragment로 전달 (네비게이션 SafeArgs 사용)
                val action = ChatFragmentDirections.actionChatFragmentToChatDetailFragment(
                    studyId = study.id,
                    roomName = study.studyName,
                    count = study.members.count(),
                    messages = messages.toTypedArray() // 배열로 변환하여 전달 (NavArgs 설정에 맞게 조정)
                )
                findNavController().navigate(action)
            }
        }

        binding.chatListRecycler.adapter = studyAdapter

        initSocket()
    }

    // Study 목록을 가져오고 어댑터 갱신
    private fun fetchJoinedStudies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.getJoinedStudies(USER_ID)
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        studyList.clear()
                        studyList.addAll(response.body()!!)

                        // studyList 정렬: 마지막 메시지 시간이 최근한 순으로 내림차순 정렬
                        studyList.sortByDescending { study ->
                            study.lastMessageCreatedAt?.let {
                                try {
                                    // API 26 이상: java.time.Instant 사용
                                    Instant.parse(it).toEpochMilli()
                                } catch (e: Exception) {
                                    0L
                                }
                            } ?: 0L
                        }
                        studyAdapter.notifyDataSetChanged()

                        // 소켓 연결 후, 각 채팅방에 대해 joinRoom 호출
                        joinAllStudyRooms()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(), "스터디 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // 채팅방의 메시지를 불러오는 함수
    private fun fetchChatMessages(studyId: String, onResult: (List<Message>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = chatService.getMessages(studyId)
                Log.d(TAG, "fetchChatMessages: $response")
                if (response.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        onResult(response.body()!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(), "메시지 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
                Log.d(TAG, "fetchChatMessages: $e")
            }
        }
    }

    // Socket 초기화: ChatFragment에서는 사용자가 속한 모든 채팅방에 대한 새 메시지 이벤트를 듣습니다.
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

    // 연결 성공 시 모든 채팅방에 joinRoom 호출
    private val onConnect = Emitter.Listener {
        lifecycleScope.launch {
            // 사용자가 가입한 각 스터디 채팅방에 입장
//            joinAllStudyRooms()
        }
    }

    private fun joinAllStudyRooms() {
        // studyList가 최신 상태임을 가정
        Log.d(TAG, "joinAllStudyRooms: $studyList")
        for (study in studyList) {
            val data = JSONObject().apply {
                put("studyId", study.id)
                put("userId", USER_ID)
            }
            socket.emit("joinRoom", data)
        }
    }

    // 새 메시지 수신 시 해당 Study 객체의 마지막 메시지 업데이트
    private val onNewMessage = Emitter.Listener { args ->
        lifecycleScope.launch {
            if (args.isNotEmpty() && args[0] != null) {
                val messageJson = args[0].toString()
                val newMessage = gson.fromJson(messageJson, Message::class.java)
                withContext(Dispatchers.Main) {
                    // 새로운 메시지가 도착하면 해당 채팅방(Study)을 찾아 업데이트합니다.
                    val index = studyList.indexOfFirst { it.id == newMessage.studyId }
                    if (index != -1) {
                        val study = studyList[index]
                        study.lastMessage = newMessage.message
                        study.lastMessageCreatedAt = newMessage.createdAt

                        // unreadCount 증가: 기존 unreadCount가 null이면 0으로 처리 후 +1
                        study.unreadCount = (study.unreadCount ?: 0) + 1

                        // 채팅방 목록에서 해당 Study를 제거하고 맨 위에 추가 (최신 채팅방이 위로 오도록)
                        studyList.removeAt(index)
                        studyList.add(0, study)
                        studyAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private val onError = Emitter.Listener { args ->
        lifecycleScope.launch {
            Log.d(TAG, "onError: $args")
        }
    }

    override fun onResume() {
        fetchJoinedStudies()
        super.onResume()
    }

    override fun onDestroyView() {
        socket.disconnect()
        socket.off(Socket.EVENT_CONNECT, onConnect)
        socket.off("newMessage", onNewMessage)
        socket.off("error", onError)
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
}
