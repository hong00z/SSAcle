package S12P11D110.ssacle.domain.auth.repository;

import S12P11D110.ssacle.domain.auth.entity.RefreshToken;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/* GPT 도움! (refreshToken을 Redis에서 저장 및 조회)
Redis를 사용하는 2가지 방식
1. Redis Template 클래스를 사용하는 방법
2. Redis Repository를 사용하는 방법
*/
@Repository
@ComponentScan
@RequiredArgsConstructor
public class RefreshTokenRepository{
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;
    public RefreshTokenRepository(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* refresh token 을 redis 에 저장 */
    public void save(RefreshToken refreshToken) {
        ValueOperations<String, Long> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken.getRefreshToken(), Long.valueOf(refreshToken.getUserId()));
        redisTemplate.expire(refreshToken.getRefreshToken(), 21L, TimeUnit.DAYS);
    }

    public RefreshToken findById(final String refreshToken) throws AuthErrorException {
        ValueOperations<String, Long> valueOperations = redisTemplate.opsForValue();
        String userId = String.valueOf(valueOperations.get(refreshToken));
        if (Objects.isNull(userId)) {
            throw new AuthErrorException(AuthErrorStatus.REFRESH_EXPIRED);
        }
        return new RefreshToken(refreshToken, userId);
    }
}
