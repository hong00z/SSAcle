package com.example.firstproject.ui.live

import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R
import com.example.firstproject.databinding.ItemStudyBinding

class StudyAdapter(private val studyList: List<StudyList>) :
    RecyclerView.Adapter<StudyAdapter.StudyViewHolder>() {
    class StudyViewHolder(val binding: ItemStudyBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        val binding = ItemStudyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        val study = studyList[position]
        with(holder.binding) {
            tvTitle.text = study.title
            tvSubject.text = study.subject

            ivLeader.visibility = if (study.leader) View.VISIBLE else View.GONE

            // 요일 동적 추가 (기존 것 제거 후 다시 추가)
            layoutDates.removeAllViews()
            layoutDates.gravity = Gravity.END
            for (day in study.date) {
                val textView = TextView(layoutDates.context).apply {
                    text = day
                    setTextColor(Color.WHITE) // 글자 색상: 흰색
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.day_background) // 원형 배경 적용

                    // 크기 설정 (완전한 원형 유지)
                    val params = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                        marginEnd = 8.dpToPx()  // 각 요일 사이 마진 추가
                    }
                    layoutParams = params
                }
                layoutDates.addView(textView)
            }
        }
    }

    // dp를 px로 변환하는 확장 함수 추가
    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }


    override fun getItemCount(): Int {
        return studyList.size
    }

}