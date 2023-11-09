package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import lombok.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;


@Builder
@ToString
@EqualsAndHashCode
@Getter
public class OrderResponse {
    private UUID orderId, customerId;
    private int boxes;
    private OrderStatusEnum orderStatus;
    private Instant createdDate;
    private Instant lastModified;
    private Address pickupAddress;
    public static OrderResponse fromOrder(Order order){
        return OrderResponse.builder().orderId(order.getId())
                .pickupAddress(Address.fromAddress(order.getPickupAddress()))
                .orderStatus(order.getOrderStatus()).boxes(order.getBoxes())
                .customerId(order.getSubmittedBy().getId()).createdDate(order.getCreatedDate())
                .lastModified(order.getLastModified()).build();
    }


}
