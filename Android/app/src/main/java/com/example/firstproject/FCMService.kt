package com.example.firstproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firstproject.MyApplication.Companion.USER_ID
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.client.RetrofitClient
import com.example.firstproject.dto.TokenUpdateRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService_TAG"
        private const val CHANNEL_ID = "fcm_channel"  // 알림 채널 ID
    }

    /**
     * FCM 메시지 수신 시 호출되는 메서드.
     * 데이터 페이로드와 알림 페이로드를 모두 처리할 수 있습니다.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "메시지를 보낸 출처: ${remoteMessage.from}")

        // 데이터 페이로드가 있는 경우 처리
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "데이터 페이로드: ${remoteMessage.data}")
            // 데이터 처리 로직 추가
            val title = remoteMessage.data["title"] ?: "FCM 데이터 메시지"
            val message = remoteMessage.data["message"] ?: "새로운 데이터 메시지를 수신했습니다."
            showNotification(title, message)
        }

        // 알림 페이로드가 있는 경우 처리
        remoteMessage.notification?.let {
            Log.d(TAG, "알림 메시지 본문: ${it.body}")
            // 알림 처리 로직 추가
            val title = it.title ?: "FCM 알림 메시지"
            val message = it.body ?: "새로운 알림 메시지를 수신했습니다."
            showNotification(title, message)
        }
    }

    /**
     * FCM 등록 토큰이 갱신될 때 호출됩니다.
     * 새 토큰을 서버에 전송하는 로직을 구현할 수 있습니다.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "새로운 토큰: $token")
        // 토큰 서버 전송 로직 추가

        tokenManager.saveFcmToken(token)
    }

    /**
     * 로컬 알림을 생성하여 사용자에게 표시합니다.
     * @param title 알림 제목
     * @param message 알림 메시지 내용
     */
    private fun showNotification(title: String, message: String) {
        // 사용자가 알림을 탭했을 때 실행할 액티비티 지정 (예: MainActivity)
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 구성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // 앱 내 알림 아이콘 (리소스 파일 추가 필요)
            .setContentTitle(title).setContentText(message).setAutoCancel(true)
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)

        // NotificationManager를 사용해 알림 생성
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android Oreo(API 26) 이상은 채널을 생성해야 함
        val channelName = "FCM 알림 채널"
        val channel = NotificationChannel(
            CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // 알림 ID 0을 사용하여 알림 표시
        notificationManager.notify(0, notificationBuilder.build())
    }

    /**
     * 새 토큰을 서버에 전송하는 메서드.
     */
    private fun sendRegistrationToServer(fcmToken: String) {
        // 서버 API 호출 코드 또는 로컬 저장 등의 로직 구현
        val userId = USER_ID

        // Retrofit 호출은 IO 스레드에서 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // RetrofitClient와 ApiService는 미리 구성한 객체로 가정합니다.
                val response = RetrofitClient.userService.updateFcmToken(
                    userId, TokenUpdateRequest(fcmToken)
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "서버에 토큰 등록 성공: ${response.body()}")
                } else {
                    Log.e(TAG, "서버 토큰 등록 실패, 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "서버에 토큰 전송 중 오류 발생: ${e.message}", e)
            }
        }
    }
}