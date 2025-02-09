package S12P11D110.ssacle.domain.tempUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    private String nickname;
//    private String image;
    private Set<String> topics;
    private Set<String> meetingDays;

}