// dotenv 패키지로 .env 파일 로드
require("dotenv").config()

const express = require("express")
const http = require("http")
const socketIo = require("socket.io")
const mongoose = require("mongoose")

const app = express()
const server = http.createServer(app)
const io = socketIo(server)

app.use(express.static("public"))
app.use(express.json()) // JSON 요청 본문을 파싱하도록 추가

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

// 간단한 스키마 예제 (ChatRoom, Message)
const ChatRoomSchema = new mongoose.Schema({
  name: { type: String, default: "채팅방" },
  isGroup: { type: Boolean, default: false },
  members: [{ type: String, required: true }],
})

const MessageSchema = new mongoose.Schema({
  chatRoomId: { type: mongoose.Schema.Types.ObjectId, ref: "ChatRoom", required: true },
  senderId: { type: String, required: true },
  content: { type: String, required: true },
  createdAt: { type: Date, default: Date.now },
})

const ChatRoom = mongoose.model("ChatRoom", ChatRoomSchema)
const Message = mongoose.model("Message", MessageSchema)

// REST API 예제: 채팅방 생성
app.post("/chatrooms", async (req, res) => {
  console.log("POST /chatrooms 요청 수신")
  console.log("요청 본문:", req.body)

  try {
    const { name, isGroup, members } = req.body

    if (!members || !Array.isArray(members) || members.length < 2) {
      console.error("채팅방 생성 실패: 최소 2명의 멤버 필요")
      return res.status(400).json({ error: "최소 2명의 멤버가 필요합니다." })
    }
    if (!isGroup && members.length !== 2) {
      console.error("채팅방 생성 실패: 갠톡(1:1)은 2명의 멤버만 가능")
      return res.status(400).json({ error: "갠톡은 2명의 멤버만 가능합니다." })
    }

    const chatRoom = new ChatRoom({
      name: name || (isGroup ? "그룹 채팅" : "개인 채팅"),
      isGroup,
      members,
    })
    await chatRoom.save()
    console.log("채팅방 생성 성공:", chatRoom)
    res.status(201).json(chatRoom)
  } catch (err) {
    console.error("채팅방 생성 중 에러 발생:", err)
    res.status(500).json({ error: err.message })
  }
})

// Socket.IO 예제: 클라이언트와의 실시간 통신
io.on("connection", (socket) => {
  console.log("새 클라이언트 연결:", socket.id)

  socket.on("joinRoom", ({ chatRoomId, userId }) => {
    socket.join(chatRoomId)
    console.log(`사용자 ${userId}가 채팅방 ${chatRoomId}에 입장 완료`)
  })

  socket.on("sendMessage", async ({ chatRoomId, senderId, content }) => {
    if (!chatRoomId || !senderId || !content) {
      console.error("sendMessage 실패: 필수 필드 누락")
      return
    }

    try {
      const chatRoom = await ChatRoom.findById(chatRoomId)
      if (!chatRoom) {
        console.error("sendMessage 실패: 채팅방을 찾을 수 없음", chatRoomId)
        return socket.emit("error", { error: "채팅방을 찾을 수 없습니다." })
      }
      if (!chatRoom.members.includes(senderId)) {
        console.error("sendMessage 실패: 발신자가 채팅방 멤버가 아님", senderId)
        return socket.emit("error", { error: "채팅방 멤버만 메시지를 보낼 수 있습니다." })
      }

      const message = new Message({ chatRoomId, senderId, content })
      await message.save()
      console.log(`\n메시지 저장 성공: \nchatRoomId = ${chatRoomId}\nsenderId = ${senderId}\ncontent = ${content}`)

      io.to(chatRoomId).emit("newMessage", message)
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
