package S12P11D110.ssacle.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;


@Getter
@Builder
public class UserProfileResponse {
    private String nickname;
    private String image;
    private String term;
    private String campus;
    private Set<String> topics;
    private Set<String> meetingDays;
}
