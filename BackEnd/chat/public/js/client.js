// Socket.IO 클라이언트 모듈 임포트
import io from "socket.io-client"

// 기본적으로 같은 도메인/포트를 사용하여 서버와 연결합니다.
const socket = io()
console.log("Socket.IO client initialized.")

// DOM 요소 참조
const joinRoomBtn = document.getElementById("joinRoom")
const sendMessageBtn = document.getElementById("sendMessage")
const studyIdInput = document.getElementById("studyId")
const userIdInput = document.getElementById("userId")
const messageInput = document.getElementById("messageInput")
const messagesDiv = document.getElementById("messages")
const studyListUl = document.getElementById("studyList")
const refreshStudiesBtn = document.getElementById("refreshStudies")

// 스터디 입장 버튼 클릭 시 (방에 입장하고 이전 메시지 불러오기)
joinRoomBtn.addEventListener("click", () => {
  const studyId = studyIdInput.value.trim()
  const userId = userIdInput.value.trim()
  if (!studyId || !userId) {
    alert("스터디 ID와 사용자 ID를 모두 입력하세요.")
    return
  }
  // Socket.IO를 통해 스터디(채팅방)에 입장
  socket.emit("joinRoom", { studyId, userId })
  console.log(`사용자 ${userId}가 스터디 ${studyId}에 입장 요청`)
  // 입장 후 해당 스터디의 이전 메시지 불러오기
  loadPreviousMessages(studyId)
})

// 메시지 전송 버튼 클릭 시
sendMessageBtn.addEventListener("click", () => {
  const studyId = studyIdInput.value.trim()
  const userId = userIdInput.value.trim()
  const message = messageInput.value.trim()
  if (!studyId || !userId || !message) {
    alert("스터디 ID, 사용자 ID, 메시지를 모두 입력하세요.")
    return
  }
  // Socket.IO를 통해 메시지 전송 (서버의 sendMessage 이벤트 호출)
  socket.emit("sendMessage", { studyId, userId, message })
  console.log(`메시지 전송 요청 - studyId: ${studyId}, userId: ${userId}, content: ${message}`)
  messageInput.value = ""
})

// 새로고침 버튼에 이벤트 리스너 추가
refreshStudiesBtn.addEventListener("click", loadStudyList)
loadStudyList()

// 서버로부터 새 메시지 이벤트 수신
socket.on("newMessage", (msg) => {
  console.log("Received newMessage:", msg)
  displayMessage(msg)
})

// 메시지 표시 함수: 새로운 <p> 요소를 생성해 메시지 영역에 추가
function displayMessage(msg) {
  const p = document.createElement("p")
  // createdAt은 ISO 문자열 형태일 수 있으므로 Date 객체로 변환
  const time = new Date(msg.createdAt).toLocaleTimeString()
  // msg.message는 메시지 내용, msg.userId는 발신자 ID입니다.
  p.textContent = `[${time}] ${msg.userId} - ${msg.nickname}: ${msg.message}`
  messagesDiv.appendChild(p)
  messagesDiv.scrollTop = messagesDiv.scrollHeight // 스크롤을 맨 아래로
}

// 이전 메시지 불러오기 함수 (REST API 호출)
function loadPreviousMessages(studyId) {
  fetch(`/api/chat/${studyId}/messages`)
    .then((response) => response.json())
    .then((data) => {
      messagesDiv.innerHTML = "" // 기존 메시지 초기화
      data.forEach((msg) => {
        displayMessage(msg)
      })
    })
    .catch((error) => {
      console.error("Error loading previous messages:", error)
    })
}

// 스터디 목록 불러오기 함수
function loadStudyList() {
  fetch("/api/studies")
    .then((response) => response.json())
    .then((studies) => {
      studyListUl.innerHTML = "" // 기존 목록 초기화
      studies.forEach((study) => {
        const li = document.createElement("li")
        // 예: 스터디 이름과 스터디 ID를 함께 표시
        li.textContent = `${study.studyName} (ID: ${study._id})`
        // 클릭 시 스터디 ID를 studyId input에 자동 입력하는 기능 등 추가 가능
        li.addEventListener("click", () => {
          document.getElementById("studyId").value = study._id
        })
        studyListUl.appendChild(li)
      })
      console.log("스터디 목록 불러오기 완료:", studies)
    })
    .catch((error) => {
      console.error("스터디 목록 불러오기 에러:", error)
    })
}
