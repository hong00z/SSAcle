package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.tempUser.TempUser;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserConditionDTO {
    private String userId;
    private List<TempUser.Topics> topics;
    private List<TempUser.MeetingDays> meetingDays;


}
