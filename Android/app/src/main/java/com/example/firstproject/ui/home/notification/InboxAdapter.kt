package com.example.firstproject.ui.home.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.databinding.ItemInboxBinding

class InboxAdapter(private var dataList: List<InboxItem>) :
    RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemInboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InboxItem) {
            binding.ivProfile.setImageResource(item.profileImage)
            binding.tvTag.text = item.tag
            binding.tvUsername.text = item.username
            binding.tvRequestMessage.text = item.message

            binding.tvAccept.setOnClickListener {
                // 수락 버튼 클릭 이벤트
            }

            binding.tvReject.setOnClickListener {
                // 거절 버튼 클릭 이벤트
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<InboxItem>) {
        dataList = newData
        notifyDataSetChanged()
    }
}