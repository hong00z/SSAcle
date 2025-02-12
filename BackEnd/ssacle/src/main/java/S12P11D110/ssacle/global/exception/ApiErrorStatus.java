package S12P11D110.ssacle.global.exception;


import lombok.Getter;

@Getter
public enum ApiErrorStatus {

    /**
     * User Api 관련 에러 코드
     */
    DUPLICATED_USER_NAME(HttpStatusCode.BAD_REQUEST, "이미 사용중인 닉네임입니다."),


    /**
     * Study Api 관련 에러 코드
     */

    /**
     * Feed Api 관련 에러 코드
     */
    NOT_EXIST(HttpStatusCode.BAD_REQUEST, " 존재하지 않습니다."),

    /**
     * Socket 통신 관련 에러 코드
     */
    SOCKET_ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR, "소켓 통신 중 문제 발생"),
    CONNECT_FAILED(HttpStatusCode.INTERNAL_SERVER_ERROR, "소켓 연결 실패"),
    DISCONNECT_FAILED(HttpStatusCode.INTERNAL_SERVER_ERROR, "연결 해제 실패"),
    PORT_ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR, "포트 접속 실패");


    private final HttpStatusCode code;
    private final String msg;

    ApiErrorStatus(HttpStatusCode code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
