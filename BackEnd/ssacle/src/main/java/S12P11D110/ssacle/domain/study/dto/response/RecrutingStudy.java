package S12P11D110.ssacle.domain.study.dto.response;

import S12P11D110.ssacle.domain.study.dto.nicknameImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
@AllArgsConstructor
@Builder
public class RecrutingStudy {
    private String studyId;
    private String studyName;
    private String topic;
    private Set<String> meetingDays;
    private int count;
    private int memberCount;
    private List<nicknameImage> members;    // 멤버들의 닉네임, 프로필 사진, 스터디 팀장 여부
}
