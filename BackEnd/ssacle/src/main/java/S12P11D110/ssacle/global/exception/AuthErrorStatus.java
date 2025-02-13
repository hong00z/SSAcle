package S12P11D110.ssacle.global.exception;

import lombok.Getter;

@Getter
public enum AuthErrorStatus {
    /**
     * 인증 문제
     */
    GET_SOCIAL_INFO_FAILED(HttpStatusCode.UNAUTHORIZED, "소셜 로그인 회원 정보 조회 실패"),
    GET_USER_FAILED(HttpStatusCode.UNAUTHORIZED, "회원 정보 조회 실패"),

    /**
     * 토큰 관련 에러
     */
    SOCIAL_TOKEN_EXPIRED(HttpStatusCode.UNAUTHORIZED, "소셜 토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatusCode.BAD_REQUEST, "유효하지 않은 토큰입니다."),
    EMPTY_TOKEN(HttpStatusCode.BAD_REQUEST, "토큰이 없습니다."),
    EXPIRED_TOKEN(HttpStatusCode.UNAUTHORIZED, "토큰이 만료되었습니다."),
    REFRESH_EXPIRED(HttpStatusCode.UNAUTHORIZED, "refresh 토큰이 만료되었습니다."),
    PARSE_FAILED(HttpStatusCode.BAD_REQUEST, "JSON 파싱 오류"),
    TOKEN_GENERATION_FAILED(HttpStatusCode.INTERNAL_SERVER_ERROR, "토큰 생성 실패");

    private final HttpStatusCode statusCode;
    private final String msg;

    AuthErrorStatus(HttpStatusCode statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }
}
