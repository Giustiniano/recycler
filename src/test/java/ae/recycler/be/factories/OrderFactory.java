package ae.recycler.be.factories;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
@Builder
public class OrderFactory {

    public static Order build(){
        SortedSet<OrderStatus> statuses = new TreeSet<>();
        statuses.add(OrderStatus.builder().orderStatus(OrderStatusEnum.SUBMITTED).build());
        return Order.builder().submittedBy(CustomerFactory.build())
        .pickupAddress(AddressFactory.build())
                .orderStatuses(statuses)
                .boxes(1).build();
    }


}
