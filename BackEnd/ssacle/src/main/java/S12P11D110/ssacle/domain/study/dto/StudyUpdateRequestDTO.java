package S12P11D110.ssacle.domain.study.dto;


import S12P11D110.ssacle.domain.study.entity.Study;
import lombok.Data;

import java.util.List;

@Data
public class StudyUpdateRequestDTO {

    private String studyName;                   // 스터디 이름
    private List<Study.Topic> topic;            // 주제 목록
    private List<Study.MeetingDays> meetingDays;  // 모임 요일
    private int count;                          //정원
    private String studyContent;                // 스터디 소개

}
