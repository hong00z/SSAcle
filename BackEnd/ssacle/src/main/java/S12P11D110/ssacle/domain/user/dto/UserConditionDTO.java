package S12P11D110.ssacle.domain.user.dto;

import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.global.entity.MeetingDay;
import S12P11D110.ssacle.global.entity.Topic;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class UserConditionDTO {
    private String userId;
    private Set<Topic> topics;
    private Set<MeetingDay> meetingDays;
}