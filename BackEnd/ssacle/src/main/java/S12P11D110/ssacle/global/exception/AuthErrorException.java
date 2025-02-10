package S12P11D110.ssacle.global.exception;


import lombok.Getter;

@Getter
public class AuthErrorException extends RuntimeException {
    private final HttpStatusCode code;
    private final String errorMsg;

    public AuthErrorException(AuthErrorStatus authStatus) {
        this.code = authStatus.getStatusCode();
        this.errorMsg = authStatus.getMsg();
    }
}
