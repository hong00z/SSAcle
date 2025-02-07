// dotenv 패키지로 .env 파일 로드
require("dotenv").config()

const express = require("express")
const http = require("http")
const socketIo = require("socket.io")
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
const server = http.createServer(app)
const io = socketIo(server)

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
app.use("/api/message", messageRouter)

const studyRouter = require("./routes/study")
app.use("/api/studies", studyRouter)

// // 예: 사용자 토큰 업데이트 엔드포인트
// app.post("/users/:userId/token", async (req, res) => {
//   const { userId } = req.params
//   const { fcmToken } = req.body
//   // 사용자 모델을 만들어 fcmToken 필드를 저장하거나 업데이트합니다.
//   console.log(`토큰 등록 요청: userId=${userId}, fcmToken=${fcmToken}`)
//   res.status(200).json({ message: "토큰 등록 성공" })
// })

// // 클라이언트에서 GET /api/users/:userId/studies 요청을 보내면 해당 사용자가 가입한 스터디 목록을 반환합니다.
// app.get("/api/users/:userId/studies", async (req, res) => {
//   const { userId } = req.params

//   try {
//     // Study 모델의 members 배열에 userId가 포함된 스터디들을 조회
//     const studies = await User.find({ members: userId })
//     console.log(`사용자 ${userId}가 가입한 스터디 조회 - 총 ${studies.length}개`)
//     res.status(200).json(studies)
//   } catch (err) {
//     console.error("가입한 스터디 조회 중 에러 발생:", err)
//     res.status(500).json({ error: err.message })
//   }
// })

// // 특정 채팅방의 이전 메시지 내역 조회 (생성 시간 기준 오름차순 정렬)
// app.get("/api/chat/:studyId/messages", async (req, res) => {
//   const { studyId } = req.params

//   // TODO jwt로 토큰 받아서 user 인증 후에 메시지 조회 기능 수행해야함

//   try {
//     const messages = await Message.find({ studyId }).sort({ createdAt: 1 })
//     console.log(`메시지 내역 조회 - 스터디 ID: ${studyId}, 조회된 메시지 수: ${messages.length}`)
//     res.json(messages)
//   } catch (err) {
//     console.error("메시지 조회 중 에러 발생:", err)
//     res.status(500).json({ error: err.message })
//   }
// })

// // 모든 스터디 목록 조회 엔드포인트
// app.get("/api/studies", async (req, res) => {
//   try {
//     // _id, studyName, members 필드만 반환 (필요에 따라 수정 가능)
//     const studies = await Study.find({})
//     console.log(`스터디 목록 조회 - 총 ${studies.length}개`)
//     res.json(studies)
//   } catch (err) {
//     console.error("스터디 목록 조회 중 에러 발생:", err)
//     res.status(500).json({ error: err.message })
//   }
// })

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
      const user = await User.findOne({ _id: userId })
      const nickname = user.nickname

      // 메시지 생성 및 저장
      const newMsg = new Message({ studyId, userId, nickname, message, isInOut: false })
      await newMsg.save()
      console.log(`\n메시지 저장 성공: \nchatRoomId = ${studyId}\nsenderId = ${userId}\nmessage = ${message}`)

      // 스터디 내 모든 클라이언트에게 메시지 브로드캐스트
      io.to(studyId).emit("newMessage", newMsg)

      // // FCM 알림 전송: study.members에 해당하는 사용자들의 FCM 토큰 조회
      // const users = await User.find({ _id: { $in: study.members } })
      // // 각 사용자에서 fcmToken이 존재하는 값만 추출
      // const fcmTokens = users.map((user) => user.fcmToken).filter((token) => token)

      // console.log("FCM 토큰 목록:", fcmTokens)

      // if (fcmTokens.length > 0) {
      //   const payload = {
      //     notification: {
      //       title: "새 메시지 도착",
      //       body: `${userId}님이 메시지를 보냈습니다.`,
      //     },
      //     data: {
      //       studyId: studyId,
      //       userId: userId,
      //     },
      //   }

      //   admin
      //     .messaging()
      //     .sendToDevice(fcmTokens, payload)
      //     .then((response) => {
      //       console.log("FCM 알림 전송 성공:", response)
      //     })
      //     .catch((error) => {
      //       console.error("FCM 알림 전송 에러:", error)
      //     })
      // }
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
server.listen(PORT, () => console.log(`Server running at http://${process.env.ANNOUNCED_IP}:${PORT}`))
