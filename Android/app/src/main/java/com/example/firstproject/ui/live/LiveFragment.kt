package com.example.firstproject.ui.live

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firstproject.databinding.FragmentLiveBinding

class LiveFragment : Fragment() {
    private var _binding : FragmentLiveBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)

        binding.apply {

        }
        return binding.root
    }

    companion object {
        fun newInstance() : LiveFragment {
            return LiveFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}