package S12P11D110.ssacle.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserRegisterRequest {
    private String nickname;
    private String email;
    private String image;
}