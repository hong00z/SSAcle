package S12P11D110.ssacle.domain.tempUser;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String userId;
    private String nickname;
    private int ssafyNum;
    private List<Topic> topic;
    private List<MeetingDay> meetingDay;
    private Set<String> joinedStudies;
    private Set<String> wishStudy;
    private Set<String> invitedStudy;

    // 토큰은 로그인 기능 완성된 후 추가하기
//    private String accessToken;
//    private String refreshToken;

    public enum Topic{
        cs, algorithm
    }

    public enum MeetingDay{
        MON, TUE, WED, THU, FRI, SAT, SUN
    }
}
