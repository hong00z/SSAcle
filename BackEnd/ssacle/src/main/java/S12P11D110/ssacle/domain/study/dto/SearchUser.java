package S12P11D110.ssacle.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class SearchUser {
    private String userId;
    private String nickname;
    private String image;
    private String term;
    private String campus;
    private int countJoinedStudies;
    private Set<String> topics;
    private Set<String> meetingDays;
    private Set<String> joinedStudies;
    private Set<String> wishStudies;
    private Set<String> invitedStudies;

}