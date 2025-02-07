package S12P11D110.ssacle.domain.tempUser;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document(collection = "users")
@Data
public class TempUser {
    @Id
    private String id;
    private String nickname;
    private int ssafyNum;
    private List<Topics> topics;
    private List<MeetingDays> meetingDays;
    private Set<String> joinedStudies;
    private Set<String> wishStudies;
    private Set<String> invitedStudies;

    // 토큰은 로그인 기능 완성된 후 추가하기
//    private String accessToken;
//    private String refreshToken;

    public enum Topics{
        cs, algorithm
    }

    public enum MeetingDays{
        MON, TUE, WED, THU, FRI, SAT, SUN
    }

}
