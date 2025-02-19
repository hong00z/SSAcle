package com.example.firstproject.ui.live

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentLiveMainBinding

class LiveMainFragment : Fragment() {

    private var _binding: FragmentLiveMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveMainBinding.inflate(inflater, container, false)

        binding.roomListComposeView.setContent {
            val xmlNavController = findNavController()

            LiveMainScreen(
                onNavigateToVideo = { studyId, studyName ->
                    val bundle = bundleOf("studyId" to studyId, "studyName" to studyName)
                    xmlNavController.navigate(R.id.action_liveMainFragment_to_videoFragment, bundle)
                }
            )
        }


        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        val studyList = listOf<StudyList>()
//        val studyList = listOf(
//            StudyList("내가 방장인 스터디", "백엔드", listOf("금", "토", "일"), true),
//            StudyList("프론트엔드 심화", "프론트엔드", listOf("월", "수"), false)
//        )
//        binding.apply {
////            if (studyList.isEmpty()) {
////                tvNoStudy.visibility = View.VISIBLE
////                recyclerView.visibility = View.GONE
////            } else {
////                tvNoStudy.visibility = View.GONE
////                recyclerView.visibility = View.VISIBLE
////                recyclerView.layoutManager = LinearLayoutManager(requireContext())
////                recyclerView.adapter = StudyAdapter(studyList)
////            }
//
//
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
