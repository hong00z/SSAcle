package S12P11D110.ssacle.domain.user.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisterResponse {
    private String userId;
    private String nickname;
    private String email;
}
