package S12P11D110.ssacle.domain.auth.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenDto {
    private String accessToken;

    public static RefreshTokenDto of(String accessToken) {
        return new RefreshTokenDto(accessToken);
    }
}
