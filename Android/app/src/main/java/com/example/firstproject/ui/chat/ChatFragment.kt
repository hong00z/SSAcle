package com.example.firstproject.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firstproject.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var _binding : FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.apply {
            chatComposeView.setContent {

                // ChatScreen()
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}