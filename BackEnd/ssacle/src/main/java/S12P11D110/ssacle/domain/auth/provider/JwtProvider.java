package S12P11D110.ssacle.domain.auth.provider;

import S12P11D110.ssacle.domain.auth.entity.CustomUserDetail;
import S12P11D110.ssacle.domain.auth.entity.RefreshToken;
import S12P11D110.ssacle.domain.auth.repository.RefreshTokenRepository;
import S12P11D110.ssacle.domain.auth.service.CustomUserDetailService;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

// JwtProvider : Access Token과 Refresh Token을 생성하고 검증하는 역할
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.access.token.expiration.seconds}") private static long accessTokenValidationTime;
    @Value("${jwt.token.secret.key}") private String secretKeyString;
    private Key secretKey;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailService userDetailService;

    @PostConstruct
    public void initializeSecretKey() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));
    }
    /**
     *  인증된 사용자에게 최초 발급할 Access Token 생성
     */
    public String generateAccessToken(Map<String, Object> claims, String subject) {
        // 토큰 생성시간
        Instant now = Instant.from(OffsetDateTime.now());
        /* claims를 subject보다 먼저 적용해야 subject가 claims에 추가된다.*/
        return Jwts.builder()
                .setClaims(claims)      // Claims: 사용자 관련 정보
                .setSubject(subject)    // Subject: JWT 에 대한 이름 추가(여기서는 이메일에 해당, claims에 자동으로 추가됨)
                .setExpiration(Date.from(now.plusMillis(accessTokenValidationTime)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성 메서드
     * - Access Token이 만료되었을 경우 이것으로 Access Token 재발급
     */
    public String generateRefreshToken(String userId) {
        // refresh token 생성
        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString(), userId);
        // db 저장
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getRefreshToken();
    }

    /**
     * Refresh 토큰으로 Access 토큰 재발급
     */
    public String reAccessToken(String token) throws AuthErrorException {

        RefreshToken refreshToken = refreshTokenRepository.findById(token);
        String userId = refreshToken.getUserId();
        // Optional<TempUser>에서 User를 가져옴 (없으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(AuthErrorStatus.GET_USER_FAILED));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", user.getRole());

        return generateAccessToken(claims, user.getEmail());
    }

    /**
     * UsernamePasswordAuthenticationToken으로 보내 인증된 유저인지 확인
     */
    public Authentication getAuthentication(String accessToken) throws ExpiredJwtException {
        Claims claims = getTokenBody(accessToken);
        // email로 UserDetail 가져오기
        CustomUserDetail userDetail = userDetailService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetail, "", userDetail.getAuthorities());
    }

    /**
     * 유효성 검사
     */
    public boolean validateToken(String token) throws AuthErrorException {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new AuthErrorException(AuthErrorStatus.INVALID_TOKEN); // 잘못된 토큰
        } catch (ExpiredJwtException e) {
            throw new AuthErrorException(AuthErrorStatus.EXPIRED_TOKEN); // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰입니다.");
        }
        return false;
    }

    /**
     * JWT Claims 꺼내기
     */
    private Claims getTokenBody(String jwtToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

}

