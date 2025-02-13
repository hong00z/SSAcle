package S12P11D110.ssacle.domain.study.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;


// 스터디원을 찾는 스터디에게 보여지는 추천 유저
@Data
@Builder
@AllArgsConstructor
public class RecommendUser {
    private String userId;
    private double similarity;
    private String nickname;
    private String image;
    private String term;
    private String campus;
    private int countJoinedStudies;
    private Set<String> topics;
    private Set<String> meetingDays;

}
