package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Builder
@ToString
@EqualsAndHashCode
@Getter
public class OrderResponse {
    private UUID id, customerId;
    private int boxes;
    private OrderStatusEnum orderStatus;
    private Instant createdDate;
    private Instant lastModified;
    private JsonAddress pickupAddress;
    public static OrderResponse fromOrder(Order order){
        if(order.getId() == null){
            return null;
        }
        return OrderResponse.builder().id(order.getId())
                .pickupAddress(JsonAddress.fromAddress(order.getPickupAddress()))
                .orderStatus(order.getOrderStatus()).boxes(order.getBoxes())
                .customerId(order.getSubmittedBy().getId()).createdDate(order.getCreatedDate())
                .lastModified(order.getLastModified()).build();
    }
    public static List<OrderResponse> fromOrders(List<Order> orders){
        List<OrderResponse> orderResponse = new ArrayList<>(orders.size());
        for(Order order : orders)
            orderResponse.add(OrderResponse.fromOrder(order));
        return orderResponse;
    }


}
