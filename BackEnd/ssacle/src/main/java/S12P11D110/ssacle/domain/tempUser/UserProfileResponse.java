package S12P11D110.ssacle.domain.tempUser;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class UserProfileResponse {
    private String nickname;
    private String image;
//    private String term;
//    private String campus;
    private List<User.Topics> topics;
    private List<User.MeetingDays> meetingDays;
}
