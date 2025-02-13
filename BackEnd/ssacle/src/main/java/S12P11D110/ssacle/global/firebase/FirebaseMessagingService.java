package S12P11D110.ssacle.global.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    // FCM으로 사용자에게 보낼 메세지를 보냄
    public void sendNotification(String token, String title, String body){
        // Message = 상요자에게 보여질 메세지
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try{
            String response = firebaseMessaging.send(message);
            System.out.println("✅ FCM 메시지 전송 성공: " + response);

        }catch (FirebaseMessagingException e){
            throw new RuntimeException("FCM 메시지 전송 실패", e);
        }
    }






}
