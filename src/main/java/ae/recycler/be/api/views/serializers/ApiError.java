package ae.recycler.be.api.views.serializers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@Getter
public class ApiError {
    private final HttpStatus status;
    private final String error, detail;
    private final String created;

    public ApiError(HttpStatus status, String error, String detail) {
        this.status = status;
        this.error = error;
        this.detail = detail;
        this.created = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now());
    }
}
