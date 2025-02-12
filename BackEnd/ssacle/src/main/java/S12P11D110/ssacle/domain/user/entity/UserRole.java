package S12P11D110.ssacle.domain.user.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    // 사용자 권한 (로그인 : 일반 유저 / 로그인 + 싸피 인증 : 싸피생)
    USER("ROLE_USER", "일반 유저"),
    SSAFYUSER("ROLE_SSAFYUSER", "싸피생"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
