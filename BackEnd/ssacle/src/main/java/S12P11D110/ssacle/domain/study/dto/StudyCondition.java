package S12P11D110.ssacle.domain.study.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class StudyCondition {
    private String id;
    private String topic;
    private Set<String> meetingDays;
}
