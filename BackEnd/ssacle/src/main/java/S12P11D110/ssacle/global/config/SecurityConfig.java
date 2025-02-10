package S12P11D110.ssacle.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// GPT: 전체
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (Swagger 테스트 시 편리)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))// X-Frame-Options 헤더를 SAMEORIGIN으로 설정
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        "/swagger-ui/**", // Swagger UI 관련 경로 허용
                        "/v3/api-docs/**", // OpenAPI 문서 관련 경로 허용
                        "/swagger-resources/**", // Swagger 리소스 허용
                        "/webjars/**", // Swagger UI에서 사용하는 WebJars 리소스 허용
                        "/user/signup",
                        "/user/signin",
                        "/api/studies",
                        "/error",
                        "/api/studies/**",
                        "/api/users/**"

                ).permitAll()
                .anyRequest().authenticated()) // 나머지는 인증 필요
                .cors(Customizer.withDefaults()) // Spring Security에 CORS 적용
                .formLogin(AbstractHttpConfigurer::disable); // 기본 로그인 화면 비활성화

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8080" ) // Swagger UI 실행 URL

                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
