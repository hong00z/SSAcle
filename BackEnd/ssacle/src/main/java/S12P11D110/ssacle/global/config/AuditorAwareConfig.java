package S12P11D110.ssacle.global.config;

import S12P11D110.ssacle.domain.auth.entity.CustomUserDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.text.html.Option;
import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorAware(){
        // 현재 사용자의 ID
//        return () -> Optional.of("defaultUser");


        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증된 사용자가 없으면 Optional.empty() 반환
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            // `CustomUserDetail`에서 사용자 ID 가져오기
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetail) {
                String userId = ((CustomUserDetail) principal).getId(); // ✅ 사용자 ID 가져오기
                return Optional.of(userId);
            }

            return Optional.empty();
        };
    }
}
