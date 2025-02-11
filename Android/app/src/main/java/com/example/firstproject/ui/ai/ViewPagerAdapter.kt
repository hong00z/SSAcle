package com.example.firstproject.ui.ai

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        // 텍스트 및 버튼 설정
        holder.binding.titleTextView.text = item.title
        holder.binding.descriptionTextView.text = item.description
        holder.binding.feedbackButton.text = item.buttonText

        // 이미지 설정
        holder.binding.cardImageView.setImageResource(item.imageRes)

        val context = holder.itemView.context

        // 배경 컬러 설정
        val backgroundColor = when (position) {
            0 -> ContextCompat.getColor(context, R.color.card_white)
            1 -> ContextCompat.getColor(context, R.color.card_eye)
            2 -> ContextCompat.getColor(context, R.color.card_face)
            else -> ContextCompat.getColor(context, R.color.card_white) // 기본값 지정
        }
        holder.binding.cardView.setCardBackgroundColor(backgroundColor)

        // 텍스트 컬러 설정
        val textColor = when (position) {
            0 -> ContextCompat.getColor(context, R.color.black)
            else -> ContextCompat.getColor(context, R.color.white)
        }
        holder.binding.descriptionTextView.setTextColor(textColor)

        // 버튼 컬러 설정
        val buttonColor = when (position) {
            0 -> ContextCompat.getColor(context, R.color.myself_button)
            1 -> ContextCompat.getColor(context, R.color.eye_button)
            2 -> ContextCompat.getColor(context, R.color.face_button)
            else -> ContextCompat.getColor(context, R.color.chart_blue)
        }
        holder.binding.feedbackButton.setBackgroundColor(buttonColor)

        // 0번 카드뷰에만 특정 margin 적용 (이미지, 텍스트, 버튼에 적용)
        if (position == 0) {
            // 원하는 dp 값을 설정 (예시: 이미지 16dp, 텍스트 8dp, 버튼 12dp)
            val imageMargin = dpToPx(context, 72)
            val textMargin = dpToPx(context, 80)
            val buttonMargin = dpToPx(context, 18)

            // 이미지 뷰 margin 적용
            val imageLayoutParams = holder.binding.cardImageView.layoutParams as ViewGroup.MarginLayoutParams
            imageLayoutParams.setMargins(0, imageMargin, 0, imageMargin)
            holder.binding.cardImageView.layoutParams = imageLayoutParams

            // 텍스트 뷰 margin 적용 (타이틀 텍스트에 margin을 주고 싶다면)
            val titleLayoutParams = holder.binding.descriptionTextView.layoutParams as ViewGroup.MarginLayoutParams
            titleLayoutParams.setMargins(0, 0, 0, 0)
            holder.binding.descriptionTextView.layoutParams = titleLayoutParams

            // 버튼 margin 적용
            val buttonLayoutParams = holder.binding.feedbackButton.layoutParams as ViewGroup.MarginLayoutParams
            buttonLayoutParams.setMargins(0, buttonMargin, 0, buttonMargin)
            holder.binding.feedbackButton.layoutParams = buttonLayoutParams
        }

        // 버튼 클릭 시 프래그먼트 전환
        holder.binding.feedbackButton.setOnClickListener {
            val fragment = when (item.title) {
                "자소서 피드백" -> AiFeedbackFragment()
                "시선 피드백" -> EyeFragment()
                "표정 피드백" -> FaceExpressionFragment()
                else -> null
            }

            fragment?.let {
                val fragmentManager =
                    (holder.itemView.context as? AppCompatActivity)?.supportFragmentManager

                fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, it)
                    ?.addToBackStack(null)
                    ?.commit()
            } ?: run {
                Toast.makeText(holder.itemView.context, "오류 발생!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // dp 값을 픽셀(px)로 변환하는 헬퍼 함수
    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
