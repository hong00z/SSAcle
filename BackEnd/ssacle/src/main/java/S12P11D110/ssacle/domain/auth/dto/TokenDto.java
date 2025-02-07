package S12P11D110.ssacle.domain.auth.dto;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
    private String type;    // Signup 또는 Login
    private String accessToken;
    private String refreshToken;
}
