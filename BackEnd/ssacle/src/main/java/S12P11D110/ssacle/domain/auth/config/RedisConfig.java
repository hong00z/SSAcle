package S12P11D110.ssacle.domain.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


import java.time.Duration;

/* GPT 도움!!
Redis TTL 설정을 따로 관리
(Token 엔티티에 @RedisHash(timeToLive = 604800)을 쓰면 더 간단하지만, yml 설정과 일관성을 유지하려면 Config에서 적용하는 게 좋음)
 */
@Configuration
public class RedisConfig {
    // yml 설정 값 가져오기
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplate() {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
