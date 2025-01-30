package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.tempUser.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserDTO {
    private String userId;
    private String nickName;
    private List<User.Topic> topic;
    private List<User.MeetingDay> meetingDay;
    private Set<String> joinedStudies;

}
