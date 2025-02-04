package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.tempUser.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


// 스터디원을 찾는 스터디에게 보여지는 추천 유저
@Data
@AllArgsConstructor
public class RecommendUserDTO {
    private String userId;
    private double similarity;
    private String nickName;
    private List<User.Topics> topics;
    private List<User.MeetingDays> meetingDays;
}
