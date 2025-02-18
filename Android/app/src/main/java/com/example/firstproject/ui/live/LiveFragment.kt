package com.example.firstproject.ui.live

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.firstproject.MyApplication
import com.example.firstproject.MyApplication.Companion.requiredPermissions
import com.example.firstproject.MyApplication.Companion.webRtcClientConnection
import com.example.firstproject.client.WebRtcClientConnection
import com.example.firstproject.databinding.FragmentLiveBinding
import com.example.firstproject.utils.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class LiveFragment : Fragment() {

    companion object {
        const val TAG = "LiveFragment_TAG"
    }

    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!

    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    // 채팅 및 방 관련 상태
    private var rooms = mutableListOf<String>()
    private var isMuted = false
    private var isVideoOff = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)

        // 권한 체크 (카메라, 마이크 등)
        val checker = PermissionChecker(this)
        if (!checker.checkPermission(requireContext(), requiredPermissions)) {
            checker.setOnGrantedListener {
                // 권한이 허용되면 WebRTC 초기화 및 signaling 서버 연결
                initWebRTC()
                initUI()
            }
            checker.requestPermissionLauncher.launch(requiredPermissions)
        } else {
            // 이미 권한이 있는 경우
            initWebRTC()
            initUI()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun initWebRTC() {
        webRtcClientConnection.init(requireContext())

        binding.localVideoView.init(webRtcClientConnection.eglBase.eglBaseContext, null)
        binding.localVideoView.setMirror(true)

        localVideoTrack = webRtcClientConnection.localVideoTrack
        localAudioTrack = webRtcClientConnection.localAudioTrack

        localVideoTrack?.addSink(binding.localVideoView)
        Log.d(TAG, "initWebRTC: ${localAudioTrack?.id()}")
        Log.d(TAG, "initWebRTC: ${localVideoTrack?.id()}")

        // 원격 소비(consume)가 완료되면 displayRemoteVideo() 호출
        webRtcClientConnection.onRemoteVideo = { videoTrack, consumer ->
            // UI 업데이트는 메인 스레드에서 실행
            lifecycleScope.launch(Dispatchers.Main) {
                displayRemoteVideo(videoTrack, consumer.producerId)
            }
        }
        webRtcClientConnection.onRemoteAudio = { audioTrack, consumer ->
            audioTrack.let {
                it.setEnabled(true)
                Log.d(TAG, "AudioTrack enabled: ${consumer.producerId}")
            }
        }

        // 원격 프로듀서 종료 이벤트 처리 콜백 설정
        webRtcClientConnection.onPeerClosed = { producerId ->
            lifecycleScope.launch(Dispatchers.Main) {
                removeRemoteVideo(producerId)
            }
        }
    }

    /**
     * UI 초기화: 버튼 클릭 이벤트, 방 목록 갱신, 채팅 전송 등
     */
    private fun initUI() {
        // 예: join 버튼 클릭 시 입력한 방 이름으로 joinRoom() 호출
        binding.joinRoomBtn.setOnClickListener {
            val roomId = binding.roomInput.text.toString().trim()
            Log.d(TAG, "click joinRoom: $roomId")

            if (roomId.isNotEmpty()) {
                webRtcClientConnection.joinRoom(roomId)
            }
        }
        binding.sendChatBtn.setOnClickListener {
            val message = binding.chatInput.text.toString().trim()
            if (message.isNotEmpty()) {
                Log.d(TAG, "click sendMessage: $message")
                lifecycleScope.launch {
                    val result = webRtcClientConnection.sendChatMessage(message)
                    if (result) {
                        displayChatMessage("나", message)
                    }
                }
            }
        }
        binding.muteBtn.setOnClickListener { toggleMute() }
        binding.videoBtn.setOnClickListener { toggleVideo() }
        binding.leaveBtn.setOnClickListener { leaveRoom() }
        binding.refreshRoomsBtn.setOnClickListener { getRoomList() }
    }


    private fun getRoomList() {
        lifecycleScope.launch(Dispatchers.Main) {
            rooms.clear()
            rooms.addAll(webRtcClientConnection.getRoomList())
            binding.roomListContainer.removeAllViews()
            for (roomId in rooms) {
                val tv = android.widget.TextView(requireContext())
                tv.text = roomId
                tv.setPadding(16, 16, 16, 16)
                tv.setOnClickListener {
                    webRtcClientConnection.joinRoom(roomId)
                }
                binding.roomListContainer.addView(tv)
            }
        }
    }

    // 원격 비디오를 UI에 추가하는 함수. 여기서 producerId를 tag로 지정
    private fun displayRemoteVideo(videoTrack: VideoTrack, producerId: String) {
        val remoteRenderer = SurfaceViewRenderer(requireContext())
        remoteRenderer.init(webRtcClientConnection.eglBase.eglBaseContext, null)
        remoteRenderer.setMirror(false)

        remoteRenderer.tag = producerId
        binding.remoteVideoContainer.addView(remoteRenderer)
        videoTrack.addSink(remoteRenderer)
        Log.d(TAG, "displayRemoteVideo: Remote video renderer added for producerId=$producerId")
    }

    // 원격 비디오 뷰를 제거하는 함수. tag가 producerId와 일치하는 뷰를 제거
    private fun removeRemoteVideo(producerId: String) {
        val container = binding.remoteVideoContainer
        for (i in container.childCount - 1 downTo 0) {
            val child = container.getChildAt(i)
            if (child.tag == producerId) {
                if (child is SurfaceViewRenderer) {
                    child.release()
                }
                container.removeViewAt(i)
                Log.d(TAG, "removeRemoteVideo: Removed remote renderer for producerId=$producerId")
            }
        }
    }

    private fun displayChatMessage(peerId: String, message: String) {
        binding.chatMessages.append("[$peerId] $message\n")
    }

    private fun leaveRoom() {
        webRtcClientConnection.leaveRoom()
        webRtcClientConnection.close()
        binding.localVideoView.release()
        binding.remoteVideoContainer.removeAllViews()
        binding.chatMessages.text = ""
    }

    private fun toggleMute() {
        localAudioTrack?.let {
            val newState = !it.enabled()
            it.setEnabled(newState)
            isMuted = newState
            binding.muteBtn.text = if (isMuted) "음소거 해제" else "음소거"
        }
    }

    private fun toggleVideo() {
        localVideoTrack?.let {
            val newState = !it.enabled()
            it.setEnabled(newState)
            isVideoOff = newState
            binding.videoBtn.text = if (isVideoOff) "비디오 켜기" else "비디오 끄기"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
