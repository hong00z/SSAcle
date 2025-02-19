package com.example.firstproject.ui.home.notification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstproject.data.model.dto.response.MyAppliedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.MyInvitedStudyListDtoItem
import kotlinx.coroutines.flow.StateFlow

class NotificationPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val inviteList: List<MyAppliedStudyListDtoItem>,
    private val studyList: List<MyInvitedStudyListDtoItem>
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // 2개의 페이지 (내 신청 현황, 내 수신함)

    override fun createFragment(position: Int): Fragment {

        Log.d("어뎁터", "1번 : ${inviteList}")
        Log.d("어뎁터", "2번 : ${studyList}")
        return when (position) {
            // 내 신청 현황
            0 -> RequestListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("inviteList", ArrayList(inviteList))
                }
            }
            // 내 수신함
            1 -> InboxListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("studyList", ArrayList(studyList))
                }
            }

            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
