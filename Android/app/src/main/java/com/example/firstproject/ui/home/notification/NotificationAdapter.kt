package com.example.firstproject.ui.home.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.databinding.ItemInboxBinding
import com.example.firstproject.databinding.ItemNotificationBinding
class NotificationAdapter(
    private var dataList: List<NotificationItem>,
    private val isInbox: Boolean // true: 내 수신함, false: 내 신청 현황
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_REQUEST = 0
        private const val TYPE_INBOX = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isInbox) TYPE_INBOX else TYPE_REQUEST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_REQUEST) {
            val binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            RequestViewHolder(binding)
        } else {
            val binding = ItemInboxBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            InboxViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        when (holder) {
            is RequestViewHolder -> holder.bind(item)
            is InboxViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = dataList.size

    class RequestViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationItem) {
            binding.tvCategory.text = item.category
            binding.tvStudyName.text = item.studyName
            binding.tvDescription.text = item.description
            binding.tvCancel.setOnClickListener {
                // 취소하기 버튼 클릭 이벤트
            }
        }
    }

    class InboxViewHolder(private val binding: ItemInboxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationItem) {
            binding.tvTag.text = item.category
            binding.tvUsername.text = item.studyName
            binding.tvRequestMessage.text = item.description
            binding.tvAccept.setOnClickListener {
                // 수락 버튼 클릭 이벤트
            }
            binding.tvReject.setOnClickListener {
                // 거절 버튼 클릭 이벤트
            }
        }
    }
}
