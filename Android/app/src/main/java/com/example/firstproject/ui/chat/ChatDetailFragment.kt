package com.example.firstproject.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentChatDetailBinding

class ChatDetailFragment : Fragment() {
    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ChatDetailFragmentArgs by navArgs()

    private lateinit var adapter: MessageAdapter
    private var allMessages = mutableListOf<ChatMessage>() // 전체 메시지 리스트
    private var visibleMessages = mutableListOf<ChatMessage>() // 현재 보이는 메시지 리스트
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 8 // 한 번에 불러올 메시지 개수

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🟢 SafeArgs로 받은 데이터 로그 출력 (디버깅 확인)
        Log.d("ChatDetailFragment", "Received roomName: ${args.roomName}")
        Log.d("ChatDetailFragment", "Received messages size: ${args.messages.size}")

        // 🟢 Toolbar 설정 (roomName 적용)
        val toolbar = binding.chatDetailToolbar
        toolbar.title = args.roomName
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 🟢 SafeArgs로 전달된 messages 데이터를 MutableList로 변환
        allMessages = args.messages.toList().toMutableList()

        // 🟢 RecyclerView 설정 (여기서 먼저 adapter 설정)
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        adapter = MessageAdapter(visibleMessages)
        binding.messagesRecycler.layoutManager = layoutManager
        binding.messagesRecycler.adapter = adapter

        // 🟢 첫 8개 메시지 로드 후 UI 업데이트
        loadMoreMessages(firstLoad = true)

        // 🟢 스크롤 리스너 추가 (페이징 처리)
        binding.messagesRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 🟢 최상단으로 스크롤 시 새로운 메시지 로드
                if (!recyclerView.canScrollVertically(-1) && !isLoading) {
                    loadMoreMessages()
                }
            }
        })
    }

    // 지금은 8개씩 로딩되게 해놨습니다.
    private fun loadMoreMessages(firstLoad: Boolean = false) {
        if (isLoading) return
        isLoading = true

        binding.loading.visibility = View.VISIBLE

        val nextPage = currentPage + 1
        val startIndex = currentPage * pageSize
        val endIndex = (nextPage * pageSize).coerceAtMost(allMessages.size)

        if (startIndex < allMessages.size) {
            val newMessages = allMessages.subList(startIndex, endIndex)
            visibleMessages.addAll(0, newMessages) // **새로운 메시지 추가**

            if (firstLoad) {
                adapter.notifyDataSetChanged() // **첫 로딩 시 전체 갱신**
            } else {
                adapter.notifyItemRangeInserted(0, newMessages.size) // **추가된 부분만 갱신**
            }

            currentPage = nextPage
        }

        binding.loading.visibility = View.GONE
        isLoading = false
    }
}
