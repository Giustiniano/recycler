package ae.recycler.be.model;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;


@Getter
@Setter
@Builder
@Node
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private Integer boxes;
    @Relationship(type = "PICKUP_FROM", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private Address pickupAddress;
    @Relationship(type = "SUBMITTED_BY", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private Customer submittedBy;
    @Property
    private OrderStatusEnum orderStatus;
    @Property
    private Integer pickupOrder;
    @Relationship(type = "DELIVER_TO", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private Address deliveryAddresses;
    @Relationship(type = "PICKUP_WITH", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private Vehicle assignedVehicle;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant lastModified;

    public boolean isUpdateable(){
        return switch (this.orderStatus){
            case SUBMITTED, ASSIGNED, PICKING_UP, SCHEDULED -> true;
            default -> false;
        };
    }


}
