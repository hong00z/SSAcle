package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.study.entity.Study;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
//스터디 정보를 담은 DTO
public class StudyDTO {
    private String studyId;
    private String studyName;
    private List<Study.Topic> topic;
    private List<Study.MeetingDays> meetingDays;
    private int count;
    private Set<String> members;
    private Set<String> wishMembers;
    private Set<String> preMembers;
}
