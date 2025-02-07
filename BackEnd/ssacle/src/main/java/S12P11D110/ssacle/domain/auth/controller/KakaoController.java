package S12P11D110.ssacle.domain.auth.controller;

import S12P11D110.ssacle.domain.auth.dto.KakaoUserInfoDto;
import S12P11D110.ssacle.domain.auth.dto.RefreshTokenDto;
import S12P11D110.ssacle.domain.auth.dto.TokenDto;
import S12P11D110.ssacle.domain.auth.provider.JwtProvider;
import S12P11D110.ssacle.domain.auth.service.KakaoUserService;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import S12P11D110.ssacle.global.exception.HttpStatusCode;
import S12P11D110.ssacle.global.exception.ResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name="Kakao Controller", description = "카카오 로그인 & JWT 관련 controller")
public class KakaoController {
    private final KakaoUserService kakaoUserService;
    private final JwtProvider jwtProvider;

    // (FE → BE) 카카오 access 토큰 전달
    @PostMapping("/api/auth/kakao")
    public ResultDto<Object> socialLogin(@RequestHeader HttpHeaders headers) throws AuthErrorException {

        String accessToken = Objects.requireNonNull(headers.getFirst("Authorization")).substring(7);

        if (accessToken.equals("")) throw new AuthErrorException(AuthErrorStatus.EMPTY_TOKEN);

        try {
            // token으로 카카오 사용자 정보 가져오기
            KakaoUserInfoDto kakaoUserInfo = kakaoUserService.getKakaoUserInfo(accessToken);

            // 회원가입/로그인 후 JWT 토큰 발급
            TokenDto tokenDto = kakaoUserService.joinorLogin(kakaoUserInfo);

            if (tokenDto.getType().equals("Signup")) {
                return ResultDto.of(HttpStatusCode.CREATED, "회원 가입 성공", tokenDto);
            } else {
                return ResultDto.of(HttpStatusCode.CREATED, "로그인 성공", tokenDto);
            }
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }

    // (FE ↔ BE) refresh 토큰으로 access 토큰 재발급
    @GetMapping("/api/auth/newToken")
    public ResultDto<Object> getNewToken(@RequestHeader HttpHeaders headers) {
        try {
            String refreshToken = Objects.requireNonNull(headers.getFirst("Authorization")).substring(7);
            String accessToken = jwtProvider.reAccessToken(refreshToken);
            return ResultDto.of(HttpStatusCode.OK, "토큰 재발급", RefreshTokenDto.of(accessToken));
        } catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }
}
