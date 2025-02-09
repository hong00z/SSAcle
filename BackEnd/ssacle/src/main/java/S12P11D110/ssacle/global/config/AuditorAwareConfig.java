package S12P11D110.ssacle.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import javax.swing.text.html.Option;
import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorAware(){
        // 현재 사용자의 ID
        return () -> Optional.of("defaultUser");

//
//        return () -> {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            // 인증된 사용자가 없으면 Optional.empty() 반환
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return Optional.empty();
//            }
//
//            // 현재 사용자의 ID 가져오기 (UserDetails에서 꺼내야 함)
//            String userId = authentication.getName(); // 기본적으로 username을 가져옴
//
//            return Optional.of(userId);
//        };
//    }


    }
}
