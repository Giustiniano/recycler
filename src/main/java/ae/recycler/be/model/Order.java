package ae.recycler.be.model;

import ae.recycler.be.api.views.serializers.OrderBody;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;


@Getter
@Builder
public class Order extends BaseModel {
    @Property
    @Setter
    private Integer boxes;
    @Relationship(type = "PICKUP_FROM", direction = Relationship.Direction.OUTGOING)
    @Setter
    @EqualsAndHashCode.Exclude
    private List<Address> pickupAddresses;
    @Relationship(type = "SUBMITTED_BY", direction = Relationship.Direction.OUTGOING)
    @Setter
    @EqualsAndHashCode.Exclude
    private List<Customer> submittedBy;
    @Relationship(type = "HAS_STATUS", direction = Relationship.Direction.OUTGOING)
    @Setter
    @EqualsAndHashCode.Exclude
    private List<OrderStatus> orderStatuses;
    @Relationship(type = "DELIVER_TO", direction = Relationship.Direction.OUTGOING)
    @Setter
    @EqualsAndHashCode.Exclude
    private List<Address> deliveryAddresses;
    @Relationship(type = "PICKUP_WITH", direction = Relationship.Direction.OUTGOING)
    @Setter
    @EqualsAndHashCode.Exclude
    private List<Vehicle> assignedVehicle;

}
