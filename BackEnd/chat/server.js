// dotenv 패키지로 .env 파일 로드
require("dotenv").config()

const express = require("express")
const fs = require("fs")
const https = require("https")
const { Server } = require("socket.io")
const mongoose = require("mongoose")

const swaggerUi = require("swagger-ui-express")
const swaggerSpec = require("./swagger")

const admin = require("firebase-admin")
const serviceAccount = require(process.env.FIREBASE_SERVICE_ACCOUNT_KEY)

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
})
console.log("Firebase Admin 초기화 완료")

const app = express()

// SSL 인증서 및 HTTPS 서버 생성
const httpsOptions = {
  key: fs.readFileSync(process.env.SSL_KEY_PATH),
  cert: fs.readFileSync(process.env.SSL_CERT_PATH),
}
const httpsServer = https.createServer(httpsOptions, app)
const io = new Server(httpsServer)

// 모델(스키마) 불러오기
const Study = require("./models/Study")
const Message = require("./models/Message")
const User = require("./models/User")

// 환경 변수에서 포트와 MongoDB 연결 URI 읽어오기
const PORT = process.env.PORT || 4001
const MONGO_URI = process.env.MONGO_URI

// 환경 변수 디버깅 로그
console.log("환경 변수 로드 완료:")
console.log(`PORT: ${PORT}`)
console.log(`MONGO_URI: ${MONGO_URI}`)
console.log(`ANNOUNCED_IP: ${process.env.ANNOUNCED_IP}`)

// MongoDB 연결
mongoose
  .connect(MONGO_URI)
  .then(() => console.log("MongoDB 연결 성공"))
  .catch((err) => console.error("MongoDB 연결 에러:", err))

app.use(express.static("public"))
app.use(express.json()) // JSON 요청 본문 파싱 미들웨어

// // Swagger UI 설정
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec))

// 라우트 모듈 연결
const userRouter = require("./routes/user")
app.use("/api/users", userRouter)

const messageRouter = require("./routes/message")
app.use("/api/chat", messageRouter)

const studyRouter = require("./routes/study")
app.use("/api/studies", studyRouter)

// Socket.IO 예제: 클라이언트와의 실시간 통신
io.on("connection", (socket) => {
  console.log("새 클라이언트 연결:", socket.id)

  socket.on("joinRoom", async ({ studyId, userId }) => {
    const study = await Study.findById(studyId)
    if (!study) {
      console.error("sendMessage 실패: 채팅방을 찾을 수 없음", studyId)
      return socket.emit("error", { error: "채팅방을 찾을 수 없습니다." })
    }
    if (!study.members.includes(userId)) {
      console.error("sendMessage 실패: 발신자가 채팅방 멤버가 아님", userId)
      return socket.emit("error", { error: "채팅방 멤버만 메시지를 보낼 수 있습니다." })
    }
    socket.userId = userId
    socket.join(studyId)
    console.log(`사용자 ${userId}가 채팅방 ${studyId}에 입장 완료`)
  })

  socket.on("sendMessage", async ({ studyId, userId, message }) => {
    if (!studyId || !userId || !message) {
      console.error("sendMessage 실패: 필수 필드 누락")
      return
    }

    try {
      const study = await Study.findById(studyId)
      if (!study) {
        console.error("sendMessage 실패: 채팅방을 찾을 수 없음", studyId)
        return socket.emit("error", { error: "채팅방을 찾을 수 없습니다." })
      }
      if (!study.members.includes(userId)) {
        console.error("sendMessage 실패: 발신자가 채팅방 멤버가 아님", userId)
        return socket.emit("error", { error: "채팅방 멤버만 메시지를 보낼 수 있습니다." })
      }

      // 보낸 사람 닉네임 찾기
      const user = await User.findById(userId)
      if (!user) {
        console.error("sendMessage 실패: 발신자 사용자를 찾을 수 없음", userId)
        return socket.emit("error", { error: "발신자 사용자를 찾을 수 없습니다." })
      }
      const nickname = user.nickname

      // 메시지 생성 및 저장
      const newMsg = new Message({ studyId, userId, nickname, message, isInOut: false })
      await newMsg.save()
      console.log(`\n메시지 저장 성공: \nchatRoomId = ${studyId}\nsenderId = ${userId}\nmessage = ${message}`)

      // 스터디 내 모든 클라이언트에게 메시지 브로드캐스트
      io.to(studyId).emit("newMessage", newMsg)

      // FCM 알림 전송: study.members에 해당하는 사용자들의 FCM 토큰 조회
      // 1. 현재 채팅방(studyId)에 연결되어 있는 소켓들의 userId 목록 수집
      const room = io.sockets.adapter.rooms.get(studyId)

      let connectedUserIds = []
      if (room) {
        connectedUserIds = Array.from(room)
          .map((socketId) => {
            const tmpSocket = io.sockets.sockets.get(socketId)
            return tmpSocket ? tmpSocket.userId : null
          })
          .filter((id) => id !== null)
      }
      console.log("현재 채팅방에 연결된 사용자들:", connectedUserIds)

      const users = await User.find({ _id: { $in: study.members } })
      // 각 사용자에서 fcmToken이 존재하는 값만 추출
      const fcmTokens = users
        .filter((user) => !connectedUserIds.includes(user._id.toString()))
        .map((user) => user.fcmToken)
        .filter((fcmToken) => !!fcmToken)

      console.log("FCM 토큰 목록:", fcmTokens)

      if (fcmTokens.length > 0) {
        const fcmMessage = {
          tokens: fcmTokens,
          notification: {
            title: `${nickname}님이 메시지를 보냈습니다.`,
            body: message,
          },
        }

        admin
          .messaging()
          .sendEachForMulticast(fcmMessage)
          .then((response) => {
            console.log("FCM 알림 전송 성공:", response)
          })
          .catch((error) => {
            console.error("FCM 알림 전송 에러:", error)
          })
      }
    } catch (err) {
      console.error("sendMessage 중 에러 발생:", err)
      socket.emit("error", { error: err.message })
    }
  })

  socket.on("disconnect", () => {
    console.log("클라이언트 연결 종료:", socket.id)
  })
})

// 서버 실행
httpsServer.listen(PORT, () => console.log(`Server running at https://${process.env.ANNOUNCED_IP}:${PORT}`))
