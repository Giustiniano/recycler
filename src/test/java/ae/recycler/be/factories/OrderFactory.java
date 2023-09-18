package ae.recycler.be.factories;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderFactory {

    public static Order build(){
        return Order.builder().submittedBy(List.of(CustomerFactory.build()))
        .pickupAddresses(List.of(AddressFactory.build()))
                .orderStatuses(List.of(OrderStatus.builder().orderStatus(OrderStatusEnum.SUBMITTED).build()))
                .boxes(1).build();
    }


}
