package ae.recycler.be.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String detail;
    public ApiException(HttpStatus status, String message, String detail, Throwable cause){
        super(message, cause);
        this.status = status;
        this.detail = detail;
    }
}
