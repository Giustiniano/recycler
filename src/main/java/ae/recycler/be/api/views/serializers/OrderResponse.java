package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import lombok.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
public class OrderResponse {
    private UUID orderId, customerId, pickupAddress;
    private int boxes;
    private OrderStatusEnum orderStatus;
    private Instant createdAt;
    private Instant updatedAt;
    public static OrderResponse fromOrder(Order order){
        return OrderResponse.builder().orderId(order.getId()).pickupAddress(order.getPickupAddresses().get(0).getId())
                .orderStatus(order.getOrderStatuses().get(0).getOrderStatus()).boxes(order.getBoxes())
                .customerId(order.getSubmittedBy().get(0).getId()).build();
    }
}
