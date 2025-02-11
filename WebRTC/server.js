require("dotenv").config() // 환경 변수 로드

const express = require("express")
const fs = require("fs")
const https = require("https")
const { Server } = require("socket.io")
const mediasoup = require("mediasoup")

const app = express()

// SSL 인증서 및 HTTPS 서버 생성
const httpsOptions = {
  key: fs.readFileSync(process.env.SSL_KEY_PATH),
  cert: fs.readFileSync(process.env.SSL_CERT_PATH),
}
const httpsServer = https.createServer(httpsOptions, app)
const io = new Server(httpsServer)

// 정적 파일 제공 (클라이언트 코드가 위치한 public 폴더)
app.use(express.static("public"))

// mediasoup 설정 (환경 변수 활용)
const config = {
  worker: {
    rtcMinPort: 10000, // 최소 WebRTC 포트
    rtcMaxPort: 10100, // 최대 WebRTC 포트
    logLevel: "debug",
    logTags: ["info", "ice", "dtls", "rtp", "srtp", "rtcp"],
  },
  router: {
    mediaCodecs: [
      {
        kind: "audio",
        mimeType: "audio/opus",
        clockRate: 48000,
        channels: 2,
      },
      {
        kind: "video",
        mimeType: "video/VP8",
        clockRate: 90000,
        parameters: { "x-google-start-bitrate": 1000 },
      },
    ],
  },
  webRtcTransport: {
    listenIps: [
      {
        ip: process.env.LISTEN_IP,
        announcedIp: process.env.ANNOUNCED_IP,
      },
    ],
    initialAvailableOutgoingBitrate: 1000000,
  },
}

/**
 * WorkerManager
 * mediasoup Worker와 Router를 초기화하고 관리합니다.
 */
const WorkerManager = {
  worker: null,
  router: null,

  async initialize() {
    this.worker = await mediasoup.createWorker({ ...config.worker })
    this.worker.on("died", () => {
      console.error("mediasoup worker died, exiting in 2 seconds...")
      setTimeout(() => process.exit(1), 2000)
    })
    this.router = await this.worker.createRouter({
      mediaCodecs: config.router.mediaCodecs,
    })

    console.log("Mediasoup Worker and Router initialized.")
  },
}

/**
 * Peer 클래스
 * 각 클라이언트(피어)의 소켓과 관련 리소스(트랜스포트, 프로듀서, 컨슈머, 방 정보)를 관리합니다.
 */
class Peer {
  constructor(socket) {
    this.socket = socket
    this.transports = new Map() // Map<transportId, transport>
    this.producers = new Map() // Map<producerId, producer>
    this.consumers = new Map() // Map<consumerId, consumer>
    this.roomId = null // 현재 입장한 방의 ID (없으면 null)
  }
}

/**
 * SocketHandler 클래스
 * 클라이언트와의 소켓 연결 및 각종 이벤트를 처리합니다.
 */
class SocketHandler {
  constructor() {
    this.rooms = new Map() // Map<roomId, Set<socketId>>
    this.peers = new Map() // Map<socketId, Peer>
    this.globalProducers = new Map() // Map<producerId, producer> (방 전체에서 공유)
  }

  handleConnection(socket) {
    console.log("Client connected:", socket.id)
    const peer = new Peer(socket)
    this.peers.set(socket.id, peer)

    // 1) 클라이언트가 라우터의 RTP Capabilities를 요청하는 이벤트
    socket.on("getRouterRtpCapabilities", (callback) => {
      try {
        const caps = WorkerManager.router.rtpCapabilities
        callback({ ok: true, routerRtpCapabilities: caps })
      } catch (err) {
        console.error(`[${socket.id}] getRouterRtpCapabilities error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 2) 클라이언트가 현재 존재하는 방 목록을 요청하는 이벤트
    socket.on("getRooms", (callback) => {
      try {
        // rooms Map의 키(roomId)를 배열로 변환하여 반환
        const roomList = Array.from(this.rooms.keys())
        console.log(`[${socket.id}] getRooms ->`, roomList)
        callback({ ok: true, rooms: roomList })
      } catch (err) {
        console.error(`[${socket.id}] getRooms error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 3) 클라이언트가 방에 입장 요청하는 이벤트
    socket.on("joinRoom", ({ roomId }, callback) => {
      console.log(`[${socket.id}] joinRoom -> ${roomId}`)
      try {
        // 해당 roomId가 없으면 새 방을 생성
        if (!this.rooms.has(roomId)) {
          this.rooms.set(roomId, new Set())
          console.log(`new roomId = ${roomId}`)
        }
        // 현재 소켓을 해당 방의 Set에 추가
        this.rooms.get(roomId).add(socket.id)
        // 피어의 roomId를 업데이트
        peer.roomId = roomId

        // 같은 방에 이미 존재하는 다른 피어들의 Producer 목록 수집 (자신 제외)
        const producers = []
        this.rooms.get(roomId).forEach((peerId) => {
          if (peerId !== socket.id) {
            const otherPeer = this.peers.get(peerId)
            otherPeer.producers.forEach((producer) => {
              producers.push({
                producerId: producer.id,
                peerId,
                kind: producer.kind,
              })
            })
          }
        })

        console.log(`[${socket.id}] Current producers in room:`, producers)
        callback({ ok: true, producers })
      } catch (err) {
        console.error(`[${socket.id}] joinRoom error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 클라이언트가 방을 떠나고자 할 때 호출하는 이벤트
    socket.on("leaveRoom", (callback) => {
      try {
        // 방에 입장해 있지 않으면 에러 처리
        if (!peer.roomId) {
          return callback({ ok: false, error: "방에 입장되어 있지 않습니다." })
        }
        const room = this.rooms.get(peer.roomId)
        if (room) {
          // 현재 소켓을 방의 Set에서 제거
          room.delete(socket.id)
          // 같은 방의 다른 피어에게 해당 피어가 떠났음을 알림
          room.forEach((otherPeerId) => {
            const otherPeer = this.peers.get(otherPeerId)
            if (otherPeer) {
              otherPeer.socket.emit("peerLeft", { peerId: socket.id })
            }
          })
          // 만약 방에 남은 피어가 없다면 해당 방을 삭제
          if (room.size === 0) {
            this.rooms.delete(peer.roomId)
          }
        }
        // 현재 사용자의 roomId 초기화
        peer.roomId = null
        console.log(`[${socket.id}] Left room successfully.`)
        callback({ ok: true })
      } catch (err) {
        console.error(`[${socket.id}] leaveRoom error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 4) 클라이언트가 WebRTC 트랜스포트 생성 요청
    socket.on("createWebRtcTransport", async ({ sender }, callback) => {
      console.log(`[${socket.id}] createWebRtcTransport (sender: ${sender})`)
      try {
        const transport = await WorkerManager.router.createWebRtcTransport(config.webRtcTransport)
        // 생성된 트랜스포트를 피어 객체의 transports Map에 저장
        peer.transports.set(transport.id, transport)

        console.log(`[${socket.id}] Transport created -> id=${transport.id}, sender=${sender}`)

        // DTLS 상태 변경 이벤트 처리 (DTLS 상태가 'closed'면 트랜스포트 종료)
        transport.on("dtlsstatechange", (dtlsState) => {
          console.log(`[${socket.id}] DTLS State Change -> ${dtlsState}`)
          if (dtlsState === "closed") transport.close()
        })

        callback({
          ok: true,
          params: {
            id: transport.id,
            iceParameters: transport.iceParameters,
            iceCandidates: transport.iceCandidates,
            dtlsParameters: transport.dtlsParameters,
            sctpParameters: transport.sctpParameters,
          },
        })
      } catch (err) {
        console.error(`[${socket.id}] createWebRtcTransport error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 5) 클라이언트가 트랜스포트 연결 요청 (DTLS 연결)
    socket.on("connectTransport", async ({ transportId, dtlsParameters }, callback) => {
      console.log(`[${socket.id}] connectTransport -> transportId=${transportId}`)
      try {
        const transport = peer.transports.get(transportId)
        if (!transport) {
          throw new Error(`Transport not found: ${transportId}`)
        }
        await transport.connect({ dtlsParameters })
        console.log(`[${socket.id}] Transport connected -> ${transportId}`)
        callback({ ok: true })
      } catch (err) {
        console.error(`[${socket.id}] connectTransport error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 6) 클라이언트가 프로듀서 생성 요청 (미디어 송신)
    socket.on("produce", async ({ transportId, kind, rtpParameters }, callback) => {
      console.log(`[${socket.id}] produce -> transportId=${transportId}, kind=${kind}`)
      const transport = peer.transports.get(transportId)
      if (!transport) {
        console.warn(`[${socket.id}] produce failed: Transport not found`)
        return callback({ ok: false, error: "Transport not found" })
      }
      try {
        // 프로듀서 생성 (미디어 송신 시작)
        const producer = await transport.produce({ kind, rtpParameters })

        // 생성된 프로듀서를 피어 객체의 producers Map에 저장
        peer.producers.set(producer.id, producer)
        // 전역 프로듀서 맵에도 등록 (다른 피어가 소비할 수 있도록)
        this.globalProducers.set(producer.id, producer)

        console.log(`[${socket.id}] Producer created -> id=${producer.id}, kind=${kind}`)

        // 트랜스포트가 종료될 때 프로듀서를 정리
        producer.on("transportclose", () => {
          console.log(`[${socket.id}] Producer closed (transportclose) -> ${producer.id}`)
          peer.producers.delete(producer.id)
          this.globalProducers.delete(producer.id)
        })

        // 같은 방의 다른 피어들에게 새 프로듀서 생성 알림 전송
        const room = this.rooms.get(peer.roomId)
        if (room) {
          room.forEach((peerId) => {
            if (peerId !== socket.id) {
              console.log(`[${socket.id}] Notifying newProducer to -> ${peerId}`)
              const otherPeer = this.peers.get(peerId)
              otherPeer.socket.emit("newProducer", {
                producerId: producer.id,
                peerId: socket.id,
                kind,
              })
            }
          })
        }

        callback({ ok: true, id: producer.id })
      } catch (err) {
        console.error(`[${socket.id}] produce error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 7) 클라이언트가 소비자 생성 요청 (미디어 수신)
    socket.on("consume", async ({ transportId, producerId, rtpCapabilities }, callback) => {
      console.log(`[${socket.id}] consume -> producerId=${producerId}`)
      const peerData = this.peers.get(socket.id)
      if (!peerData) {
        return callback({ ok: false, error: "Peer not found" })
      }

      // 전역 프로듀서 맵에서 해당 producerId로 프로듀서 조회
      const producer = this.globalProducers.get(producerId)
      if (!producer) {
        console.warn(`[${socket.id}] consume failed: Producer not found -> ${producerId}`)
        return callback({ ok: false, error: "Cannot consume" })
      }

      // 라우터가 해당 프로듀서를 소비할 수 있는지 확인
      if (!WorkerManager.router.canConsume({ producerId, rtpCapabilities })) {
        console.warn(`[${socket.id}] cannot consume producerId=${producerId}`)
        return callback({ ok: false, error: "Cannot consume" })
      }

      try {
        // 요청받은 트랜스포트에서 소비자(Consumer) 생성
        const transport = peerData.transports.get(transportId)
        if (!transport) {
          console.warn(`[${socket.id}] consume failed: Transport not found -> ${transportId}`)
          return callback({ ok: false, error: `Transport not found: ${transportId}` })
        }

        const consumer = await transport.consume({
          producerId,
          rtpCapabilities,
          paused: true,
        })

        // 소비자 생성 후 바로 재개(resume) 요청
        await consumer.resume()

        // 소비자 정보를 피어 객체에 저장
        peerData.consumers.set(consumer.id, consumer)
        console.log(`[${socket.id}] Consumer created -> id=${consumer.id}`)

        // 소비자 이벤트 핸들러 등록
        consumer.on("transportclose", () => {
          console.log(`[${socket.id}] Consumer closed (transportclose) -> ${consumer.id}`)
          peerData.consumers.delete(consumer.id)
        })

        consumer.on("producerclose", () => {
          console.log(`[${socket.id}] Consumer closed (producerclose) -> ${consumer.id}`)
          peerData.consumers.delete(consumer.id)
          socket.emit("producerClosed", { producerId })
        })

        callback({
          ok: true,
          params: {
            id: consumer.id,
            producerId,
            kind: consumer.kind,
            rtpParameters: consumer.rtpParameters,
          },
        })
      } catch (err) {
        console.error(`[${socket.id}] consume error:`, err)
        callback({ ok: false, error: err.message })
      }
    })

    // 8) 클라이언트가 채팅 메시지 전송 요청
    socket.on("chatMessage", ({ message }, callback) => {
      console.log(`[${socket.id}] chatMessage: ${message}`)
      if (!peer.roomId) {
        return callback({ ok: false, error: "방에 입장하지 않았습니다." })
      }
      const room = this.rooms.get(peer.roomId)
      if (!room) {
        return callback({ ok: false, error: "방을 찾을 수 없습니다." })
      }
      // 해당 방의 모든 피어에게 채팅 메시지를 전송 (자신 제외)
      room.forEach((peerId) => {
        if (peerId !== socket.id) {
          const otherPeer = this.peers.get(peerId)
          if (otherPeer) {
            otherPeer.socket.emit("newChatMessage", {
              peerId: socket.id,
              message,
            })
          }
        }
      })
      callback({ ok: true })
    })

    // 9) 클라이언트 연결 종료 시 (소켓 자체가 종료되는 경우)
    socket.on("disconnect", () => {
      console.log(`[${socket.id}] disconnected`)
      this.cleanupPeer(socket.id)
    })
  }

  // 피어 정리 (연결 종료 시 호출)
  cleanupPeer(socketId) {
    const peer = this.peers.get(socketId)
    if (!peer) return

    // 해당 피어가 속한 방에서 제거 및 방이 비면 삭제
    if (peer.roomId) {
      const room = this.rooms.get(peer.roomId)
      if (room) {
        room.delete(socketId)
        if (room.size === 0) {
          this.rooms.delete(peer.roomId)
        } else {
          room.forEach((pid) => {
            const otherPeer = this.peers.get(pid)
            otherPeer.socket.emit("peerClosed", { peerId: socketId })
          })
        }
      }
    }

    // 피어가 사용 중인 트랜스포트 종료
    peer.transports.forEach((transport) => {
      console.log(`[${socketId}] closing transport -> ${transport.id}`)
      transport.close()
    })

    // 글로벌 Producer 맵에서 해당 피어의 프로듀서 제거
    peer.producers.forEach((producer, producerId) => {
      this.globalProducers.delete(producerId)
    })

    // 최종적으로 피어 삭제
    this.peers.delete(socketId)
  }
}

const socketHandler = new SocketHandler()
io.on("connection", (socket) => socketHandler.handleConnection(socket))

// 서버 시작
async function start() {
  await WorkerManager.initialize()
  const port = process.env.PORT || 4000
  httpsServer.listen(port, () => {
    console.log(`Server running at https://${process.env.ANNOUNCED_IP}:${port}`)
  })
}

start()
