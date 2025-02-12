package S12P11D110.ssacle.domain.auth.service;


import S12P11D110.ssacle.domain.auth.dto.KakaoUserInfoDto;
import S12P11D110.ssacle.domain.auth.dto.TokenDto;
import S12P11D110.ssacle.domain.auth.provider.JwtProvider;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.entity.UserRole;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoUserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    /**
     * 카카오 액세스 토큰으로 카카오 사용자 정보 받아오는 메서드
     * @param kakaoToken : access token
     * @return 사용자 정보를 담은 Dto
     */
    public KakaoUserInfoDto getKakaoUserInfo(String kakaoToken) {

        // HttpHeader 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + kakaoToken);

        // HttpHeader 담기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);


        // 사용자 정보 요청 (POST)
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class);

        // Http 응답 (JSON)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 응답 코드 확인
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AuthErrorException(AuthErrorStatus.SOCIAL_TOKEN_EXPIRED);
        }

        try {
            // 카카오 사용자 정보
            Map<String, Object> originAttributes = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            return new KakaoUserInfoDto(originAttributes);
        } catch (JsonProcessingException e) {
            throw new AuthErrorException(AuthErrorStatus.PARSE_FAILED);
        }
    }

    /**
     * 회원 가입/로그인 후 사용자 반환
     * - 이메일로 DB에 존재하는 회원인지 조회
     */
    public TokenDto joinorLogin(KakaoUserInfoDto kakaoUserInfo) {
        log.info("✅ [Step 5] joinorLogin() 실행");

        String email = kakaoUserInfo.getEmail();
        log.info("✅ [Step 6] 검색할 이메일: {}", email);
        return userRepository.findByEmail(email)
                .map(user -> createTokens(user, "Login")) // 존재하면 로그인
                .orElseGet(() -> {
                    log.info("🆕 [Step 7] 신규 회원가입 진행: {}", kakaoUserInfo);
                    User newUser = join(kakaoUserInfo);
                    log.info("🆕 [Step 8] 회원가입 완료: {}", newUser);
                    return createTokens(newUser, "Signup");
//                    return createTokens(join(kakaoUserInfo), "Signup"); // 없으면 회원가입 후 토큰 발급
                });
    }

    /**
     * 회원 가입
     */
    @Transactional
    public User join(KakaoUserInfoDto kakaoUserInfo) {
        User newUser = User.builder()
                .email(kakaoUserInfo.getEmail())
                .build();
        userRepository.save(newUser);
        log.info("join 성공 = {}", newUser.getNickname());
        return newUser;
    }

    /**
     * JWT 토큰 발급
     *@param user: 현재 로그인한 user
     *@param type: signup / login
     */
    private TokenDto createTokens(User user, String type) {
        // Access Token 생성
        String accessToken = delegateAccessToken(user.getUserId(), user.getEmail(), user.getRole());
        // Refresh Token 생성
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
        return new TokenDto(type, accessToken, refreshToken);
    }

    /**
     *  Access Token 생성
     */
    private String delegateAccessToken(String id, String email, UserRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        return jwtProvider.generateAccessToken(claims, email);
    }

}
