import { Device } from "mediasoup-client"

class VideoChat {
  constructor() {
    // 소켓 연결 설정 (HTTPS 사용, 인증서 검증 무시)
    this.socket = io({
      secure: true,
      rejectUnauthorized: false,
    })

    // mediasoup-client의 Device 인스턴스 (WebRTC 장치)
    this.device = null

    // 송신/수신 트랜스포트
    this.producerTransport = null
    this.consumerTransport = null

    // 미디어 트랙(Producer, Consumer) 관리
    this.producers = new Map()
    this.consumers = new Map()

    // 미디어 상태 및 기본 정보
    this.isProducing = false
    this.roomId = null // 초기에는 방 이름이 없으므로 null로 설정
    this.localVideo = null
    this.localStream = null
    this.isMuted = false
    this.isVideoOff = false

    // UI 요소 초기화
    this.initializeElements()

    // 소켓 이벤트 핸들러 등록
    this.addSocketListeners()

    // 페이지 로드 시 방 목록을 요청합니다.
    this.getRoomList()

    // 방 입장 전 UI 숨김
    this.toggleUIOnJoinRoom(false)
  }

  // DOM 요소 초기화
  initializeElements() {
    this.videoContainer = document.getElementById("videoContainer")
    this.muteBtn = document.getElementById("muteBtn")
    this.videoBtn = document.getElementById("videoBtn")
    this.leaveBtn = document.getElementById("leaveBtn") // 초기 숨김 상태 (CSS에서 display: none)

    // 채팅 UI 요소 초기화 (초기 숨김 상태)
    this.chatContainer = document.getElementById("chatContainer")
    this.chatMessages = document.getElementById("chatMessages")
    this.chatInput = document.getElementById("chatInput")
    this.sendChatBtn = document.getElementById("sendChatBtn")

    // 방 목록 UI 요소 초기화
    this.roomList = document.getElementById("roomList")
    this.refreshRoomsBtn = document.getElementById("refreshRoomsBtn")

    // 방 생성 UI 요소 초기화
    this.roomInput = document.getElementById("roomInput")
    this.createRoomBtn = document.getElementById("createRoomBtn")

    // 버튼 이벤트 등록
    // 방 생성 버튼 클릭 시, 입력한 방 이름으로 joinRoom() 호출
    this.createRoomBtn.addEventListener("click", () => {
      const roomName = this.roomInput.value.trim()
      if (!roomName) {
        alert("방 이름을 입력해주세요.")
        return
      }
      this.roomId = roomName
      this.joinRoom()
    })

    // 방 목록 새로고침 버튼 클릭 시 방 목록 재요청
    this.refreshRoomsBtn.addEventListener("click", () => this.getRoomList())

    // 채팅 전송 버튼 및 엔터키 이벤트 등록
    this.sendChatBtn.addEventListener("click", () => this.sendChatMessage())
    this.chatInput.addEventListener("keyup", (e) => {
      if (e.key === "Enter") {
        this.sendChatMessage()
      }
    })

    // 기존 제어 버튼(음소거, 비디오 토글, 나가기) 이벤트 등록
    this.muteBtn.addEventListener("click", () => this.toggleMute())
    this.videoBtn.addEventListener("click", () => this.toggleVideo())
    this.leaveBtn.addEventListener("click", () => this.leaveRoom())
  }

  // 소켓 이벤트 핸들러 등록
  addSocketListeners() {
    this.socket.on("connect", () => {
      console.log("[client] Socket connected ->", this.socket.id)
    })

    // 서버에서 새로운 프로듀서가 생성되었음을 알림 (자기 자신 제외)
    this.socket.on("newProducer", async ({ producerId, peerId, kind }) => {
      if (peerId === this.socket.id) {
        console.log("[client] Skipping own producer (newProducer event).")
        return
      }
      console.log(`[client] newProducer event: producerId=${producerId}, peerId=${peerId}, kind=${kind}`)
      try {
        await this.consume(producerId, peerId, kind)
      } catch (err) {
        console.error("[client] consume error:", err)
      }
    })

    // 다른 피어가 연결 종료되었을 때 알림 수신
    this.socket.on("peerClosed", ({ peerId }) => {
      console.log(`[client] peerClosed event: peerId=${peerId}`)
      this.removeVideoElement(peerId)
    })

    // 서버에서 Producer 종료 알림
    this.socket.on("producerClosed", ({ producerId }) => {
      console.log(`[client] producerClosed -> producerId=${producerId}`)
    })

    // 채팅 메시지 수신 처리
    this.socket.on("newChatMessage", ({ peerId, message }) => {
      console.log(`[client] newChatMessage: ${peerId} : ${message}`)
      this.displayChatMessage(peerId, message)
    })

    // 서버로부터 방 목록 응답 수신 (getRooms 이벤트 응답)
    this.socket.on("getRooms", (response) => {
      if (response.ok) {
        this.renderRoomList(response.rooms)
      } else {
        console.error("방 목록 요청 실패:", response.error)
      }
    })

    // 서버로부터 다른 peer가 나갔음을 수신
    this.socket.on("peerLeft", ({ peerId }) => {
      console.log(`[client] peerLeft event: peerId=${peerId}`)
      this.removeVideoElement(peerId)
    })
  }

  // 방 참가 처리 (사용자가 입력한 roomId를 사용)
  async joinRoom() {
    try {
      console.log("[client] joinRoom called")
      // 1) 로컬 미디어 스트림 획득
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: {
          width: { ideal: 1280 },
          height: { ideal: 720 },
          frameRate: { ideal: 30 },
        },
      })
      console.log("[client] Got localStream:", this.localStream)
      this.displayLocalVideo()

      // 2) 서버에 joinRoom 요청 (사용자가 입력한 roomId)
      console.log("[client] Emitting joinRoom ->", this.roomId)
      const joinResponse = await this.emitWithPromise("joinRoom", {
        roomId: this.roomId,
      })
      console.log("[client] joinRoom response:", joinResponse)
      if (!joinResponse.ok) {
        throw new Error(joinResponse.error || "joinRoom failed")
      }

      // 3) 서버의 RTP Capabilities 요청
      const rtpCapResponse = await this.getRouterRtpCapabilities()
      console.log("[client] getRouterRtpCapabilities response:", rtpCapResponse)
      if (!rtpCapResponse.ok) {
        throw new Error(rtpCapResponse.error || "RTP Capabilities failed")
      }

      // 4) mediasoup Device 생성 및 로드
      console.log("[client] Creating Device...")
      this.device = new Device()
      await this.device.load({
        routerRtpCapabilities: rtpCapResponse.routerRtpCapabilities,
      })
      console.log("[client] Device loaded.", this.device)

      // 5) 송신/수신 트랜스포트 생성
      console.log("[client] Creating ProducerTransport...")
      await this.createProducerTransport()
      console.log("[client] ProducerTransport created.")
      console.log("[client] Creating ConsumerTransport...")
      await this.createConsumerTransport()
      console.log("[client] ConsumerTransport created.")

      // 6) 미디어 송신을 위한 프로듀서 생성 (비디오, 오디오)
      console.log("[client] Creating producers...")
      await this.produce("video")
      await this.produce("audio")

      // 7) 이미 존재하는 다른 피어의 프로듀서 소비
      for (const { producerId, peerId, kind } of joinResponse.producers) {
        if (peerId === this.socket.id) {
          console.log("[client] Skipping own producer in joinRoom producers list.")
          continue
        }
        console.log(`[client] Consuming existing producer -> ${producerId}, kind=${kind}`)
        await this.consume(producerId, peerId, kind)
      }

      // 방 참가 성공 시, 방 목록 및 방 생성 UI는 숨기고, 채팅창과 나가기 버튼을 표시
      this.toggleUIOnJoinRoom(true)

      // 입력 필드 초기화
      this.roomInput.value = ""
    } catch (err) {
      console.error("[client] joinRoom error:", err)
      alert("오류가 발생했습니다: " + err.message)
    }
  }

  // RTP Capabilities 요청 (서버에)
  getRouterRtpCapabilities() {
    console.log("[client] getRouterRtpCapabilities called")
    return new Promise((resolve) => {
      this.socket.emit("getRouterRtpCapabilities", (response) => {
        resolve(response)
      })
    })
  }

  // 송신 트랜스포트 생성 요청
  async createProducerTransport() {
    console.log("[client] createProducerTransport emit...")
    const { ok, params, error } = await this.emitWithPromise("createWebRtcTransport", {
      sender: true,
    })
    if (!ok) {
      throw new Error(error || "createProducerTransport failed")
    }
    console.log("[client] ProducerTransport params:", params)
    this.producerTransport = this.device.createSendTransport(params)
    // 송신 트랜스포트 연결 처리
    this.producerTransport.on("connect", async ({ dtlsParameters }, callback, errback) => {
      console.log("[client] ProducerTransport connect event", dtlsParameters)
      try {
        const connectResp = await this.emitWithPromise("connectTransport", {
          transportId: this.producerTransport.id,
          dtlsParameters,
        })
        console.log("[client] connectTransport response:", connectResp)
        callback()
      } catch (error) {
        errback(error)
      }
    })
    // 프로듀서 생성 요청 처리 (트랙 송신)
    this.producerTransport.on("produce", async ({ kind, rtpParameters }, callback, errback) => {
      console.log("[client] ProducerTransport produce event -> kind:", kind)
      try {
        const produceResp = await this.emitWithPromise("produce", {
          transportId: this.producerTransport.id,
          kind,
          rtpParameters,
        })
        console.log("[client] produce response:", produceResp)
        if (!produceResp.ok) {
          throw new Error(produceResp.error)
        }
        callback({ id: produceResp.id })
      } catch (error) {
        errback(error)
      }
    })
  }

  // 수신 트랜스포트 생성 요청
  async createConsumerTransport() {
    console.log("[client] createConsumerTransport emit...")
    const { ok, params, error } = await this.emitWithPromise("createWebRtcTransport", {
      sender: false,
    })
    if (!ok) {
      throw new Error(error || "createConsumerTransport failed")
    }
    console.log("[client] ConsumerTransport params:", params)
    this.consumerTransport = this.device.createRecvTransport(params)
    // 수신 트랜스포트 연결 처리
    this.consumerTransport.on("connect", async ({ dtlsParameters }, callback, errback) => {
      console.log("[client] ConsumerTransport connect event", dtlsParameters)
      try {
        const connectResp = await this.emitWithPromise("connectTransport", {
          transportId: this.consumerTransport.id,
          dtlsParameters,
        })
        console.log("[client] connectTransport response:", connectResp)
        callback()
      } catch (error) {
        errback(error)
      }
    })
  }

  // 프로듀서 생성 (미디어 송신)
  async produce(kind) {
    console.log(`[client] produce called -> kind=${kind}`)
    const track = kind === "video" ? this.localStream.getVideoTracks()[0] : this.localStream.getAudioTracks()[0]
    if (!track) {
      console.warn(`[client] No track found for ${kind}`)
      return
    }
    const producer = await this.producerTransport.produce({
      track,
      encodings:
        kind === "video"
          ? [
              { rid: "r0", maxBitrate: 100000 },
              { rid: "r1", maxBitrate: 300000 },
              { rid: "r2", maxBitrate: 900000 },
            ]
          : undefined,
    })
    console.log(`[client] Producer created -> id=${producer.id}, kind=${producer.kind}`)
    this.producers.set(kind, producer)
    producer.on("trackended", () => {
      console.log(`[client] track ended -> ${kind}`)
    })
    producer.on("transportclose", () => {
      console.log(`[client] transport closed -> ${kind}`)
    })
  }

  // 다른 피어의 프로듀서를 소비하여 원격 미디어 표시
  async consume(producerId, peerId, kind) {
    console.log(`[client] consume called -> producerId=${producerId}, peerId=${peerId}, kind=${kind}`)
    const consumeResp = await this.emitWithPromise("consume", {
      transportId: this.consumerTransport.id,
      producerId,
      rtpCapabilities: this.device.rtpCapabilities,
    })
    console.log("[client] consume response:", consumeResp)
    if (!consumeResp.ok) {
      throw new Error(consumeResp.error || "consume failed")
    }
    const { params } = consumeResp
    const consumer = await this.consumerTransport.consume(params)
    this.consumers.set(consumer.id, consumer)
    const mediaStream = new MediaStream([consumer.track])
    console.log(`[client] consumer track =>`, consumer.track)
    this.displayRemoteVideo(mediaStream, peerId, kind)
    console.log("스트림=", mediaStream.getTracks())
    console.log("[client] Resume consumer...")
    await consumer.resume()
    console.log(`[client] Consumer resumed -> id=${consumer.id}`)
    consumer.on("transportclose", () => {
      console.log(`consumer ${consumer.id} closed`)
    })
    consumer.on("trackended", () => {
      console.log(`trackended ${consumer.id}`)
    })
  }

  // 로컬 비디오 스트림 표시
  displayLocalVideo() {
    console.log("[client] displayLocalVideo")
    const videoWrapper = document.createElement("div")
    videoWrapper.className = "video-wrapper"
    videoWrapper.id = "local"
    const video = document.createElement("video")
    video.srcObject = this.localStream
    video.autoplay = true
    video.playsInline = true
    video.muted = true
    videoWrapper.appendChild(video)
    this.videoContainer.appendChild(videoWrapper)
    this.localVideo = video
  }

  // 원격 미디어 스트림 표시 (비디오/오디오)
  displayRemoteVideo(stream, peerId, kind) {
    console.log(`[client] displayRemoteVideo -> peerId=${peerId}, kind=${kind}`)
    let videoWrapper = document.getElementById(`peer-${peerId}`)
    if (!videoWrapper) {
      videoWrapper = document.createElement("div")
      videoWrapper.className = "video-wrapper"
      videoWrapper.id = `peer-${peerId}`
      const video = document.createElement("video")
      video.autoplay = true
      video.playsInline = true
      videoWrapper.appendChild(video)
      this.videoContainer.appendChild(videoWrapper)
    }
    const video = videoWrapper.querySelector("video")
    if (kind === "video") {
      video.srcObject = stream
    } else {
      const existingStream = video.srcObject
      if (existingStream) {
        existingStream.addTrack(stream.getAudioTracks()[0])
      } else {
        video.srcObject = stream
      }
    }
    console.log("[client] video.srcObject =", video.srcObject)
  }

  // 특정 피어의 비디오 엘리먼트 제거
  removeVideoElement(peerId) {
    console.log(`[client] removeVideoElement -> peerId=${peerId}`)
    const videoWrapper = document.getElementById(`peer-${peerId}`)
    if (videoWrapper) {
      videoWrapper.remove()
    }
  }

  // 음소거 토글 (로컬 오디오)
  toggleMute() {
    console.log("[client] toggleMute")
    const audioTrack = this.localStream && this.localStream.getAudioTracks()[0]
    if (audioTrack) {
      audioTrack.enabled = !audioTrack.enabled
      this.isMuted = !audioTrack.enabled
      this.muteBtn.textContent = this.isMuted ? "음소거 해제" : "음소거"
      console.log(`[client] Mute status -> ${this.isMuted}`)
    }
  }

  // 비디오 On/Off 토글 (로컬 비디오)
  toggleVideo() {
    console.log("[client] toggleVideo")
    const videoTrack = this.localStream && this.localStream.getVideoTracks()[0]
    if (videoTrack) {
      videoTrack.enabled = !videoTrack.enabled
      this.isVideoOff = !videoTrack.enabled
      this.videoBtn.textContent = this.isVideoOff ? "비디오 켜기" : "비디오 끄기"
      console.log(`[client] Video status -> ${this.isVideoOff}`)
    }
  }

  // 방 떠나기 처리 (방에서 나가되, 소켓 연결은 유지)
  async leaveRoom() {
    console.log("[client] leaveRoom called")

    // 송신/수신 트랜스포트 종료 및 null 처리
    if (this.producerTransport) {
      this.producerTransport.close()
      this.producerTransport = null
      console.log("[client] ProducerTransport closed")
    }
    if (this.consumerTransport) {
      this.consumerTransport.close()
      this.consumerTransport = null
      console.log("[client] ConsumerTransport closed")
    }

    // 로컬 스트림의 트랙 중지 및 스트림 초기화
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop())
      this.localStream = null
      console.log("[client] Local stream tracks stopped")
    }

    // 비디오 영역 초기화
    this.videoContainer.innerHTML = ""

    // 채팅 내용 초기화
    this.chatMessages.innerHTML = ""

    // 서버에 방 떠남을 알리는 leaveRoom 이벤트 전송
    this.socket.emit("leaveRoom", (response) => {
      if (response.ok) {
        console.log("[client] Successfully left the room")
      } else {
        console.error("[client] Error leaving the room:", response.error)
      }
    })

    // UI를 방에 입장하기 전 상태로 업데이트
    this.toggleUIOnJoinRoom(false)
  }

  // 채팅 메시지 전송 처리
  sendChatMessage() {
    const message = this.chatInput.value.trim()
    if (!message) return
    this.socket.emit("chatMessage", { message }, (response) => {
      if (response.ok) {
        this.chatInput.value = ""
        this.displayChatMessage("나", message)
      } else {
        console.error("채팅 메시지 전송 실패:", response.error)
        alert("메시지 전송 실패: " + response.error)
      }
    })
  }

  // 채팅 메시지를 UI에 표시
  displayChatMessage(peerId, message) {
    const messageElem = document.createElement("div")
    messageElem.style.padding = "4px 0"
    if (peerId === "나") {
      messageElem.style.fontWeight = "bold"
    }
    messageElem.textContent = `[${peerId}] ${message}`
    this.chatMessages.appendChild(messageElem)
    this.chatMessages.scrollTop = this.chatMessages.scrollHeight
  }

  // 서버 emit을 Promise 방식으로 호출 (비동기 응답 처리)
  emitWithPromise(event, data = {}) {
    console.log(`[client] emitWithPromise -> event=${event}, data=`, data)
    return new Promise((resolve) => {
      this.socket.emit(event, data, (response) => {
        console.log(`[client] emitWithPromise response -> event=${event}, resp=`, response)
        resolve(response)
      })
    })
  }

  // 소켓 이벤트를 통한 방 목록 요청
  getRoomList() {
    console.log("[client] getRoomList called")
    this.socket.emit("getRooms", (response) => {
      if (response.ok) {
        this.renderRoomList(response.rooms)
      } else {
        console.error("방 목록 요청 실패:", response.error)
      }
    })
  }

  // 받은 방 목록을 UI에 렌더링
  renderRoomList(rooms) {
    if (!this.roomList) return
    this.roomList.innerHTML = "" // 기존 목록 초기화
    rooms.forEach((roomId) => {
      const li = document.createElement("li")
      li.textContent = roomId
      li.style.cursor = "pointer"
      li.addEventListener("click", () => {
        // 클릭 시 해당 방 이름으로 설정 후 joinRoom() 호출
        this.roomId = roomId
        this.joinRoom()
      })
      this.roomList.appendChild(li)
    })
  }

  // UI 상태를 업데이트하는 함수
  toggleUIOnJoinRoom(isJoined) {
    const controlsContainer = document.getElementById("controlsContainer") // 컨트롤 박스
    controlsContainer.style.display = isJoined ? "flex" : "none" // ✅ 컨트롤 박스도 숨김

    this.chatContainer.style.display = isJoined ? "block" : "none"
    this.leaveBtn.style.display = isJoined ? "block" : "none"
    this.muteBtn.style.display = isJoined ? "inline-block" : "none"
    this.videoBtn.style.display = isJoined ? "inline-block" : "none"
    this.roomList.parentElement.style.display = isJoined ? "none" : "block"
    this.roomInput.parentElement.style.display = isJoined ? "none" : "block"
  }
}

// 페이지 로드 시 VideoChat 인스턴스 생성
window.addEventListener("load", () => {
  window.videoChat = new VideoChat()
})
