package com.example.firstproject.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.MainActivity
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        // 리로딩 할때
        val swipeRefreshLayout = binding.swipeRefreshLayout

        swipeRefreshLayout.setOnRefreshListener {
            refreshChatList()
            swipeRefreshLayout.isRefreshing = false
        }


        // 샘플
        val chatRooms = listOf(
            ChatRoom(
                roomName = "이거슨 알고리즘 스터디여",
                messages = listOf(
                    ChatMessage("god of 알고리즘", "이번주 알고리즘 문제는 이거 어떠신가요?", "오전 10:21", false, R.drawable.default_profile),
                    ChatMessage("나", "네! 좋아요. 그렇게 합시다.", "오전 11:01", true),
                    ChatMessage("알고리즘 갓커", "안녕하세요! 열심히 참가하겠습니다 ㅎㅎ", "오후 3:00", false, R.drawable.default_profile),
                    ChatMessage("god of 알고리즘", "환영합니다~", "오후 3:00", false,R.drawable.default_profile),
                    ChatMessage("나", "알고리즘 갓커님 안녕하세요!", "오후 3:01", true),
                    ChatMessage("god of 알고리즘", "이번주 알고리즘 문제는 이거 어떠신가요?", "오전 10:21", false, R.drawable.default_profile),
                    ChatMessage("나", "네! 좋아요. 그렇게 합시다.", "오전 11:01", true),
                    ChatMessage("알고리즘 갓커", "안녕하세요! 열심히 참가하겠습니다 ㅎㅎ", "오후 3:00", false, R.drawable.default_profile),
                    ChatMessage("god of 알고리즘", "환영합니다~", "오후 3:00", false,R.drawable.default_profile),
                    ChatMessage("나", "알고리즘 갓커님 안녕하세요!", "오후 3:01", true),
                    ChatMessage("god of 알고리즘", "이번주 알고리즘 문제는 이거 어떠신가요?", "오전 10:21", false, R.drawable.default_profile),
                    ChatMessage("나", "네! 좋아요. 그렇게 합시다.", "오전 11:01", true),
                    ChatMessage("알고리즘 갓커", "안녕하세요! 열심히 참가하겠습니다 ㅎㅎ", "오후 3:00", false, R.drawable.default_profile),
                    ChatMessage("god of 알고리즘", "환영합니다~", "오후 3:00", false,R.drawable.default_profile),
                    ChatMessage("나", "알고리즘 갓커님 안녕하세요!", "오후 3:01", true)
                )
            ),
            ChatRoom(
                roomName = "css아니고 cs",
                messages = listOf(
                    ChatMessage("스터디장", "이번 주 주제는 '보안'입니다!", "오전 10:54",false)
                )
            ),
            ChatRoom(
                roomName = "algo스터디",
                messages = listOf(
                    ChatMessage("팀원A", "ㅋㅋㅋㅋㅋ 그렇게 됐네요 ㅠ", "오전 10:54", false)
                )
            )
        )

        // item..!!
        val chatItems = chatRooms.map { chatRoom ->
            ChatItem(
                title = chatRoom.roomName,
                lastMessage = chatRoom.messages.lastOrNull()?.content ?: "No messages",
                time = chatRoom.messages.lastOrNull()?.time ?: "Unknown time"
            )
        }

        // RecyclerView 설정
        binding.chatListRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatAdapter(chatItems) { chatItem ->
                val chatRoom = chatRooms.find { it.roomName == chatItem.title } // 선택된 채팅방 찾기
                chatRoom?.let {
                    val action = ChatFragmentDirections.actionChatFragmentToChatDetailFragment(
                        roomName = it.roomName,
                        messages = it.messages.toTypedArray() // 메시지 목록을 배열로 전달
                    )
                    findNavController().navigate(action)
                }
            }
        }


        // main으로 가게
        binding.apply {
            backButton.setOnClickListener {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        return binding.root
    }

    private fun refreshChatList() {
        // 기능은 안만들었습니다.
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
}
