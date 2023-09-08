package ae.recycler.be.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;


@Getter
@Setter
public class Customer extends BaseModel{
    @Property
    private String email;
    @Relationship(type = "HAS_ADDRESS", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private List<Address> addresses;
    @Relationship(type = "SUBMITTED_BY", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    private List<Order> submittedOrders;
}
