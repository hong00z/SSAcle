package com.example.firstproject.ui.ai

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firstproject.databinding.FragmentAiBinding

class AiFragment : Fragment() {
    private var _binding : FragmentAiBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)

        binding.apply {

        }
        return binding.root
    }

    companion object {
        fun newInstance() : AiFragment {
            return AiFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}