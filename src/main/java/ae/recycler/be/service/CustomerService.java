package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.Address;
import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.events.OrderEventProducer;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class CustomerService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderEventProducer producer;

    public Mono<Order> submitOrder(Mono<NewOrderRequest> orderBody) {
        // first make sure the user exists
        return orderBody.flatMap(newOrderRequest1 -> customerRepository.findById(newOrderRequest1.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Customer not found")))
                .flatMap(customer -> customerRepository.findCustomerAddress(
                        newOrderRequest1.getCustomerId(), newOrderRequest1.getPickupAddress()
                ).switchIfEmpty(Mono.error(
                        new IllegalStateException("Cannot find pickup address in customer address list"))
                ).flatMap(address -> orderRepository.save(Order.builder()
                        .orderStatus(OrderStatusEnum.SUBMITTED)
                        .pickupAddress(address).boxes(newOrderRequest1.getBoxes())
                        .submittedBy(customer).build())
                )));
    }

    public Mono<Order> updateOrder(UUID orderId, OrderUpdateRequest dataToUpdate) {
        return orderRepository.findById(orderId).switchIfEmpty(
                        Mono.error(new IllegalStateException("Order with the given id was not found")))
                .flatMap(this::isOrderUpdatable)
                .flatMap(updateableOrder -> updatePickupAddress(updateableOrder, dataToUpdate.getNewPickupAddress()))
                .flatMap(orderWithUpdatedPickupAddress -> updateStatus(orderWithUpdatedPickupAddress, dataToUpdate.getNewStatus()))
                .flatMap(orderWithUpdatedStatus -> updateBoxes(orderWithUpdatedStatus, dataToUpdate.getNewBoxesCount()))
                .flatMap(fullyUpdatedOrder -> orderRepository.save(fullyUpdatedOrder))
                .flatMap(savedOrder -> producer.sendOrderEvent(OrderEvent.fromOrder(savedOrder)).flatMap(eventSendResult
                        -> {
                    if(eventSendResult.exception() != null){
                        return Mono.error(eventSendResult.exception());
                    }
                    return Mono.just(savedOrder);
                }));
    }

    public Mono<Address> saveCustomerAddress(UUID customerId, Address address){
        return customerRepository.saveNewCustomerAddress(customerId, address.toMap())
                .map(Address::fromAddress).switchIfEmpty(Mono.error(
                new IllegalArgumentException("Address not saved, customer was not found")));
    }

    public Mono<List<Address>> getCustomerAddresses(UUID customerId){
        return customerRepository.findById(customerId)
                .map(Customer::getAddresses).map(Address::fromAddress)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")));
    }


    public Mono<List<OrderResponse>> getCustomerOrders(UUID customerId){
        return customerRepository.findById(customerId)
                .flatMap(customer -> orderRepository.findOrderBySubmittedByOrderByCreatedDateAsc(customer.getId(),
                                List.of(OrderStatusEnum.SUBMITTED, OrderStatusEnum.ASSIGNED,
                                        OrderStatusEnum.PICKING_UP))
                        .collectList()).map(OrderResponse::fromOrders);
    }
    private Mono<Order> isOrderUpdatable(Order order){
        return order.isUpdateable() ? Mono.just(order) : Mono.error(
                new IllegalStateException(String.format("Order with status %s cannot be updated",
                        order.getOrderStatus())));
    }
    private Mono<Order> updatePickupAddress(Order order, UUID address){
        if(address == null){
            return Mono.just(order);
        }
        return customerRepository.findCustomerAddress(order.getSubmittedBy().getId(), address)
                .switchIfEmpty(Mono.error(new IllegalStateException("Unable to find address"))).
                flatMap(address1 -> {
                    order.setPickupAddress(address1); return Mono.just(order);});
    }

    private Mono<Order> updateStatus(Order order, OrderStatusEnum newStatus){
        if(newStatus != null){
            order.setOrderStatus(newStatus);
        }

        return Mono.just(order);
    }

    private Mono<Order> updateBoxes(Order order, Integer newBoxesCount){
        if(newBoxesCount != null){
            order.setBoxes(newBoxesCount);
        }
        return Mono.just(order);
    }
}
