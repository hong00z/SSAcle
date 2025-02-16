package S12P11D110.ssacle.global.exception;


import lombok.Getter;

@Getter
public enum ApiErrorStatus {

    /**
     * User Api 관련 에러 코드
     */
    // 닉네임 중복 검사
    DUPLICATED_USER_NAME(HttpStatusCode.BAD_REQUEST, "이미 사용중인 닉네임입니다."),

    // 싸피생 인증
    INVALID_STUDENT_ID(HttpStatusCode.BAD_REQUEST, "등록되지 않은 학번입니다."),
    INVALID_STUDENT_INFO(HttpStatusCode.BAD_REQUEST, "유효하지 않은 회원 정보 입니다."),
    ALREADY_AUTHENTICATED(HttpStatusCode.BAD_REQUEST, "이미 인증된 교육생 정보입니다.");


    private final HttpStatusCode code;
    private final String msg;

    ApiErrorStatus(HttpStatusCode code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
