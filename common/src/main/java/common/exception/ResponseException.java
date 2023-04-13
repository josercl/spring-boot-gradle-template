package common.exception;

import org.springframework.http.HttpStatus;

public class ResponseException extends RuntimeException {
    protected final HttpStatus code;
    public ResponseException(HttpStatus code, String message) {
        super(message);
        this.code = code;
    }

    public HttpStatus getCode() {
        return code;
    }
}
