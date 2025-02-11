package S12P11D110.ssacle.domain.tempUser;


import S12P11D110.ssacle.domain.user.entity.UserRole;
import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor               // GPT 도움!! (MongoDB에서 필요)
@AllArgsConstructor              // GPT 도움!! (모든 필드를 포함한 생성자 추가)
@Builder
@Document(collection = "users")  // GPT 도움!! (MongoDB 컬렉션 이름 지정)
public class TempUser extends BaseEntity {
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

    // 프로필 (기본값 설정)
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
    private String fcmToken = "";




    //------------------------------------------- << 메서드 >> -------------------------------------------
    // 사용자 계정 생성
    public TempUser(String email, String nickname) {
        this.userId = UUID.randomUUID().toString();
        this.email = email;
        this.nickname = nickname;
        this.image = "";
        this.term = "미인증";
        this.campus = "미인증";
        this.role = UserRole.USER;
        this.topics = new HashSet<>();
        this.meetingDays = new HashSet<>();
        this.createdStudies = new HashSet<>();
        this.joinedStudies = new HashSet<>();
        this.wishStudies = new HashSet<>();
        this.invitedStudies = new HashSet<>();
    }

    // 프로필 수정
    public void updateProfile(String nickname, String image, Set<String> topics, Set<String> meetingDays) {
        // 기존 필드를 유지하면서 null이 아닌 값만 업데이트
        if (nickname != null) this.nickname = nickname;
        if (image != null) this.image = image;
        if (topics != null) this.topics = topics;
        if (meetingDays != null) this.meetingDays = meetingDays;
    }


}
