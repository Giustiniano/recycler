package ae.recycler.be.api.exceptions;

import ae.recycler.be.api.views.serializers.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class RestWebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Mono<ApiError>> notFound(ResourceNotFoundException ex) {

        log.debug("handling exception::" + ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(
                        Mono.just(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getDetail())));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Mono<ApiError>> badRequest(BadRequestException ex) {
        log.debug("handling exception::" + ex);
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getDetail())));
    }
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Mono<ApiError>> badRequest(UnprocessableEntityException ex) {
        log.debug("handling exception::" + ex);
        return ResponseEntity.unprocessableEntity().contentType(MediaType.APPLICATION_JSON).body(
                Mono.just(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getDetail())));
    }
}