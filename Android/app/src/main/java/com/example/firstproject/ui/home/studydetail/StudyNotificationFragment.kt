package com.example.firstproject.ui.home.studydetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentStudyNotificationBinding
import com.example.firstproject.ui.common.CommonTopBar
import com.example.firstproject.ui.home.detail.DetailNotificationPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator


class StudyNotificationFragment : Fragment() {
    private var _binding: FragmentStudyNotificationBinding? = null
    private val binding get() = _binding!!

    lateinit var studyId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyNotificationBinding.inflate(inflater, container, false)
        studyId = arguments?.getString("studyId")!!


        binding.apply {
            studyTopbarComposeView.setContent {

                CommonTopBar(
                    title = "스터디 수신함",
                    onBackPress = {
                        findNavController().navigate(R.id.action_studyNotificationFragment_to_homeFragment)
                    }
                )
            }

            studyViewPager.adapter = DetailNotificationPagerAdapter(requireActivity(), studyId)

            TabLayoutMediator(studyTabLayout, studyViewPager) { tab, position ->
                tab.text = if (position == 0) "초대 현황" else "가입 요청"

            }.attach()
        }



        return binding.root


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}