package com.example.firstproject.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.databinding.ItemChatBinding
import com.example.firstproject.dto.Study
import com.example.firstproject.utils.CommonUtils

class StudyAdapter(
    private val studies: List<Study>,
    private val onChatClick: (Study) -> Unit
) :
    RecyclerView.Adapter<StudyAdapter.StudyViewHolder>() {

    inner class StudyViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(study: Study) {
            binding.chatTitle.text = study.studyName
            binding.lastMessage.text = study.lastMessage ?: ""
            Log.d(TAG, "bind: study.image=${study.image}")
            if (study.image != "") {
                binding.profileImage.load(RemoteDataSource().getImageUrl(study.image))
            }
            binding.chatTime.text = study.lastMessageCreatedAt?.let {
                CommonUtils.formatKoreanTime(it)
            } ?: ""

            if (study.unreadCount != null && study.unreadCount!! > 0) {
                binding.unreadCount.text = "${study.unreadCount}"
                binding.unreadCount.visibility = View.VISIBLE
            } else {
                binding.unreadCount.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onChatClick(study)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        holder.bind(studies[position])
    }

    override fun getItemCount(): Int = studies.size

}