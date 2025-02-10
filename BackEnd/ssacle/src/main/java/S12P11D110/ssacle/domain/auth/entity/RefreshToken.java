package S12P11D110.ssacle.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Getter
@AllArgsConstructor
@RedisHash(value="jwtToken")
public class RefreshToken {
    @Id
    private final String refreshToken;  // Key
    private final String userId; // Value : 어떤 유저의 토큰인지 식별
}
