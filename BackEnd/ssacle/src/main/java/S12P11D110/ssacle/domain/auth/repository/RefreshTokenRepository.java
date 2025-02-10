package S12P11D110.ssacle.domain.auth.repository;

import S12P11D110.ssacle.domain.auth.entity.RefreshToken;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    @Value("${jwt.refresh.token.expiration.seconds}")
    private long refreshTokenExpiration;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* refresh token 을 redis 에 저장 */
    public void save(RefreshToken refreshToken) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken.getRefreshToken(), refreshToken.getUserId());
        redisTemplate.expire(refreshToken.getRefreshToken(), refreshTokenExpiration, TimeUnit.DAYS);
    }

    /* refresh token 으로 userId 조회 */
    public RefreshToken findById(final String refreshToken) throws AuthErrorException {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String userId = valueOperations.get(refreshToken);;
        // userId가 없으면 refesh token이 만료되었다고 예외 처리
        if (userId == null) {
            throw new AuthErrorException(AuthErrorStatus.REFRESH_EXPIRED);
        }
        return new RefreshToken(refreshToken, userId);
    }

    /* 로그아웃 또는 탈퇴 시 refresh token 정보 삭제*/
    public void deleteByUserId(String userId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // Redis에 저장된 모든 키 조회
        Set<String> keys = redisTemplate.keys("*");

        if (keys != null) {
            for (String key : keys) {
                String storedUserId = valueOperations.get(key);
                if (storedUserId != null && storedUserId.equals(userId)) {
                    // 해당 userId와 연결된 Refresh Token 삭제
                    redisTemplate.delete(key);
                    break;
                }
            }
        }
    }
}
