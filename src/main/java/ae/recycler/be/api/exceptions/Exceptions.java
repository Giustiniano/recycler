package ae.recycler.be.api.exceptions;

import lombok.experimental.StandardException;

public class Exceptions {
    @StandardException
    public static class ResourceNotFoundException extends RuntimeException{}
    @StandardException
    public static class BadRequestException extends RuntimeException{}
}
