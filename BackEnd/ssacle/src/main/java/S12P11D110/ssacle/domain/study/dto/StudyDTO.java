package S12P11D110.ssacle.domain.study.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
//스터디 정보를 담은 DTO
public class StudyDTO {
    private String studyId;
    private String studyName;
    private String topic;
    private Set<String> meetingDays;
    private int count;
    private Set<String> members;
    private Set<String> wishMembers;
    private Set<String> preMembers;
}
