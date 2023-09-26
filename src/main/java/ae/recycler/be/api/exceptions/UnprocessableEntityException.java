package ae.recycler.be.api.exceptions;


import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException{
    private static final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    public UnprocessableEntityException(String message, String detail, Throwable cause) {
        super(status, message, detail, cause);
    }
    public UnprocessableEntityException(String message, Throwable cause){
        super(status, message, null, cause);
    }
    public UnprocessableEntityException(String message){
        super(status, message, null, null);
    }
}
