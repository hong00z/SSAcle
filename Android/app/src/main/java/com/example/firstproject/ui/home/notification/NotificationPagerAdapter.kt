package com.example.firstproject.ui.home.notification

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class NotificationPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2 // 2개의 페이지 (내 신청 현황, 내 수신함)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RequestListFragment() // 내 신청 현황
            1 -> InboxListFragment()   // 내 수신함
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
