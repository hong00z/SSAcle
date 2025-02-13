package S12P11D110.ssacle.domain.study.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class WishPreMembers {
    private String userId;
    private String nickname;
    private String image;
    private String term;
    private String campus;
    private Set<String> topics;
    private Set<String> meetingDays;

}

