package S12P11D110.ssacle.domain.study.dto;

import lombok.Data;
import java.util.Set;

@Data
public class StudyUpdateRequestDTO {

    private String studyName;                   // 스터디 이름
    private String topic;            // 주제 목록
    private Set<String> meetingDays;  // 모임 요일
    private int count;                          //정원
    private String studyContent;                // 스터디 소개

}
