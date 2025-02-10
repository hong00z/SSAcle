package S12P11D110.ssacle.domain.tempUser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    // 사용자 권한 (로그인 : 일반 유저 / 로그인 + 싸피 인증 : 싸피생)
    USER("일반 유저"),
    SSAFYUSER("싸피생");

    private final String key;
}