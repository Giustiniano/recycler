package ae.recycler.be.api.views.order;

import ae.recycler.be.api.exceptions.BadRequestException;

import java.util.UUID;

public class Validators {
    public static UUID validateOrderId(String id){
        try {
            return UUID.fromString(id);
        }
        catch (IllegalArgumentException ex){
            throw new BadRequestException(String.format("'%s' is not a valid order id", id), null, ex);
        }
    }
}
