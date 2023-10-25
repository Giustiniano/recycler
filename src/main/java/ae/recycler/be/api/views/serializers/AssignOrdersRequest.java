package ae.recycler.be.api.views.serializers;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignOrdersRequest {
    private UUID driver, vehicle;
}
