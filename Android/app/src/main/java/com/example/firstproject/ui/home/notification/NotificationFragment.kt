package com.example.firstproject.ui.home.notification

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.MyAppliedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.MyInvitedStudyListDtoItem
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.databinding.FragmentNotificationBinding
import com.example.firstproject.ui.common.CommonTopBar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    val repository = MainRepository

    // ✅ 리스트를 StateFlow로 관리하여 값 변경 감지 가능
    private val _inviteList = MutableStateFlow<List<MyAppliedStudyListDtoItem>>(emptyList())
    val inviteList: StateFlow<List<MyAppliedStudyListDtoItem>> get() = _inviteList

    private val _studyList = MutableStateFlow<List<MyInvitedStudyListDtoItem>>(emptyList())
    val studyList: StateFlow<List<MyInvitedStudyListDtoItem>> get() = _studyList

    private val notificationViewModel: NotificationViewModel by viewModels()

    // ✅ 리스트를 StateFlow로 관리하여 값 변경 감지 가능
    private val _inviteList = MutableStateFlow<List<MyAppliedStudyListDtoItem>>(emptyList())
    val inviteList: StateFlow<List<MyAppliedStudyListDtoItem>> get() = _inviteList

    private val _studyList = MutableStateFlow<List<MyInvitedStudyListDtoItem>>(emptyList())
    val studyList: StateFlow<List<MyInvitedStudyListDtoItem>> get() = _studyList

    private val notificationViewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)



        binding.apply {
            topbarComposeView.setContent {

                CommonTopBar(
                    title = "나의 알림함",
                    onBackPress = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                )
            }

            viewPager.adapter = NotificationPagerAdapter(requireActivity(), inviteList.value, studyList.value)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = if (position == 0) "내 신청 현황" else "내 수신함"
            }.attach()
        }

        return binding.root
    }

//    // viewModel 통신
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                notificationViewModel.myAppliedResult.collect { result ->
//                    when (result) {
//                        is RequestResult.Success -> {
//                            _inviteList.value = result.data
//                            Log.d("뷰모델 실행", "1번 : ${inviteList.value}")
//                        }
//
//                        is RequestResult.Failure -> {
//                            Log.e("Mypage", "오류 발생: ${result.exception?.message}")
//                        }
//
//                        else -> Unit
//                    }
//                }
//
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                notificationViewModel.inviteResult.collect { result ->
//                    when (result) {
//                        is RequestResult.Success -> {
//                            _studyList.value = result.data
//
//                        }
//
//                        is RequestResult.Failure -> {
//                            Log.e("Mypage", "오류 발생: ${result.exception?.message}")
//                        }
//
//                        else -> Unit
//                    }
//                }
//
//
//            }
//        }
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
