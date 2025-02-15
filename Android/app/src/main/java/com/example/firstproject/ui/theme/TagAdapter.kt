package com.example.firstproject.ui.theme

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R
import com.example.firstproject.databinding.ItemTagBinding

class TagAdapter(
    private val context: Context, private val tagList: List<String>,
    private val onSelectionChanged: (selectedCount: Int, showWarning: Boolean) -> Unit
) :
    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    // 선택된 태그 저장 변수
    private val selectedTags = mutableSetOf<String>()

    // 카테고리별 배경(선택 시 채워질 색) 매핑
    private val categoryColors = mapOf(
        "웹 프론트" to ContextCompat.getColor(context, R.color.frontend_stack_tag),
        "백엔드" to ContextCompat.getColor(context, R.color.backend_stack_tag),
        "모바일" to ContextCompat.getColor(context, R.color.mobile_stack_tag),
        "인공지능" to ContextCompat.getColor(context, R.color.ai_stack_tag),
        "빅데이터" to ContextCompat.getColor(context, R.color.data_stack_tag),
        "임베디드" to ContextCompat.getColor(context, R.color.embaded_stack_tag),
        "인프라" to ContextCompat.getColor(context, R.color.infra_stack_tag),
        "CS 이론" to ContextCompat.getColor(context, R.color.cs_stack_tag),
        "알고리즘" to ContextCompat.getColor(context, R.color.algo_stack_tag),
        "게임" to ContextCompat.getColor(context, R.color.game_stack_tag),
        "기타" to ContextCompat.getColor(context, R.color.etc_stack_tag)
    )

    // 카테고리별 테두리(border) 색상 매핑
    private val borderColors = mapOf(
        "웹 프론트" to ContextCompat.getColor(context, R.color.frontend_stack_default_border),
        "백엔드" to ContextCompat.getColor(context, R.color.backend_stack_default_border),
        "모바일" to ContextCompat.getColor(context, R.color.mobile_stack_default_border),
        "인공지능" to ContextCompat.getColor(context, R.color.ai_stack_default_border),
        "빅데이터" to ContextCompat.getColor(context, R.color.data_stack_default_border),
        "임베디드" to ContextCompat.getColor(context, R.color.embaded_stack_default_border),
        "인프라" to ContextCompat.getColor(context, R.color.infra_stack_default_border),
        "CS 이론" to ContextCompat.getColor(context, R.color.cs_stack_default_border),
        "알고리즘" to ContextCompat.getColor(context, R.color.algo_stack_default_border),
        "게임" to ContextCompat.getColor(context, R.color.game_stack_default_border),
        "기타" to ContextCompat.getColor(context, R.color.etc_stack_default_border)
    )

    private val defaultBackground = ContextCompat.getColor(context, R.color.white)

    inner class TagViewHolder(private val binding: ItemTagBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: String) {
            binding.txtTag.text = tag

            val bgColor = categoryColors[tag] ?: defaultBackground
            val borderColor =
                borderColors[tag] ?: ContextCompat.getColor(context, android.R.color.black)

            // 선택 여부에 따라 Drawable 생성
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                // tag_background.xml의 cornerRadius와 (이거 삭제하면 안됨)
                cornerRadius = dpToPx(16).toFloat()
                if (selectedTags.contains(tag)) {
                    // 선택 -> categoryColors로
                    setColor(bgColor)
                } else {
                    setColor(defaultBackground)
                }
                // 테두리는 category별 border 색상, stroke 1dp
                setStroke(dpToPx(1), borderColor)
            }

            binding.root.background = drawable
            // 텍스트 : 선택되면 흰색, 아니면 검은색
            binding.txtTag.setTextColor(
                if (selectedTags.contains(tag))
                    ContextCompat.getColor(context, android.R.color.white)
                else
                    ContextCompat.getColor(context, android.R.color.black)
            )

            // 4개
            binding.root.setOnClickListener {
                if (selectedTags.contains(tag)) {
                    selectedTags.remove(tag)
                    onSelectionChanged(selectedTags.size, false)

                } else {
                    if (selectedTags.size < 4) {
                        selectedTags.add(tag)
                        onSelectionChanged(selectedTags.size, false)
                    } else {
                        onSelectionChanged(selectedTags.size, true)
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    // dp -> px
    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tagList[position])
    }

    override fun getItemCount(): Int = tagList.size
}
