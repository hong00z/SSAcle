package com.example.firstproject.ui.live

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.MyApplication.Companion.webRtcClientConnection
import com.example.firstproject.databinding.ItemLiveMemberBinding
import com.example.firstproject.dto.LiveMember
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.VideoTrack

class LiveMemberAdapter(
    private val members: List<LiveMember>,
    private val eglBaseContext: EglBase.Context
) : RecyclerView.Adapter<LiveMemberAdapter.LiveMemberViewHolder>() {


    companion object {
        const val TAG = "LiveMemberAdapter_TAG"
    }

    inner class LiveMemberViewHolder(val binding: ItemLiveMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMemberViewHolder {
        val binding = ItemLiveMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LiveMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LiveMemberViewHolder, position: Int) {
        val member = members[position]
        holder.binding.tvUsername.text = member.nickname

        if (member.isMe) {
            with(holder.binding.videoView) {
                init(eglBaseContext, null)
                setMirror(true)
                holder.binding.ivAvatar.visibility = View.GONE
                visibility = View.VISIBLE
            }
            val localVideoTrack = webRtcClientConnection.localVideoTrack
            localVideoTrack?.addSink(holder.binding.videoView)
        } else {
            // videoTrack 처리: SurfaceViewRenderer에 연결
            member.videoConsumer?.let { consumer ->
                with(holder.binding.videoView) {
                    // 이미 초기화되지 않았다면 init() 호출
                    if (tag == null) {
                        init(eglBaseContext, null)
                        tag = "initialized"  // 초기화 완료 후 태그 설정
                    }
                    setMirror(false)
                    val videoTrack = consumer.track as VideoTrack
                    videoTrack.addSink(this)
                    holder.binding.ivAvatar.visibility = View.GONE
                    visibility = View.VISIBLE
                }
            } ?: run {
                holder.binding.videoView.visibility = View.GONE
            }
            member.audioConsumer?.let { consumer ->
                val audioTrack = consumer.track as AudioTrack
                audioTrack.setEnabled(true)
            }
        }

    }

    override fun getItemCount(): Int = members.size
}