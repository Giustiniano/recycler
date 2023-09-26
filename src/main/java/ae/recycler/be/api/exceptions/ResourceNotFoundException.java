package ae.recycler.be.api.exceptions;


import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException{
    private static final HttpStatus status = HttpStatus.NOT_FOUND;
    public ResourceNotFoundException(String message, String detail, Throwable cause) {
        super(status, message, detail, cause);
    }
    public ResourceNotFoundException(String message, Throwable cause){
        super(status, message, null, cause);
    }
    public ResourceNotFoundException(String message){
        super(status, message, null, null);
    }
}
