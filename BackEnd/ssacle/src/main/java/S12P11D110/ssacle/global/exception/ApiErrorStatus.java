package S12P11D110.ssacle.global.exception;


import lombok.Getter;

@Getter
public enum ApiErrorStatus {

    /**
     * User Api 관련 에러 코드
     */
    DUPLICATED_USER_NAME(HttpStatusCode.BAD_REQUEST, "이미 사용중인 닉네임입니다.");



    private final HttpStatusCode code;
    private final String msg;

    ApiErrorStatus(HttpStatusCode code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
