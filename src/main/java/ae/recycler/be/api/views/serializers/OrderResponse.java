package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import lombok.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


@Builder
@ToString
@EqualsAndHashCode
@Getter
public class OrderResponse {
    private UUID orderId, customerId, pickupAddress;
    private int boxes;
    private OrderStatusEnum orderStatus;
    private Instant createdDate;
    private Instant lastModified;
    public static Mono<OrderResponse> fromOrder(Order order){
        return Mono.just(OrderResponse.builder().orderId(order.getId()).pickupAddress(order.getPickupAddresses().get(0)
                        .getId()).orderStatus(order.getOrderStatuses().get(0).getOrderStatus()).boxes(order.getBoxes())
                .customerId(order.getSubmittedBy().get(0).getId()).createdDate(order.getCreatedDate())
                .lastModified(order.getLastModified()).build());
    }
}
