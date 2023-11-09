package ae.recycler.be.service.events.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private UUID pickupAddress, submittedBy, assignedVehicle, orderId;
    private OrderStatusEnum currentStatus;
    private Instant lastModified;

    public static OrderEvent fromOrder(Order order){
        var builder = OrderEvent.builder().orderId(order.getId()).
                currentStatus(order.getOrderStatus())
                .lastModified(order.getLastModified()).pickupAddress(order.getPickupAddress().getId())
                .submittedBy(order.getSubmittedBy().getId());
        if(order.getAssignedVehicle() != null){
            builder.assignedVehicle(order.getAssignedVehicle().getId());
        }
        return builder.build();
    }

}
