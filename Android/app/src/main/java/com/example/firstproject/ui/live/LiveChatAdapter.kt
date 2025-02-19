package com.example.firstproject.ui.live

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.databinding.ItemLiveChatBinding
import com.example.firstproject.dto.LiveChatMessage

class LiveChatAdapter(private val chatList: List<LiveChatMessage>) :
    RecyclerView.Adapter<LiveChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemLiveChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemLiveChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        if (chat.isMe) {
            holder.binding.tvName.gravity = android.view.Gravity.END
            holder.binding.tvChat.gravity = android.view.Gravity.END
        }
        else {
            holder.binding.tvName.gravity = android.view.Gravity.START
            holder.binding.tvChat.gravity = android.view.Gravity.START
        }
        holder.binding.tvName.text = chat.nickname
        holder.binding.tvChat.text = chat.message
    }

    override fun getItemCount(): Int = chatList.size
}

