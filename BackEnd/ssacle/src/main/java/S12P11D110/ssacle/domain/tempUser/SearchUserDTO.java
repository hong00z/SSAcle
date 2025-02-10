package S12P11D110.ssacle.domain.tempUser;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SearchUserDTO {
    private String userId;
    private String nickName;
    private Set<String> topics;
    private Set<String> meetingDays;
    private Set<String> joinedStudies;
    private Set<String> wishStudies;
    private Set<String> invitedStudies;

}