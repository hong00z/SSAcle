// Socket.IO 클라이언트 모듈 임포트
import io from "socket.io-client"

// 기본적으로 같은 도메인/포트를 사용하여 서버와 연결합니다.
const socket = io()
console.log("Socket.IO client initialized.")

// 기존 DOM 요소 참조
const joinRoomBtn = document.getElementById("joinRoom")
const sendMessageBtn = document.getElementById("sendMessage")
const roomIdInput = document.getElementById("roomId")
const senderIdInput = document.getElementById("senderId")
const messageInput = document.getElementById("messageInput")
const chatDiv = document.getElementById("chat")

// 신규: 채팅방 생성 폼 관련 요소
const createChatRoomForm = document.getElementById("createChatRoomForm")
const chatRoomNameInput = document.getElementById("chatRoomName")
const isGroupCheckbox = document.getElementById("isGroup")
const membersInput = document.getElementById("members")
const chatRoomResultDiv = document.getElementById("chatRoomResult")

console.log("DOM elements loaded.")

/**
 * 채팅방 생성 폼 제출 이벤트 처리
 * - 폼에 입력한 정보를 바탕으로 REST API의 POST /chatrooms 엔드포인트 호출
 * - 생성된 채팅방 정보를 화면에 출력합니다.
 */
createChatRoomForm.addEventListener("submit", async (e) => {
  console.log("Create Chat Room form submitted.")
  e.preventDefault() // 폼 제출 시 페이지 리로딩 방지

  const name = chatRoomNameInput.value.trim()
  const isGroup = isGroupCheckbox.checked
  // 쉼표로 구분된 문자열을 배열로 변환 (빈 값은 제거)
  const members = membersInput.value
    .split(",")
    .map((str) => str.trim())
    .filter((str) => str !== "")

  console.log("Chat Room creation data:", { name, isGroup, members })

  if (members.length < 2) {
    alert("최소 2명의 멤버 정보를 입력해야 합니다.")
    return
  }
  if (!isGroup && members.length !== 2) {
    alert("갠톡(1:1 채팅)은 정확히 2명의 멤버 정보가 필요합니다.")
    return
  }

  try {
    const response = await fetch("/chatrooms", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ name, isGroup, members }),
    })
    console.log("Fetch response received:", response)
    const result = await response.json()
    console.log("Fetch result:", result)
    if (!response.ok) {
      chatRoomResultDiv.innerHTML = `<p style="color:red;">에러: ${result.error}</p>`
      console.error("Error creating chat room:", result.error)
    } else {
      chatRoomResultDiv.innerHTML = `<p style="color:green;">채팅방 생성 성공! ID: ${result._id}, 이름: ${result.name}</p>`
      console.log("Chat room created successfully:", result)
      // 필요에 따라 생성 후 자동 참가 등의 추가 로직을 구현할 수 있습니다.
    }
  } catch (error) {
    chatRoomResultDiv.innerHTML = `<p style="color:red;">에러 발생: ${error.message}</p>`
    console.error("Fetch error:", error)
  }
})

/**
 * 채팅방 참가 버튼 클릭 이벤트
 * - 입력한 채팅방 ID와 사용자 ID를 사용해 서버에 'joinRoom' 이벤트 전송
 */
joinRoomBtn.addEventListener("click", () => {
  console.log("Join Room button clicked.")
  const roomId = roomIdInput.value.trim()
  const senderId = senderIdInput.value.trim()
  console.log("Attempting to join room with data:", { roomId, senderId })
  if (!roomId || !senderId) {
    alert("채팅방 ID와 사용자 ID를 모두 입력하세요.")
    return
  }
  socket.emit("joinRoom", { chatRoomId: roomId, userId: senderId })
  console.log("joinRoom event emitted.")
  appendMessage(`채팅방 ${roomId}에 참가하였습니다.`)
})

/**
 * 메시지 전송 버튼 클릭 이벤트
 * - 입력한 정보를 바탕으로 서버에 'sendMessage' 이벤트 전송
 */
sendMessageBtn.addEventListener("click", () => {
  console.log("Send Message button clicked.")
  const roomId = roomIdInput.value.trim()
  const senderId = senderIdInput.value.trim()
  const content = messageInput.value.trim()
  console.log("Message data:", { roomId, senderId, content })
  if (!roomId || !senderId || !content) {
    alert("채팅방 ID, 사용자 ID, 메시지를 모두 입력하세요.")
    return
  }
  socket.emit("sendMessage", { chatRoomId: roomId, senderId, content })
  console.log("sendMessage event emitted.")
  messageInput.value = ""
})

/**
 * 서버로부터 'newMessage' 이벤트를 수신하면 메시지를 화면에 출력합니다.
 */
socket.on("newMessage", (message) => {
  console.log("Received newMessage event:", message)
  const { senderId, content, createdAt } = message
  const time = new Date(createdAt).toLocaleTimeString()
  appendMessage(`[${time}] ${senderId}: ${content}`)
})

/**
 * 서버 에러 발생 시 알림창으로 표시합니다.
 */
socket.on("error", (error) => {
  console.error("Received error event:", error)
  alert(`에러: ${error.error}`)
})

/**
 * 채팅 영역에 메시지를 추가하는 헬퍼 함수입니다.
 * @param {string} text - 출력할 메시지 텍스트
 */
function appendMessage(text) {
  console.log("Appending message:", text)
  const p = document.createElement("p")
  p.textContent = text
  chatDiv.appendChild(p)
  chatDiv.scrollTop = chatDiv.scrollHeight
}
