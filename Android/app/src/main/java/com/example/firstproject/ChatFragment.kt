package com.example.firstproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firstproject.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private var binding : FragmentChatBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    companion object {
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}