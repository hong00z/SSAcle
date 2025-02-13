package S12P11D110.ssacle.global.config;


import S12P11D110.ssacle.domain.auth.filter.JwtExceptionFilter;
import S12P11D110.ssacle.domain.auth.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;


// GPT 도움!!
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtExceptionFilter jwtExceptionFilter;

    /**
     * Spring Security 설정 : SecurityFilterChain 빈
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                /* Spring Security 6.1부터 http.cors() 메서드가 Deprecated되어 메서드 체이닝의 사용을 지양하고 람다식을 통해 함수형으로 설정하게 지향하고 있습니다. */
                // CORS 설정 활성화
                .cors(withDefaults())
                // CSRF 비활성화 (Swagger 테스트 시 편리)
                .csrf(AbstractHttpConfigurer::disable)
                // 기본 로그인 폼 미사용
                .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer.disable())
                // 세션을 사용하지 않음 (JWT 기반 인증)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headersConfigurer -> headersConfigurer
                        .frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::sameOrigin // X-Frame-Options 헤더를 SAME ORIGIN으로 설정
                        ))
                // Swagger UI 및 OpenAPI 관련 경로 허용
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",       // Swagger UI 관련 경로
                                "/v3/api-docs/**",              // OpenAPI 문서 관련 경로
                                "/swagger-resources/**",        // Swagger 리소스 허용
                                "/webjars/**",                  // Swagger UI에서 사용하는 WebJars 리소스 허용
                                "/error",                       // 에러 핸들링 경로
                                "/api/auth/**"                 // ✅ 인증 관련 API (로그인, 회원가입 등)
                        ).permitAll()
                        .requestMatchers(
                                "/api/user/**",
                                "/api/studies/**"                   // ✅ [수정] 모든 스터디 API JWT 없이 허용
                        ).hasAnyRole("USER", "SSAFYUSER")     // 로그인 권한 필요
                        .anyRequest().authenticated())              // 나머지는 인증 필요
                // ✅ JWT 필터 추가 (인증 처리)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                .addFilterAfter(jwtExceptionFilter, JwtRequestFilter.class)
                .build();
    }

    /**
     * Spring Security 인증 관리 : AuthenticationManager 빈 등록
     * ID/PW 로그인도 지원할 거면 유지하고, JWT만 사용할 거면 제거해도 됨!
     */
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
}
