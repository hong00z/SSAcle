package S12P11D110.ssacle.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;


// 스터디를 찾는 유저에게 보여지는 추천 스터디
@Data
@Builder
@AllArgsConstructor
public class RecoStudyResult {
    private String studyId;
    private double similarity;
    private String studyName;
    private String topic;
    private Set<String> meetingDays;
    private int count;
    private Set<String> members;
    private  String createdBy;

}
