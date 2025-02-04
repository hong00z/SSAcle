package com.example.firstproject.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.databinding.ItemMessageReceivedBinding
import com.example.firstproject.databinding.ItemMessageSentBinding

class MessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMine) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size
    fun addMessages(newMessages: List<ChatMessage>) {
        messages.addAll(0, newMessages) // 상단에 추가
        notifyItemRangeInserted(0, newMessages.size) // RecyclerView에 반영
    }
    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.messageContent.text = message.content
            binding.messageTime.text = message.time


            if (message.imageResId != null) {
                binding.messageProfile.visibility = View.VISIBLE
                binding.messageProfile.setImageResource(message.imageResId)
            } else {
                binding.messageProfile.visibility = View.GONE
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.senderName.text = message.sender
            binding.messageContent.text = message.content
            binding.messageTime.text = message.time

            if (message.imageResId != null) {
                binding.messageProfile.visibility = View.VISIBLE
                binding.messageProfile.setImageResource(message.imageResId)
            } else {
                binding.messageProfile.visibility = View.GONE
            }
        }
    }
}
