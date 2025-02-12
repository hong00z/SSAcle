package S12P11D110.ssacle.domain.study.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import S12P11D110.ssacle.domain.study.dto.nicknameImage;

import java.util.List;
import java.util.Set;


// 스터디를 찾는 유저에게 보여지는 추천 스터디
@Data
@AllArgsConstructor
@Builder
public class RecommendStudyDTO {
    private String studyId;
    private double similarity;
    private String studyName;
    private String topic;
    private Set<String> meetingDays;
    private int count;
    private int memberCount;
    private List<nicknameImage> members;    // 멤버들의 닉네임, 프로필 사진

}
