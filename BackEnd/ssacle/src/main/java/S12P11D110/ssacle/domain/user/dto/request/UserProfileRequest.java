package S12P11D110.ssacle.domain.user.dto.request;


import S12P11D110.ssacle.global.entity.MeetingDay;
import S12P11D110.ssacle.global.entity.Topic;
import lombok.*;

import java.util.Set;

@Getter
public class UserProfileRequest {
    private String nickname;
    private String image;
    private Set<Topic> topics;
    private Set<MeetingDay> meetingDays;
}
