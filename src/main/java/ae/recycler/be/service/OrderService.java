package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.OrderBody;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;


    public Mono<Order> saveOrder(Mono<OrderBody> orderBody) {
                                               // first make sure the user exists
        return orderBody.flatMap(orderBody1 -> customerRepository.findById(orderBody1.getUserId()).
                switchIfEmpty(Mono.error(new IllegalArgumentException("Unable to find customer")))
                .flatMap(customer -> customerRepository.
                        // second, check that the supplied address is one of those associated to the user
                        findCustomerAddress(orderBody1.getUserId(), orderBody1.getPickupAddress())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Unable to find address for customer")))
                        // save the order
                        .flatMap(address -> orderRepository
                                .save(Order.builder()
                                        .boxes(orderBody1.getBoxes())
                                        .orderStatuses(List.of(new OrderStatus(OrderStatusEnum.SUBMITTED)))
                                        .pickupAddresses(List.of(address))
                                        .submittedBy(List.of(customer))
                                        .build()))));
    }
}





