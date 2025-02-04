package com.example.firstproject.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentChatDetailBinding

class ChatDetailFragment : Fragment() {
    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    val args: ChatDetailFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)

        // 툴바 설정
        val toolbar = binding.chatDetailToolbar
        toolbar.title = args.roomName // SafeArgs로 전달된 채팅방 이름 설정
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24) // 뒤로 가기 아이콘 설정
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // RecyclerView 설정
        val messages = args.messages.toList()
        binding.messagesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MessageAdapter(messages)
        }

        return binding.root
    }
}
