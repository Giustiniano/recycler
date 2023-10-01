package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.OrderStatus;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.events.OrderEventProducer;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@Slf4j
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private OrderEventProducer producer;



    public Mono<Order> saveOrder(Mono<NewOrderRequest> orderBody) {
        // first make sure the user exists
        return orderBody.flatMap(newOrderRequest1 -> customerRepository.findById(newOrderRequest1.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Customer not found")))
                .flatMap(customer -> customerRepository.findCustomerAddress(
                        newOrderRequest1.getCustomerId(), newOrderRequest1.getPickupAddress()
                ).switchIfEmpty(Mono.error(
                        new IllegalStateException("Cannot find pickup address in customer address list"))
                ).flatMap(address -> {
                            SortedSet<OrderStatus> statuses = new TreeSet<>();
                            statuses.add(OrderStatus.builder().orderStatus(OrderStatusEnum.SUBMITTED).build());
                            return orderRepository.save(Order.builder()
                                    .orderStatuses(statuses)
                                    .pickupAddress(address).boxes(newOrderRequest1.getBoxes())
                                    .submittedBy(customer).build());
                        }
                )));
    }

    public Mono<Order> findById(Mono<UUID> orderId){
        return orderRepository.findById(orderId);
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

    public void assignOrderToVehicle(OrderEvent orderEvent){
        orderRepository.findById(orderEvent.getOrderId()).flatMap(order ->{
            OrderStatusEnum orderState =  order.getOrderStatuses().last().getOrderStatus();
            if(orderState.equals(OrderStatusEnum.SUBMITTED)){
                Vehicle assignedVehicle = vehicleRepository.findClosestVehicle(orderEvent.getOrderId());
                if(assignedVehicle == null){
                    return Mono.error(new IllegalStateException("Unable to find vehicle to assign to this order"));
                }
                order.setAssignedVehicle(assignedVehicle);
                orderRepository.save(order);
            }
            else {
                log.debug("Order {} has state {}, nothing to do", orderEvent, orderState);
            }
            return Mono.just(order);
        }).switchIfEmpty(Mono.error(new IllegalStateException(String.format("Event %s cannot be " +
                "processed, because order with id %s cannot be found", orderEvent, orderEvent.getOrderId()))));
    }

    private Mono<Order> isOrderUpdatable(Order order){
        return order.isUpdateable() ? Mono.just(order) : Mono.error(
                new IllegalStateException(String.format("Order with status %s cannot be updated",
                        order.getOrderStatuses().last().getOrderStatus())));
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
            order.getOrderStatuses().add(OrderStatus.builder().orderStatus(newStatus).build());
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







