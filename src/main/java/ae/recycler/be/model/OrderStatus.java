package ae.recycler.be.model;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@Node
public class OrderStatus extends BaseModel implements Comparable<OrderStatus>{
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private OrderStatusEnum orderStatus;


    @Override
    public int compareTo(OrderStatus o) {
        return this.getLastModified().compareTo(o.getLastModified());
    }

    public Instant getCreatedDate() {
        return super.getCreatedDate() != null ? super.getCreatedDate() : Instant.now();
    }

    public Instant getLastModified() {
        return super.getLastModified() != null ? super.getLastModified() : Instant.now();
    }

}
