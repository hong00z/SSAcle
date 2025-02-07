package com.example.firstproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.firstproject.databinding.ActivityMainBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

const val TAG = "MainActivity_TAG"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var chatSocket: Socket
    val STUDY_ID = "67a43b80a9314e94fbb7f8f8" // 스터디 1
    val USER_ID = "67a43b80a9314e94fbb7f8ee" // 유저2_스터디1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            chatSocket = IO.socket("http://127.0.0.1:4001")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        chatSocket.on(Socket.EVENT_CONNECT, onConnect)
        chatSocket.on("newMessage", onNewMessage)
        chatSocket.on("error", onError)
        chatSocket.connect()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

    }

    // 연결 성공 시 처리
    private val onConnect = Emitter.Listener {
        runOnUiThread {
            // 예: 채팅방에 입장(joinRoom)
            joinRoom(STUDY_ID, USER_ID)
        }
    }

    // 새 메시지 수신 이벤트 처리
    private val onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            if (args.isNotEmpty() && args[0] is JSONObject) {
                val messageJson = args[0] as JSONObject
                // 전달된 메시지 데이터를 파싱하여 UI 업데이트 (예: 리사이클러뷰 갱신)
                val senderId = messageJson.optString("userId")
                val nickname = messageJson.optString("nickname")
                val message = messageJson.optString("message")
                // UI 업데이트 코드 추가
            }
        }
    }

    // 에러 이벤트 처리
    private val onError = Emitter.Listener { args ->
        runOnUiThread {
            // 에러 메시지 처리 (예: 토스트로 표시)
        }
    }

    // 채팅방 입장 이벤트 전송
    private fun joinRoom(studyId: String, userId: String) {
        val data = JSONObject().apply {
            put("studyId", studyId)
            put("userId", userId)
        }
        chatSocket.emit("joinRoom", data)
    }

    // 메시지 전송 메서드
    private fun sendMessage(studyId: String, userId: String, message: String) {
        val data = JSONObject().apply {
            put("studyId", studyId)
            put("userId", userId)
            put("message", message)
        }
        chatSocket.emit("sendMessage", data)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatSocket.disconnect()
        chatSocket.off(Socket.EVENT_CONNECT, onConnect)
        chatSocket.off("newMessage", onNewMessage)
        chatSocket.off("error", onError)
    }
}