package com.example.firstproject.client

import android.content.Context
import android.util.Log
import com.example.firstproject.client.RetrofitClient.WEBRTC_URL
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.Device
import org.mediasoup.droid.Producer
import org.mediasoup.droid.RecvTransport
import org.mediasoup.droid.SendTransport
import org.mediasoup.droid.Transport
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WebRtcClientConnection : CoroutineScope {

    // UI에 원격 비디오 트랙을 전달하기 위한 콜백 (VideoTrack와 해당 producerId를 전달)
    var onRemoteVideo: ((VideoTrack, String) -> Unit)? = null
    var onRemoteAudio: ((AudioTrack, String) -> Unit)? = null
    var onProducerClosed: ((String) -> Unit)? = null

    companion object {
        const val TAG = "WebRtcClientConnection_TAG"
    }

    override val coroutineContext = Dispatchers.IO
    private var mSocket: Socket? = null

    // mediasoup-client의 Device 인스턴스
    private var device: Device? = null

    // 송신/수신 트랜스포트
    private var producerTransport: SendTransport? = null
    private var recvTransport: RecvTransport? = null

    // 미디어 프로듀서(자신의 미디어)와 소비자(타인의 미디어)
    var camProducer: Producer? = null
    var micProducer: Producer? = null
    val consumers = mutableListOf<Consumer>()
    var producersJsonArray: JSONArray? = null

    // WebRTC 미디어 관련 변수
    var videoCapturer: VideoCapturer? = null
    var localVideoTrack: VideoTrack? = null
    var videoSource: VideoSource? = null

    var localAudioTrack: AudioTrack? = null
    var audioSource: AudioSource? = null

    lateinit var eglBase: EglBase

    // PeerConnectionFactory는 WebRTC 미디어 소스와 트랙을 생성하는 팩토리
    private var peerConnectionFactory: PeerConnectionFactory? = null

    /**
     * WebRTC 및 PeerConnectionFactory 초기화
     * Application이나 Activity의 onCreate 등 초기에 호출
     */
    fun init(context: Context) {
        connectSocket()

        // 1. WebRTC 라이브러리 초기화
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        // 2. EGLContext 생성 (비디오 렌더링 및 인코딩에 필요)
        eglBase = EglBase.create()

        // 3. Video Encoder/Decoder Factory 생성
        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        // 4. PeerConnectionFactory 생성
        peerConnectionFactory =
            PeerConnectionFactory.builder().setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory).createPeerConnectionFactory()

        // 5. SurfaceTextureHelper 생성 (비디오 캡쳐 초기화에 필요)
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

        // 6. 전면 카메라 기반 VideoCapturer 생성
        videoCapturer = createCameraCapturer(context)

        // 7. VideoSource 및 캡쳐 초기화
        videoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer!!.initialize(
            surfaceTextureHelper, context, videoSource?.capturerObserver
        )

        getAudioTrack()
        getVideoTrack()
    }

    /**
     * 서버와의 소켓 연결을 생성하고, 이벤트 리스너를 등록함.
     */
    private fun connectSocket() {
        mSocket = IO.socket(WEBRTC_URL)
        mSocket?.connect()

        addSocketListeners()
    }

    private fun addSocketListeners() {
        Log.d(TAG, "Setting up socket event listeners.")
        mSocket?.let { socket ->
            socket.on(Socket.EVENT_CONNECT, onConnect)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            // 다른 클라이언트가 새로운 프로듀서를 생성했을 때 처리
            socket.on("newProducer") { data ->
                val payload = data[0] as JSONObject

                val newProducerId = payload.getString("producerId")
                val peerId = payload.optString("peerId")
                val kind = payload.getString("kind") // "video" 또는 "audio"

                Log.d(TAG, "newProducer: $newProducerId, kind: $kind")

                // 자신의 producer 인 경우 스킵
                if (peerId == mSocket?.id()) {
                    Log.d(TAG, "newProducer: Skip own producer event.")
                    return@on
                }

                // 새로운 프로듀서의 미디어 소비 시작
                consume(newProducerId, kind)
            }

            // 프로듀서 종료 이벤트 처리
            socket.on("producerClosed") { data ->
                val payload = data[0] as JSONObject

                val producerId = payload.getString("producerId")
                Log.d(TAG, "producerClosed -> producerId = $producerId")

                onProducerClosed?.invoke(producerId)

                // consumers 리스트에서 해당 producerId와 일치하는 소비자(Consumer) 제거
                val iterator = consumers.iterator()
                while (iterator.hasNext()) {
                    val consumer = iterator.next()
                    if (consumer.producerId == producerId) { // consumer 객체에 producerId 프로퍼티가 있다고 가정
                        consumer.close()  // 소비자 객체를 닫거나 필요한 리소스를 해제
                        iterator.remove()
                        Log.d(TAG, "Removed consumer for producerId: $producerId")
                    }
                }
            }

            // 채팅 메시지 수신 처리
            socket.on("newChatMessage") { data ->
                val payload = data[0] as JSONObject

                val peerId = payload.getString("peerId")
                val message = payload.optString("message")

                Log.d(TAG, "Chat message from $peerId: $message")
            }


        }

    }


    /**
     * 라우터의 RTP Capabilities를 조회하여 mediasoup Device를 초기화하고,
     * 송신 및 수신 트랜스포트를 생성함.
     */
    private fun getRouterRtpCapabilities() {
        launch {
            mSocket?.emit("getRouterRtpCapabilities", Ack { data ->
                val response = data[0] as JSONObject

                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "getRouterRtpCapabilities error: ${response.optString("error")}")
                    return@Ack
                }

                val routerRtpCapabilities = response.getString("routerRtpCapabilities")
                Log.d(TAG, "routerRtpCapabilities = $routerRtpCapabilities\n")

                // Device 초기화 및 capabilities 로딩
                device = Device().apply { load(routerRtpCapabilities, null) }
                Log.d(TAG, "Mediasoup Device created.")

                // 송신, 수신 트랜스포트 생성
                createSendTransport()
                createRecvTransport()
            })
        }
    }


    /**
     * 송신 트랜스포트를 생성하여 미디어를 전송할 준비를 함.
     */
    private fun createSendTransport() {
        val data = JSONObject().apply {
            put("sender", true)
        }

        launch {
            mSocket?.emit("createWebRtcTransport", data, Ack { data ->
                val response = data[0] as JSONObject

                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "Error creating send transport: ${response.optString("error")}")
                    return@Ack
                }

                val params = response.getJSONObject("params")

                val transportId = params.getString("id")
                val iceParameters = params.getString("iceParameters")
                val iceCandidates = params.getString("iceCandidates")
                val dtlsParameters = params.getString("dtlsParameters")

                val sendTransportListener: SendTransport.Listener =
                    object : SendTransport.Listener {
                        override fun onConnect(transport: Transport, dtlsParameters: String) {
                            Log.d(TAG, "Send transport connecting...")

                            val data = JSONObject().apply {
                                put("transportId", transport.id)
                                put("dtlsParameters", toJsonObject(dtlsParameters))
                            }
                            mSocket?.emit("connectTransport", data, Ack { data ->
                                val response = data[0] as JSONObject

                                if (!response.getBoolean("ok")) Log.e(
                                    TAG,
                                    "Error connecting send transport: ${response.optString("error")}"
                                )
                                else Log.d(TAG, "Send transport connected successfully.")
                            })
                        }

                        override fun onConnectionStateChange(
                            transport: Transport?, connectionState: String?
                        ) {
                            Log.d(TAG, "Send transport state changed: $connectionState")
                        }

                        override fun onProduce(
                            transport: Transport,
                            kind: String,
                            rtpParameters: String,
                            appData: String?
                        ): String {
                            Log.d(TAG, "ProducerTransport onProduce: $kind")

                            val data = JSONObject().apply {
                                put("transportId", transport.id)
                                put("kind", kind)
                                put("rtpParameters", toJsonObject(rtpParameters))
                            }

                            // CompletableFuture 를 사용해 서버 응답을 대기함
                            val producerId = getProducerIdAsync(data)
                            return producerId.toString()
                        }

                        override fun onProduceData(
                            transport: Transport?,
                            sctpStreamParameters: String?,
                            label: String?,
                            protocol: String?,
                            appData: String?
                        ): String {
                            return ""
                        }
                    }

                // mediasoup Device를 통해 송신 트랜스포트 생성
                producerTransport = device?.createSendTransport(
                    sendTransportListener, transportId, iceParameters, iceCandidates, dtlsParameters
                )

                produceVideo()
                produceAudio()
            })
        }
    }

    /**
     * 수신 트랜스포트를 생성하여 원격 미디어를 수신할 준비를 함.
     */
    private fun createRecvTransport() {
        val data = JSONObject().apply {
            put("sender", false)
        }

        launch {
            mSocket?.emit("createWebRtcTransport", data, Ack { data ->
                val response = data[0] as JSONObject

                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "Error creating receive transport: ${response.optString("error")}")
                    return@Ack
                }

                val params = response.getJSONObject("params")

                val transportId = params.getString("id")
                val iceParameters = params.getString("iceParameters")
                val iceCandidates = params.getString("iceCandidates")
                val dtlsParameters = params.getString("dtlsParameters")

                val recvTransportListener: RecvTransport.Listener =
                    object : RecvTransport.Listener {
                        override fun onConnect(transport: Transport, dtlsParameters: String) {
                            Log.d(TAG, "Receive transport connecting...")

                            val data = JSONObject().apply {
                                put("transportId", transport.id)
                                put("dtlsParameters", toJsonObject(dtlsParameters))
                            }
                            mSocket?.emit("connectTransport", data, Ack { args ->
                                val response = args[0] as JSONObject
                                if (!response.getBoolean("ok")) Log.e(
                                    TAG,
                                    "Error connecting receive transport: ${response.optString("error")}"
                                )
                                else Log.d(TAG, "Receive transport connected successfully.")
                            })
                        }

                        override fun onConnectionStateChange(
                            transport: Transport, connectionState: String
                        ) {
                            Log.d(TAG, "Receive transport state changed: $connectionState")
                        }

                    }

                recvTransport = device?.createRecvTransport(
                    recvTransportListener, transportId, iceParameters, iceCandidates, dtlsParameters
                )

                producersJsonArray?.let {
                    Log.d(TAG, "consume!!")
                    for (i in 0 until it.length()) {
                        val producerJson = it.getJSONObject(i)

                        val producerId = producerJson.getString("producerId")
                        val kind = producerJson.getString("kind")
                        val peerId = producerJson.optString("peerId")

                        if (peerId != mSocket?.id()) {
                            consume(producerId, kind)
                        }
                    }
                }
            })
        }
    }


    fun getProducerIdAsync(data: JSONObject): CompletableFuture<String> {
        val futureResult = CompletableFuture<String>()

        mSocket?.emit("produce", data, Ack { args ->
            val response = args[0] as JSONObject
            val producerId: String

            try {
                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "Error during production: ${response.optString("error")}")
                    producerId = ""
                } else {
                    producerId = response.getString("id")
                }
                futureResult.complete(producerId)
            } catch (e: JSONException) {
                futureResult.completeExceptionally(e)
            }
        })
        return futureResult
    }


    private fun getVideoTrack() {
        videoCapturer?.startCapture(1280, 720, 30)
        localVideoTrack = peerConnectionFactory?.createVideoTrack("videoTrack", videoSource)
        Log.d(TAG, "getVideoTrack: start")
    }

    /**
     * 로컬 비디오를 송신 트랜스포트를 통해 전송
     */
    private fun produceVideo() {
        Log.d(TAG, "Produce video")
        camProducer = producerTransport?.produce(
            { Log.d(TAG, "Local video producer closed") }, localVideoTrack, null, null, null
        )
    }

    private fun getAudioTrack() {
        audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("mic", audioSource)
        localAudioTrack?.setEnabled(true)
        Log.d(TAG, "getAudioTrack: start")
    }

    /**
     * 로컬 오디오를 송신 트랜스포트를 통해 전송
     */
    private fun produceAudio() {

        Log.d(TAG, "Produce audio")
        micProducer = producerTransport?.produce(
            { Log.d(TAG, "Local audio producer closed") }, localAudioTrack, null, null, null
        )
    }

    /**
     * 방 입장 요청, 방에 존재하는 다른 프로듀서들의 미디어 consume
     */
    fun joinRoom(roomId: String) {
        Log.d(TAG, "joinRoom: $roomId")

        val data = JSONObject().apply {
            put("roomId", roomId)
        }

        launch {
            mSocket?.emit("joinRoom", data, Ack { data ->
                val response = data[0] as JSONObject

                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "joinRoom error: ${response.optString("error")}")
                    return@Ack
                }

                // 기존 방의 프로듀서 리스트
                producersJsonArray = response.optJSONArray("producers")
                Log.d(TAG, "joinRoom: producers = $producersJsonArray")

                // 라우터 RTP capabilities를 가져오고, 트랜스포트를 생성
                getRouterRtpCapabilities()
            })
        }
    }

    /**
     * 원격 프로듀서로부터 미디어를 소비(수신)함.
     *
     * @param producerId 소비할 프로듀서의 ID
     * @param kind 미디어 타입 ("video" 또는 "audio")
     */
    private fun consume(producerId: String, kind: String) {
        Log.d(TAG, "Consuming media from producer: $producerId, kind: $kind")

        val data = JSONObject().apply {
            put("transportId", recvTransport?.id)
            put("producerId", producerId)
            put("rtpCapabilities", toJsonObject(device!!.rtpCapabilities))
        }

        launch {
            mSocket?.emit("consume", data, Ack { data ->
                Log.d(TAG, "consume: started...")
                val response = data[0] as JSONObject

                // response로 false를 받으면 error 로그 띄우기
                if (!response.getBoolean("ok")) {
                    Log.e(TAG, "Error consuming producer: ${response.optString("error")}")
                    return@Ack
                }

                val params = response.getJSONObject("params")

                val consumerId = params.getString("id")
                val producerId = params.getString("producerId")
                val kind = params.getString("kind")
                val rtpParameters = params.getString("rtpParameters")

                val consumer = recvTransport?.consume(
                    { Log.d(TAG, "Consumer $consumerId closed") },
                    consumerId,
                    producerId,
                    kind,
                    rtpParameters
                )

                consumer?.let {
                    consumers.add(it)
                    Log.d(TAG, "consume: producerId=${it.producerId}")
                    if (kind == "video") onRemoteVideo?.invoke(it.track as VideoTrack, producerId)
                    else if (kind == "audio") onRemoteAudio?.invoke(
                        it.track as AudioTrack,
                        producerId
                    )
                }
            })
        }
    }

    /**
     * 채팅 메시지를 서버로 전송함.
     *
     * @param message 전송할 메시지 내용
     * @return 메시지 전송 성공 여부
     */
    suspend fun sendChatMessage(message: String): Boolean {
        if (message.isBlank()) {
            Log.d(TAG, "sendChatMessage: 메시지가 비어있습니다.")
            return false
        }

        val data = JSONObject().apply {
            put("message", message)
        }

        val response = mSocket?.emitAndAwait("chatMessage", data)
        return if (response?.getBoolean("ok") == true) {
            true
        } else {
            Log.e(TAG, "Error sending chat message: ${response?.optString("error")}")
            false
        }
    }


    /**
     * 서버로부터 사용 가능한 방 목록을 조회함.
     *
     * @return 방 이름 목록, 실패 시 빈 리스트 반환
     */
    suspend fun getRoomList(): List<String> {
        Log.d(TAG, "Fetching room list.")

        val response = mSocket?.emitAndAwait("getRooms")
        return if (response?.getBoolean("ok") == true) {
            val roomsJsonArray = response.getJSONArray("rooms")
            List(roomsJsonArray.length()) { i -> roomsJsonArray.getString(i) }
        } else {
            Log.e(TAG, "Error fetching room list: ${response?.optString("error")}")
            emptyList()
        }
    }

    /**
     * 현재 방에서 퇴장하고, 관련 리소스를 정리함.
     */
    fun leaveRoom() {
        Log.d(TAG, "Leaving room.")

        producerTransport?.close()
        recvTransport?.close()
        camProducer?.close()
        micProducer?.close()
        videoCapturer?.stopCapture()

        mSocket?.emit("leaveRoom", Ack { data ->
            val response = data[0] as JSONObject

            if (response.getBoolean("ok")) Log.d(TAG, "Left room successfully.")
            else Log.e(TAG, "Error leaving room: ${response.optString("error")}")
        })
    }

    fun getSocket(): Socket? {
        return mSocket
    }

    fun close() {
        mSocket = null
        producerTransport = null
        device = null
        camProducer = null
        videoCapturer?.dispose()
        micProducer = null
        peerConnectionFactory = null

    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "Socket connected: ${mSocket?.id()}")
    }

    private val onConnectError = Emitter.Listener {
        Log.d(TAG, "Socket connection error.")
        mSocket?.disconnect()
    }

    private val onDisconnect = Emitter.Listener {
        Log.d(TAG, "Socket disconnected.")

        producerTransport?.close()
        recvTransport?.close()
        camProducer?.close()
        micProducer?.close()
        audioSource?.dispose()
        videoSource?.dispose()
        videoCapturer?.stopCapture()

        close()
    }

    /**
     * 전면 카메라를 사용하여 VideoCapturer를 생성함.
     *
     * @param context 애플리케이션 컨텍스트
     * @return VideoCapturer 인스턴스
     * @throws RuntimeException 사용 가능한 전면 카메라가 없을 경우 예외 발생
     */
    private fun createCameraCapturer(context: Context): VideoCapturer {
        val enumerator = Camera2Enumerator(context)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName,
                    object : CameraVideoCapturer.CameraEventsHandler {
                        override fun onCameraError(error: String) {
                            Log.w("CameraEvents", error)
                        }

                        override fun onCameraDisconnected() {
                            Log.w("CameraEvents", "Camera disconnected.")
                        }

                        override fun onCameraFreezed(error: String) {
                            Log.w("CameraEvents", "Camera freezed: $error")
                        }

                        override fun onCameraOpening(cameraName: String) {
                            Log.w("CameraEvents", "Opening camera: $cameraName")
                        }

                        override fun onFirstFrameAvailable() {
                            Log.w("CameraEvents", "First frame available.")
                        }

                        override fun onCameraClosed() {
                            Log.w("CameraEvents", "Camera closed.")
                        }
                    })
            }
        }
        throw RuntimeException("No available front camera found.")
    }

    /**
     * 문자열 형태의 JSON 데이터를 JSONObject로 변환하는 헬퍼 함수
     *
     * @param data JSON 문자열
     * @return 변환된 JSONObject, 실패 시 빈 JSONObject 반환
     */
    private fun toJsonObject(data: String): JSONObject {
        return try {
            JSONObject(data)
        } catch (e: JSONException) {
            e.printStackTrace()
            JSONObject()
        }
    }


    /**
     * Socket의 emit 이벤트를 호출하고 JSON 응답을 대기하는 확장 함수
     */
    private suspend fun Socket.emitAndAwait(event: String): JSONObject =
        suspendCancellableCoroutine { continuation ->
            this.emit(event, Ack { data ->
                try {
                    val response = data[0] as JSONObject
                    continuation.resume(response)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            })
        }

    /**
     * 데이터를 포함하여 Socket의 emit 이벤트를 호출하고 JSON 응답을 대기하는 확장 함수
     */
    private suspend fun Socket.emitAndAwait(event: String, data: JSONObject): JSONObject =
        suspendCancellableCoroutine { continuation ->
            this.emit(event, data, Ack { data ->
                try {
                    val response = data[0] as JSONObject
                    continuation.resume(response)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            })
        }

}