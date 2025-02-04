package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.tempUser.User;
import S12P11D110.ssacle.domain.tempUser.userController;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserConditionDTO {
    private String userId;
    private List<User.Topics> topics;
    private List<User.MeetingDays> meetingDays;


}
