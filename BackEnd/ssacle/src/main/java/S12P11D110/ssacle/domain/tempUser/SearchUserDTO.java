package S12P11D110.ssacle.domain.tempUser;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class SearchUserDTO {
    private String userId;
    private String nickName;
    private List<User.Topics> topics;
    private List<User.MeetingDays> meetingDays;
    private Set<String> joinedStudies;
    private Set<String> wishStudies;
    private Set<String> invitedStudies;

}