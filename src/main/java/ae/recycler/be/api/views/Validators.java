package ae.recycler.be.api.views;

import ae.recycler.be.api.exceptions.BadRequestException;

import java.util.UUID;

public class Validators {
    public static UUID validateId(String id, String err){
        try {
            return UUID.fromString(id);
        }
        catch (IllegalArgumentException ex){
            throw new BadRequestException(err, null, ex);
        }
    }
}
