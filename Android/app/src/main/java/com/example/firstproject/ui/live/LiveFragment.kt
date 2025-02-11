package com.example.firstproject.ui.live

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firstproject.MyApplication.Companion.requiredPermissions
import com.example.firstproject.client.RetrofitClient.WEBRTC_URL
import com.example.firstproject.databinding.FragmentLiveBinding
import com.example.firstproject.utils.PermissionChecker
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

class LiveFragment : Fragment() {
    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var socket: Socket
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private lateinit var eglBase: EglBase

    private lateinit var localVideoTrack: VideoTrack
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var videoCapturer: VideoCapturer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)

        val checker = PermissionChecker(this)/* permission check */
        if (!checker.checkPermission(requireContext(), requiredPermissions)) {
            checker.setOnGrantedListener {
                // 권한 획득 성공한 경우
                setupWebRTC()
                connectToSignalingServer()
            }

            checker.requestPermissionLauncher.launch(requiredPermissions)
        } else {
            // 이미 권한이 있는 경우
            setupWebRTC()
            connectToSignalingServer()
        }



        return binding.root
    }

    /**
     * WebRTC 및 SurfaceViewRenderer 설정
     */
    private fun setupWebRTC() {
        eglBase = EglBase.create()

        // WebRTC 라이브러리 초기화
        val options = PeerConnectionFactory.InitializationOptions.builder(requireContext())
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory =
            PeerConnectionFactory.builder().setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory).createPeerConnectionFactory()

        // 비디오 화면 초기화
        binding.localVideoView.init(eglBase.eglBaseContext, null)
        binding.localVideoView.setMirror(true)

        startLocalVideoCapture()
    }

    /**
     * 카메라 비디오 캡쳐 시작
     */
    private fun startLocalVideoCapture() {
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer = createCameraCapturer()
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer.initialize(
            surfaceTextureHelper, requireContext(), videoSource.capturerObserver
        )
        videoCapturer.startCapture(1280, 720, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource)
        localVideoTrack.addSink(binding.localVideoView)

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource)
    }

    /**
     * 카메라 캡처 객체 생성
     */
    private fun createCameraCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(requireContext())
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
                    ?: throw RuntimeException("Camera not found")
            }
        }
        throw RuntimeException("No front camera available")
    }

    /**
     * 시그널링 서버 연결 (Socket.IO 사용)
     */
    private fun connectToSignalingServer() {
        try {
            socket = IO.socket(WEBRTC_URL) // 서버 주소 변경
            socket.connect()

            socket.on("getRouterRtpCapabilities") { args ->
                val data = args[0] as JSONObject
                Log.d("WebRTC", "Router Capabilities: $data")
            }

            socket.on("newProducer") { args ->
                val data = args[0] as JSONObject
                val producerId = data.getString("producerId")
                Log.d("WebRTC", "New producer detected: $producerId")
            }

            socket.emit("getRouterRtpCapabilities", object : Ack {
                override fun call(vararg args: Any?) {
                    val response = args[0] as JSONObject
                    Log.d("WebRTC", "Router Capabilities received: $response")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * PeerConnection 설정
     */
//    private fun createPeerConnection() {
//        val iceServers = listOf(
//            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
//        )
//        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
//
//        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
//            override fun onIceCandidate(candidate: IceCandidate?) {
//                candidate?.let {
//                    val json = JSONObject().apply {
//                        put("sdpMid", it.sdpMid)
//                        put("sdpMLineIndex", it.sdpMLineIndex)
//                        put("candidate", it.sdp)
//                    }
//                    socket.emit("ice-candidate", json)
//                }
//            }
//
//            override fun onAddStream(stream: MediaStream?) {
//                Log.d("WebRTC", "Remote stream received")
//            }
//
//            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
//            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
//            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
//            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
//            override fun onRemoveStream(stream: MediaStream?) {}
//            override fun onDataChannel(dc: DataChannel?) {}
//            override fun onRenegotiationNeeded() {}
//        })!!
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        videoCapturer.stopCapture()
        peerConnection.close()
        socket.disconnect()
    }
}