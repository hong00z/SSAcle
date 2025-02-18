package com.example.firstproject.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.firstproject.MyApplication.Companion.USER_ID
import com.example.firstproject.R
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.databinding.ItemMessageReceivedBinding
import com.example.firstproject.databinding.ItemMessageSentBinding
import com.example.firstproject.dto.Message
import com.example.firstproject.utils.CommonUtils

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class DateHeader(val date: String) : ChatItem()
}

class MessageAdapter(private val items: MutableList<ChatItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatItem.DateHeader -> TYPE_DATE_HEADER
            is ChatItem.MessageItem -> {
                if (item.message.userId == USER_ID) TYPE_SENT else TYPE_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                // 날짜 헤더 레이아웃 (item_date_header.xml)
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }

            TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentMessageViewHolder(binding)
            }

            TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> {
                val header = items[position] as ChatItem.DateHeader
                holder.bind(header)
            }

            is SentMessageViewHolder -> {
                val messageItem = items[position] as ChatItem.MessageItem
                holder.bind(messageItem.message)
            }

            is ReceivedMessageViewHolder -> {
                val messageItem = items[position] as ChatItem.MessageItem
                holder.bind(messageItem.message)
            }
        }
    }

    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // item_date_header.xml에 TextView id를 dateTextView로 지정했다고 가정
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        fun bind(header: ChatItem.DateHeader) {
            dateTextView.text = header.date
        }
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageContent.text = message.message
            binding.messageTime.text = CommonUtils.formatKoreanTime(message.createdAt)
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.senderName.text = message.nickname
            binding.messageContent.text = message.message
            binding.messageTime.text = CommonUtils.formatKoreanTime(message.createdAt)
            if (message.image != "") {
                binding.messageProfile.load(RemoteDataSource().getImageUrl(message.image))
            }
        }
    }
}
