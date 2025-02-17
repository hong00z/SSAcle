package S12P11D110.ssacle.domain.auth.dto;

import S12P11D110.ssacle.domain.user.entity.UserRole;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String type;    // Signup 또는 Login
    private String accessToken;
    private String refreshToken;
    private boolean isAuth;    // 싸피생 인증 구분 (False : 일반 사용자 / True : 싸피생)
}
