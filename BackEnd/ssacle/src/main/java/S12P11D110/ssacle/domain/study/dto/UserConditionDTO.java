package S12P11D110.ssacle.domain.study.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserConditionDTO {
    private String userId;
    private Set<String> topics;
    private Set<String> meetingDays;


}
