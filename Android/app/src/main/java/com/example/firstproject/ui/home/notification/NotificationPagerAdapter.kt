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
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // 2개의 페이지 (내 신청 현황, 내 수신함)

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            // 내 신청 현황
            0 -> RequestListFragment()
            // 내 수신함
            1 -> InboxListFragment()

            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
