package S12P11D110.ssacle.domain.user.entity;


import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor               // GPT 도움!! (MongoDB에서 필요)
@AllArgsConstructor              // GPT 도움!! (모든 필드를 포함한 생성자 추가)
@Builder
@Document(collection = "users")  // GPT 도움!! (MongoDB 컬렉션 이름 지정)
public class User extends BaseEntity {
//------------------------------------------- << 필드 >> -------------------------------------------
    @Id
    private String userId;  // GPT 도움!! (MongoDB에서는 String 타입으로 ID를 관리할 수 있음)
    // 카카오 로그인 후 받아옴
    private String email;
    private String nickname;
    // 기본값 설정
    @Builder.Default
    private String image = "";

    // 싸피생 인증 후 받아옴 (기본값 설정)
    @Builder.Default
    private String term = "미인증";
    @Builder.Default
    private String campus = "미인증";
    @Builder.Default
    private UserRole role = UserRole.USER;

    // 스터디 프로필 (기본값 설정)
    @Builder.Default
    private Set<String> topics = new HashSet<>();
    @Builder.Default
    private Set<String> meetingDays = new HashSet<>();

    // 스터디 정보 (기본값 설정)
    @Builder.Default
    private Set<String> createdStudies = new HashSet<>();
    @Builder.Default
    private Set<String> joinedStudies = new HashSet<>();
    @Builder.Default
    private Set<String> wishStudies = new HashSet<>();
    @Builder.Default
    private Set<String> invitedStudies = new HashSet<>();

    // FCM 토큰
    @Builder.Default
    private String
            fcmToken = "";
}
