package S12P11D110.ssacle.domain.auth.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.Map;

// 카카오에서 받은 유저 정보를 담은 DTO
@Data
@RequiredArgsConstructor
public class KakaoUserInfoDto {
    private Long id;  // 카카오에서 제공하는 고유 ID
    private String nickname;
    private String email;
    private String image;
    private Map<String, Object> kakao_account;
    private Map<String, Object> properties;

    public KakaoUserInfoDto(Map<String, Object> attributes) {
        this.id = (Long) attributes.get("id");
        this.properties = (Map<String, Object>) attributes.get("properties");
        this.kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        this.nickname = (String) properties.get("nickname");
        this.email = (String) kakao_account.get("email");
        this.image = (String) properties.get("profile_image");

    }
}
