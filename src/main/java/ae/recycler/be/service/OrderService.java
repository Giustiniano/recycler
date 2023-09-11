package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.OrderRequest;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;


    public Mono<Order> saveOrder(Mono<OrderRequest> orderBody) {
                                               // first make sure the user exists
        return orderBody.flatMap(orderRequest1 -> customerRepository.findById(orderRequest1.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Customer not found")))
                .flatMap(customer -> customerRepository.findCustomerAddress(
                        orderRequest1.getCustomerId(), orderRequest1.getPickupAddress()
                ).switchIfEmpty(Mono.error(
                        new IllegalStateException("Cannot find pickup address in customer address list"))
                ).flatMap(address -> orderRepository.save(Order.builder()
                                .orderStatuses(List.of(OrderStatus.builder().orderStatus(OrderStatusEnum.SUBMITTED)
                                                .build()))
                                .pickupAddresses(List.of(address))
                                .boxes(orderRequest1.getBoxes())
                                .submittedBy(List.of(customer))
                        .build()))));
    }
}





