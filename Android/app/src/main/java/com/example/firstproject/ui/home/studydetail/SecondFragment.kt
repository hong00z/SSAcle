package com.example.firstproject.ui.home.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firstproject.databinding.FragmentFirstBinding
import com.example.firstproject.databinding.FragmentSecondBinding

class SecondFragment : Fragment()  {
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var studyId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        studyId = arguments?.getString("studyId") ?: ""

        binding.secondComposeView.setContent {
            JoinRequestListScreen(studyId = studyId)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}