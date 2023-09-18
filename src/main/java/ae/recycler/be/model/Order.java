package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Getter
@Builder
@Node
@EqualsAndHashCode
@ToString
public class Order {
    @GeneratedValue
    @Id
    private UUID id;
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
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant lastModified;

}
