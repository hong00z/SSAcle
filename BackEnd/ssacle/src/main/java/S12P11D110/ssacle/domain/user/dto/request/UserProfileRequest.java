package S12P11D110.ssacle.domain.user.dto.request;


import lombok.*;
import java.util.Set;


@Getter
public class UserProfileRequest {
    private String nickname;
    private String image;
    private Set<String> topics;
    private Set<String> meetingDays;
}
