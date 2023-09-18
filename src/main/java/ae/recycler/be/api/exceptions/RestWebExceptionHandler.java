package ae.recycler.be.api.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
class RestExceptionHandler {

    @ExceptionHandler(Exceptions.ResourceNotFoundException.class)
    public ResponseEntity<Mono<String>> notFound(Exceptions.ResourceNotFoundException ex) {
        log.debug("handling exception::" + ex);
        return ResponseEntity.notFound().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    @ExceptionHandler(Exceptions.BadRequestException.class)
    public ResponseEntity badRequest(Exceptions.ResourceNotFoundException ex) {
        log.debug("handling exception::" + ex);
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).build();
    }
}