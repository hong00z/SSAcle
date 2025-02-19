package com.example.firstproject.ui.home.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firstproject.databinding.FragmentFirstBinding

class FirstFragment() : Fragment()  {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var studyId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        studyId = arguments?.getString("studyId") ?: ""

        binding.firstComposeView.setContent {
            InvitingListScreen(studyId = studyId)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}