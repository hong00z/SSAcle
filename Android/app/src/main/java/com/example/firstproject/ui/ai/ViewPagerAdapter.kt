package com.example.firstproject.ui.ai

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R
import com.example.firstproject.databinding.SliderItemBinding
import com.example.firstproject.ui.ai.eye.EyeFragment
import com.example.firstproject.ui.ai.face.FaceExpressionFragment

class ViewPagerAdapter(private val items: List<CardItem>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    // 뷰 홀더 정의
    inner class ViewHolder(val binding: SliderItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SliderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTextView.text = item.title
        holder.binding.descriptionTextView.text = item.description
        holder.binding.feedbackButton.text = item.buttonText

        holder.binding.feedbackButton.setOnClickListener {
            val fragment = when (item.title) {
                "자소서 피드백" -> AiFeedbackFragment()
                "영상 피드백" -> FaceExpressionFragment()
                "인터뷰 피드백" -> EyeFragment()
                else -> null
            }

            fragment?.let {
                val fragmentManager =
                    (holder.itemView.context as? AppCompatActivity)?.supportFragmentManager

                fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, it) // 프래그먼트를 변경
//                    ?.addToBackStack(null) // 뒤로 가기 가능하도록 설정
                    ?.commit()
            } ?: run {
                Toast.makeText(holder.itemView.context, "오류 발생!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
