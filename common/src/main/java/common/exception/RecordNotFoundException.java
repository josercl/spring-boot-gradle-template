package common.exception;

import org.springframework.http.HttpStatus;

public class RecordNotFoundException extends ResponseException {
    public RecordNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
