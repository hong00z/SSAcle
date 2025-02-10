package com.example.firstproject.ui.matching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.firstproject.databinding.FragmentRegisterStudyBinding

class StudyRegisterFragment : Fragment() {
    private var _binding: FragmentRegisterStudyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterStudyBinding.inflate(inflater, container, false)


        binding.registerComposeView.setContent {
            val xmlNavController = findNavController()

            RegisterStudyScreen(xmlNavController)

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}