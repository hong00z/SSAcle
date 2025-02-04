package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.study.entity.Study;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyConditionDTO {
    private String id;
    private List<Study.Topic> topic;
    private List<Study.MeetingDays> meetingDays;
}
