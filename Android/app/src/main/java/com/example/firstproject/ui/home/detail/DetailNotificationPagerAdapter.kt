package com.example.firstproject.ui.home.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailNotificationPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val studyId: String
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstFragment().apply {
                arguments = Bundle().apply {
                    putString("studyId", studyId)
                }
            }
            1 -> SecondFragment().apply {
                arguments = Bundle().apply {
                    putString("studyId", studyId)
                }
            }
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

}