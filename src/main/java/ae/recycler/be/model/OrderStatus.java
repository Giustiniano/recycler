package ae.recycler.be.model;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.UUID;

@Getter
@Setter
@Builder
@Node
public class OrderStatus extends BaseModel{
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private OrderStatusEnum orderStatus;

}
