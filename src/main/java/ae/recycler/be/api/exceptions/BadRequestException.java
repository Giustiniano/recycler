package ae.recycler.be.api.exceptions;


import org.springframework.http.HttpStatus;


public class BadRequestException extends ApiException{
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;
    public BadRequestException(String message, String detail, Throwable cause) {
        super(status, message, detail, cause);
    }

}
