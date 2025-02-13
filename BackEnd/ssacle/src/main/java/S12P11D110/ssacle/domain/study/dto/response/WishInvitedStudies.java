package S12P11D110.ssacle.domain.study.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class WishInvitedStudies {
    private String studyId;
    private String studyName;
    private String topic;
    private Set<String> meetingDays;
    private int count;
    private Set<String> members;

}
