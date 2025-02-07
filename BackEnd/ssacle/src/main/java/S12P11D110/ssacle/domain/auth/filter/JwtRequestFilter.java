package S12P11D110.ssacle.domain.auth.filter;


import S12P11D110.ssacle.domain.auth.provider.JwtProvider;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JwtRequestFilter : API 요청이 올 때마다 Access 토큰이 유효한지 확인
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * SecurityContext에 Access Token으로부터 뽑아온 인증 정보를 저장하는 메서드
     *
     *  doFilterInternal() 내부에서 수행하는 작업:
     *  – HTTP 쿠키 or 헤더에서 JWT 가져오기
     *  – 요청에 JWT가 있으면 유효성을 검사하고 사용자 이름을 구문 분석한다.
     *  – 사용자 이름에서 UserDetails를 가져와 인증 개체를 만든다.
     *  – setAuthentication(authentication) 메서드를 사용하여 SecurityContext에서 현재 UserDetails를 설정한다.
     *
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // "/api/auth/kakao 경로로의 요청은 필터를 실행하지 않도록 걸러낸다.
        if (!request.getRequestURI().equals("/api/auth/kakao") && !request.getRequestURI().equals("/api/auth/newToken")) {
            // request: 헤더에서 넘어오는 JWT
            String jwt = resolveToken(request);
            log.info("jwt token = {}", jwt);

            // token 검사
            try {
                if (jwtProvider.validateToken(jwt)) {
                    Authentication authentication = jwtProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (AuthErrorException e) {
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Header에서 토큰값 추출하는 메서드
     */
    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(7);  // "Bearer "를 제외한 토큰 값
        }
        return null;
    }
}
