package S12P11D110.ssacle.domain.study.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;


// 스터디원을 찾는 스터디에게 보여지는 추천 유저
@Data
@AllArgsConstructor
public class RecommendUserDTO {
    private String userId;
    private double similarity;
    private String nickName;
    private Set<String> topics;
    private Set<String> meetingDays;
}
