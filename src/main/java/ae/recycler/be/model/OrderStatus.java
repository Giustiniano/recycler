package ae.recycler.be.model;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Property;

@Getter
@Setter
@AllArgsConstructor
public class OrderStatus extends BaseModel{
    @Property
    private OrderStatusEnum orderStatus;

    public static void main(String[] args) {
        new OrderStatus(OrderStatusEnum.SUBMITTED);
    }
}
