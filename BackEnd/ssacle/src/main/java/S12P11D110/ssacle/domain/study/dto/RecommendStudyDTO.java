package S12P11D110.ssacle.domain.study.dto;


import S12P11D110.ssacle.domain.study.entity.Study;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


// 스터디를 찾는 유저에게 보여지는 추천 스터디
@Data
@AllArgsConstructor
public class RecommendStudyDTO {
    private String studyId;
    private double similarity;
    private String studyName;
    private List<Study.Topic> topic;
    private List<Study.MeetingDays> meetingDays;

}
